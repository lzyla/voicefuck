# AI English Coach — Brief decyzji projektowych (źródło prawdy)

> Ten dokument jest kanonicznym źródłem nazw, decyzji i zakresu dla całej dokumentacji
> (etapy 1–17) oraz implementacji. Wszystkie pozostałe dokumenty MUSZĄ używać nazw
> zdefiniowanych tutaj.

## 1. Produkt

**Nazwa:** AI English Coach
**Platforma:** Android (natywnie, minSdk 26, targetSdk 35)
**Grupa docelowa:** dorośli Polacy (poziomy A2–C1) chcący poprawić **mówiony** angielski,
którzy nie mają czasu/odwagi na konwersacje z lektorem.

**Propozycja wartości:** codzienne, realistyczne rozmowy głosowe z tutorem AI, który
*pamięta ucznia* (cele, błędy, słabości, kontekst życiowy), analizuje każdą rozmowę,
generuje spersonalizowane ćwiczenia i powtórki (SRS), śledzi słownictwo, wymowę i postępy.

**Model działania (pętla nauki):**
rozmowa głosowa → analiza AI (błędy, nowe słownictwo, feedback) → ćwiczenia i powtórki
→ statystyki i pamięć długoterminowa → kolejna, lepiej spersonalizowana rozmowa.

**Offline-first:** historia rozmów, słownictwo, ćwiczenia, statystyki i notatki dostępne
offline (Room = źródło prawdy). Sieć potrzebna tylko do wywołań AI.

## 2. Moduły funkcjonalne (kanoniczne nazwy)

| Moduł | Zakres |
|---|---|
| Rozmowy (conversation) | rozmowa głosowa STT→AI→TTS, scenariusze, historia, transkrypcje |
| Pamięć (memory) | pamięć długoterminowa AI: fakty, cele, słabości; embeddings + RAG |
| Ćwiczenia (exercises) | generowanie ćwiczeń z błędów, wykonywanie, ocena |
| Powtórki (revision) | harmonogram SRS (SM-2) dla słówek i ćwiczeń |
| Słownictwo (vocabulary) | wykryte/nowe słówka, fiszki, status opanowania |
| Wymowa (pronunciation) | analiza wymowy (confidence STT + feedback AI), trening słów |
| Notatki głosowe (voicenotes) | nagrywanie, transkrypcja, lista notatek |
| Statystyki (statistics) | streak, minuty rozmów, słówka, skuteczność, wykresy |
| Ustawienia (settings) | profil, poziom, cele, głos TTS, klucz API, dane |
| Synchronizacja (sync) | eksport/backup + przyszła synchronizacja chmurowa (roadmapa) |
| AI (ai) | klient OpenAI, prompty, analiza, generatory, embeddings |

Zależności: conversation → ai, memory, audio; exercises → ai, revision; vocabulary →
revision; pronunciation → audio, ai; statistics ← wszystkie; memory → ai.

## 3. Ekrany (kanoniczne nazwy tras nawigacji)

Nawigacja dolna (4 zakładki): **Home**, **Rozmowy**, **Nauka**, **Statystyki**.
Ustawienia z ikony na górnym pasku Home.

| Ekran | Route | Opis |
|---|---|---|
| SplashScreen | `splash` | decyzja: onboarding czy home |
| WelcomeScreen | `onboarding/welcome` | wprowadzenie do produktu |
| LevelSelectionScreen | `onboarding/level` | wybór poziomu A2–C1 (samoocena) |
| GoalsScreen | `onboarding/goals` | cele nauki + cel dzienny (minuty) |
| PermissionsScreen | `onboarding/permissions` | mikrofon, powiadomienia |
| ApiSetupScreen | `onboarding/api` | klucz OpenAI (MVP; docelowo backend proxy) |
| HomeScreen | `home` | streak, cel dzienny, szybki start, powtórki due |
| ConversationListScreen | `conversations` | historia rozmów |
| ConversationScreen | `conversation/{id}` | aktywna rozmowa głosowa |
| ConversationSummaryScreen | `conversation/{id}/summary` | analiza po rozmowie |
| PracticeHubScreen | `practice` | hub: ćwiczenia, słownictwo, wymowa, notatki |
| ExercisesScreen | `practice/exercises` | lista ćwiczeń due/wszystkie |
| ExercisePlayerScreen | `practice/exercises/session` | wykonywanie serii ćwiczeń |
| VocabularyScreen | `practice/vocabulary` | lista słówek + filtry |
| VocabularyReviewScreen | `practice/vocabulary/review` | fiszki SRS |
| PronunciationScreen | `practice/pronunciation` | trening wymowy słów |
| VoiceNotesScreen | `practice/notes` | notatki głosowe (nagrywanie + lista) |
| StatisticsScreen | `statistics` | wykresy i wskaźniki |
| SettingsScreen | `settings` | ustawienia główne |
| SettingsAiScreen | `settings/ai` | model, klucz API, głos |
| SettingsProfileScreen | `settings/profile` | poziom, cele |
| SettingsDataScreen | `settings/data` | eksport/kasowanie danych |

