# Moduł AI — jak zaimplementowano

Cały kod AI żyje w `core/ai`. Publiczny kontrakt to dwa interfejsy domenowe:
`AiTutorService` (rozmowa, analiza, generowanie ćwiczeń, ekstrakcja pamięci,
feedback wymowy, tłumaczenie) i `MemoryRetrievalService` (RAG). Implementacje:
`OpenAiTutorService` i `MemoryManager`.

## Prompty (`core/ai/prompt/PromptBuilder.kt`)

Wszystkie system prompty są w jednym miejscu — recenzowalne, testowalne,
wersjonowane jak reszta kodu. Pięć promptów:

1. **Tutor rozmowy** (`tutorSystemPrompt`) — instrukcje: tylko angielski, poziom
   CEFR steruje słownictwem/tempem (`levelGuidance`), odpowiedzi krótkie
   (1–3 zdania, bo są czytane przez TTS — bez markdown/emoji), naturalne
   przeformułowanie błędu zamiast wykładu, wstrzyknięte wspomnienia długoterminowe
   i cele nauki z onboardingu.
2. **Analiza rozmowy** (`analysisSystemPrompt`) — zwraca JSON: `summary`,
   `fluency_score` (0–100), `focus_tip`, `errors[]` (kategoria, oryginał,
   poprawka, wyjaśnienie **po polsku**), `vocabulary[]`. Limit 8 błędów / 8 słówek.
3. **Generator ćwiczeń** (`exerciseGenerationSystemPrompt`) — na wejściu lista
   błędów z `id`; jedno ćwiczenie per błąd, typ FILL_GAP/MULTIPLE_CHOICE/TRANSLATION,
   odpowiedź krótka (≤6 słów) by dało się sprawdzić porównaniem stringów.
4. **Konsolidacja pamięci** (`memoryExtractionSystemPrompt`) — ekstrakcja max 5
   wspomnień (FACT/PREFERENCE/WEAKNESS/GOAL/PROGRESS) z transkryptu, z listą już
   znanych faktów żeby uniknąć duplikatów; jawny zakaz zapisywania danych
   wrażliwych (zdrowie, poglądy, adresy, finanse).
5. **Feedback wymowy** (`pronunciationFeedbackSystemPrompt`) — 2 zdania po polsku,
   typowe błędy Polaków (th, w/v, długość samogłosek, ubezdźwięcznianie).

## Pamięć krótkoterminowa

Okno kontekstu = ostatnie 30 wiadomości (`SendMessageUseCase.CONTEXT_WINDOW_MESSAGES`),
wysyłane w całości jako historia do modelu przy każdej odpowiedzi.

## Pamięć długoterminowa i RAG (`core/ai/MemoryManager.kt`)

Pipeline po zakończeniu rozmowy (`ConsolidateMemoryUseCase`, uruchamiany w tle przez
`MemoryConsolidationWorker`):

```
transkrypt rozmowy → LLM ekstrahuje wspomnienia (JSON)
  → MemoryRepositoryImpl.upsert() dedupikuje po treści (case-insensitive)
  → MemoryManager.backfillEmbeddings() dolicza embeddingi dla nowych wpisów
  → AiMemoryDao.prune(maxCount=200) usuwa najstarsze/najmniej istotne
```

Wyszukiwanie (RAG) — `MemoryManager.retrieveRelevant(query, topK=8)`:
1. Jeśli wspomnień jest ≤ `topK`, zwraca wszystkie (bez wywołania sieci).
2. W przeciwnym razie liczy embedding zapytania (`OpenAiDataSource.embed`) i sortuje
   wspomnienia po podobieństwie kosinusowym (czysty Kotlin, `cosineSimilarity`,
   z niewielkim boostem za `importance` jako tie-breaker).
3. **Degradacja offline**: jeśli embedding zapytania się nie uda (brak sieci),
   zwraca top-K po `importance DESC, updatedAt DESC` (kolejność już taka z
   `AiMemoryDao.getAll()`) — rozmowa nadal jest spersonalizowana bez sieci do
   pamięci, sieć jest potrzebna tylko do samej rozmowy.

Embeddingi (`text-embedding-3-small`, 1536 float) są przechowywane jako
`FloatArray` w domenie i `ByteArray` (little-endian) w Room.

## Analiza błędów → ćwiczenia → SRS

```
AnalyzeConversationUseCase: transkrypt → analiza LLM → zapis UserError + VocabularyItem
GenerateExercisesUseCase:   nierozwiązane UserError → LLM → Exercise (SrsState świeży)
SubmitExerciseAnswerUseCase: odpowiedź → Sm2Scheduler.next() → nowy SrsState
                             → po 2 poprawnych powtórzeniach: UserError.markResolved()
```

## SM-2 (`core/domain/revision/Sm2Scheduler.kt`)

Czysta funkcja, bez zależności od Androida/AI — łatwo testowalna (patrz
`Sm2SchedulerTest`). Ocena < 3 (`AGAIN`): reset powtórzeń, ponowne pytanie za
10 minut w tej samej sesji. Ocena ≥ 3: 1. powtórzenie → 1 dzień, 2. → 6 dni,
kolejne → `poprzedni_interwał × easeFactor`. `easeFactor` aktualizowany klasycznym
wzorem SM-2, dolny limit 1.3.

## Analiza wymowy (`EvaluatePronunciationUseCase`)

Wynik 0–100 = `0.7 × podobieństwo_tekstu + 0.3 × confidence_recognizera`.
Podobieństwo tekstu = znormalizowany dystans Levenshteina (czysty Kotlin, bez
zależności zewnętrznych). Feedback AI wywoływany tylko gdy wynik < 85 (próg
`FEEDBACK_THRESHOLD`) — oszczędność kosztów, bo dobre wymowy nie potrzebują
komentarza.

## Personalizacja

Poziom CEFR (`UserPreferences.englishLevel`) i cele nauki (`learningGoals`) z
onboardingu wchodzą do każdego system promptu tutora. Model AI jest konfigurowalny
(`UserPreferences.aiModel`, domyślnie `gpt-4o-mini`) — ustawienia → AI.

## Koszty i degradacja

Orientacyjnie: jedna wymiana w rozmowie to system prompt (~300–500 tokenów z
wspomnieniami) + historia (do 30 wiadomości) + odpowiedź (≤200 tokenów, limit
`REPLY_MAX_TOKENS`). Analiza/generowanie ćwiczeń/ekstrakcja pamięci to pojedyncze
wywołania JSON-mode po zakończeniu rozmowy, nie per-wiadomość. Gdy AI jest
niedostępne: rozmowa zwraca `AppError` (UI pokazuje baner z akcją ponowienia),
ale historia, słownictwo, ćwiczenia z SRS, statystyki i notatki głosowe działają
w pełni offline (Room jest źródłem prawdy — patrz `docs/database.md`).