## 4. Architektura techniczna

Clean Architecture + MVVM (UiState przez `StateFlow`), Repository Pattern, Use Cases,
Hilt DI, Offline-First, architektura wielomodułowa (Gradle), konwencje z projektu
Now in Android (convention plugins + version catalog).

**Moduły Gradle:**

```
build-logic/convention      – pluginy konwencji (application, library, compose, hilt)
app                         – MainActivity, App, nawigacja, scaffold
core/common                 – Result, dispatchery, utils, Timber
core/designsystem           – Theme, Colors, Typography, komponenty
core/domain                 – modele domenowe, interfejsy repozytoriów, use case'y
core/database               – Room: encje, DAO, baza
core/datastore              – DataStore: UserPreferences (+ szyfrowany klucz API)
core/network                – Retrofit/OkHttp: OpenAI API (DTO)
core/data                   – implementacje repozytoriów
core/audio                  – AudioRecorder, SpeechRecognizerManager, TextToSpeechManager
core/ai                     – prompty, ConversationEngine, analiza, generatory, embeddings
core/testing                – wspólne utils testowe
feature/onboarding, feature/home, feature/conversation, feature/practice
  (exercises+vocabulary+pronunciation+voicenotes), feature/statistics, feature/settings
```

**Pakiet bazowy:** `com.aienglishcoach` (np. `com.aienglishcoach.core.domain`).

**Przepływ danych:** Compose UI → ViewModel (UiState/zdarzenia) → UseCase → Repository
(interfejs w domain) → implementacja w data → Room / DataStore / Network. Room jest
źródłem prawdy; wyniki AI zawsze zapisywane lokalnie przed pokazaniem.

**WorkManager:** `RevisionSchedulerWorker` (codzienny — wyznacza powtórki due,
powiadomienie), `MemoryConsolidationWorker` (po rozmowie — konsolidacja pamięci AI).

## 5. Stos technologiczny (wersje kanoniczne)

Kotlin 2.0.21, AGP 8.7.3, Compose BOM 2024.12.01, Material 3, Hilt 2.53.1,
Room 2.6.1, DataStore 1.1.1, Retrofit 2.11.0, OkHttp 4.12.0,
Kotlinx Serialization 1.7.3, Coroutines 1.9.0, Navigation Compose 2.8.5,
WorkManager 2.10.0, Coil 2.7.0, Timber 5.0.1, JUnit4, MockK 1.13.14,
Turbine 1.2.0, security-crypto (szyfrowanie klucza API).
Java toolchain 17. minSdk 26, targetSdk/compileSdk 35.

## 6. Baza danych (Room, kanoniczne tabele)

| Tabela | Encja | Kluczowe kolumny |
|---|---|---|
| `conversations` | ConversationEntity | id, title, scenario, startedAt, endedAt, durationSec, status, summary |
| `messages` | MessageEntity | id, conversationId FK, role (USER/ASSISTANT), content, translation?, audioPath?, createdAt |
| `user_errors` | UserErrorEntity | id, conversationId FK?, category (GRAMMAR/VOCABULARY/PRONUNCIATION/FLUENCY), original, corrected, explanation, createdAt, resolvedAt? |
| `vocabulary_items` | VocabularyItemEntity | id, word, translation, definition, example, sourceConversationId?, status (NEW/LEARNING/MASTERED), easeFactor, intervalDays, repetitionCount, dueAt, createdAt |
| `exercises` | ExerciseEntity | id, type (FILL_GAP/MULTIPLE_CHOICE/TRANSLATION/SPEAKING), question, options(json), correctAnswer, explanation, sourceErrorId?, easeFactor, intervalDays, repetitionCount, dueAt, createdAt |
| `exercise_attempts` | ExerciseAttemptEntity | id, exerciseId FK, userAnswer, isCorrect, attemptedAt |
| `pronunciation_results` | PronunciationResultEntity | id, word, expectedText, recognizedText, score (0–100), feedback, createdAt |
| `voice_notes` | VoiceNoteEntity | id, title, audioPath, transcription?, durationSec, createdAt |
| `daily_stats` | DailyStatsEntity | date PK, conversationSec, messagesSent, wordsLearned, exercisesDone, exercisesCorrect, pronunciationAvg |
| `ai_memories` | AiMemoryEntity | id, kind (FACT/PREFERENCE/WEAKNESS/GOAL/PROGRESS), content, embedding (BLOB?), importance, createdAt, updatedAt |

Relacje: conversations 1—N messages; conversations 1—N user_errors; user_errors 1—N
exercises (sourceErrorId); exercises 1—N exercise_attempts.
DataStore (`UserPreferences`): onboardingCompleted, englishLevel, dailyGoalMinutes,
learningGoals, ttsVoice/speed, aiModel, theme, apiKey (szyfrowany).

## 7. Warstwy modeli

DTO (network, `*Dto`/`*Request`/`*Response`) → mapery → Domain (czysty Kotlin, `Conversation`,
`Message`, `UserError`, `VocabularyItem`, `Exercise`, `PronunciationResult`, `VoiceNote`,
`DailyStats`, `AiMemory`, `UserPreferences`) ← Encje Room (`*Entity`).
UI models: `*UiState` (immutable data class w ViewModelach).

## 8. Integracje API

- **OpenAI Chat Completions** (`POST /v1/chat/completions`, model domyślny
  `gpt-4o-mini`, konfigurowalny): rozmowa, analiza błędów, generowanie ćwiczeń,
  konsolidacja pamięci, feedback wymowy. Structured output (JSON) dla analiz.
- **OpenAI Embeddings** (`POST /v1/embeddings`, `text-embedding-3-small`): pamięć RAG.
- **STT:** Android `SpeechRecognizer` (on-device/Google, darmowe, niska latencja);
  opcjonalnie Whisper API (roadmapa).
- **TTS:** Android `TextToSpeech` (US/GB, regulowane tempo); opcjonalnie OpenAI TTS (roadmapa).
- **Synchronizacja/aktualizacje:** MVP = lokalny eksport JSON; chmura + Play In-App
  Updates w roadmapie.
- Klucz API: MVP — klucz użytkownika w szyfrowanym storage; produkcyjnie — własny
  backend proxy (udokumentować!).

## 9. AI — pamięć i personalizacja

- **Pamięć krótkoterminowa:** okno ostatnich N wiadomości rozmowy.
- **Pamięć długoterminowa:** `ai_memories` — po każdej rozmowie
  `MemoryConsolidationWorker` wyciąga fakty/cele/słabości (LLM, JSON), zapisuje
  z embeddingiem; przed rozmową `MemoryManager` pobiera top-K wspomnień
  (podobieństwo kosinusowe) i wstrzykuje do system promptu (RAG).
- **Analiza błędów:** po rozmowie LLM zwraca listę błędów (kategoria, oryginał,
  poprawka, wyjaśnienie) → `user_errors` → `ExerciseGenerator` tworzy ćwiczenia.
- **SRS:** algorytm SM-2 (`easeFactor` start 2.5, oceny 0–5, intervalDays 1→6→EF*n).
- **Raporty:** tygodniowe podsumowanie postępów generowane przez LLM ze statystyk.

## 10. Design System (UI)

- **Kolory (light):** primary `#4F46E5` (indygo), secondary `#14B8A6` (teal),
  tertiary `#F59E0B` (bursztyn), error `#DC2626`, background `#FAFAFF`.
  Dark: primary `#A5B4FC`, tła `#121218`/`#1C1C24`. Pełne role Material 3.
- **Typografia:** Inter (display/headline/title/body/label wg skali M3).
- **Siatka:** 4dp; spacing tokens 4/8/12/16/24/32; corner radius 12 (karty), 16 (arkusze), pełny (FAB/przyciski głosowe).
- **Ikony:** Material Symbols Rounded.
- **Motion:** M3 motion (emphasized easing, 200–400 ms), animacja "fali" podczas słuchania.
- **Tryby:** Light + Dark + dynamic color (Android 12+, opcjonalnie).
- Komponenty kanoniczne: `CoachPrimaryButton`, `CoachCard`, `CoachTopBar`,
  `MicButton` (stany: idle/listening/processing/speaking), `MessageBubble`,
  `StatTile`, `ProgressRing`, `LevelChip`, `EmptyState`, `LoadingIndicator`, `ErrorBanner`.

## 11. Plan sprintów (etap 17, skrót)

- **S0** – fundamenty: build-logic, moduły core, design system, nawigacja, CI.
- **S1** – onboarding + ustawienia + DataStore.
- **S2** – rozmowa głosowa (audio, OpenAI, zapis do Room) + historia.
- **S3** – analiza po rozmowie: błędy, słownictwo, podsumowanie.
- **S4** – ćwiczenia + SRS + fiszki słownictwa.
- **S5** – statystyki, wymowa, notatki głosowe, WorkManager, raporty.
- **S6** – synchronizacja chmurowa, Whisper/OpenAI TTS, RAG rozszerzony (roadmapa).

Zakres implementacji w tym repozytorium: S0–S5. S6 = roadmapa.

## 12. Wymagania niefunkcjonalne

Bezpieczeństwo (szyfrowany klucz, brak PII w logach release, HTTPS), wydajność
(cold start < 2 s, latencja odpowiedzi głosowej < 4 s cel), dostępność (TalkBack,
kontrast AA, touch targets 48dp), testowalność (unit: domain/data/ai; UI: Compose),
prywatność (dane lokalnie, audio kasowane po transkrypcji, opt-in na wysyłkę do AI).
