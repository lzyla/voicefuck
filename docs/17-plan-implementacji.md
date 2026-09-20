# ETAP 17 — Plan implementacji (sprinty S0–S6)

> Rozwinięcie planu sprintów z `docs/00-brief-decyzje-projektowe.md` (sekcja 11).
> Nazwy modułów Gradle, ekranów, tabel i komponentów — kanoniczne z briefu.
>
> **Zakres tego repozytorium: S0–S5.** Sprint **S6** (synchronizacja chmurowa,
> Whisper, OpenAI TTS, rozszerzony RAG) to **roadmapa** — opisany dla kompletności,
> nie wchodzi w zakres implementacji.

Założenia: sprinty ~2-tygodniowe; każdy sprint kończy się **działającą,
instalowalną wersją aplikacji** (zasada "always shippable"); trunk-based
development z krótkimi feature branchami i CI na każdym PR.

---

## 1. Kolejność implementacji modułów (wynikająca z zależności)

Zależności modułów (z briefu): conversation → ai, memory, audio; exercises → ai,
revision; vocabulary → revision; pronunciation → audio, ai; statistics ←
wszystkie; memory → ai. Stąd porządek topologiczny budowy:

```
1. build-logic/convention          (niczego nie wymaga — fundament buildów)
2. core/common                     (Result, dispatchery, Timber)
3. core/designsystem               (Theme, Colors, Typography, komponenty)
4. core/testing                    (utils testowe — równolegle z 2–3)
5. core/domain                     (modele + interfejsy repo + use case'y)
6. core/database  ┐
   core/datastore ├─ (równolegle; wymagają tylko domain/common)
   core/network   ┘
7. core/data                       (implementacje repo — spina 6 z domain)
8. core/audio                      (AudioRecorder, SpeechRecognizerManager,
                                    TextToSpeechManager)
9. core/ai                         (prompty, ConversationEngine, analiza,
                                    generatory, embeddings — wymaga network/domain)
10. app                            (nawigacja, scaffold — szkielet już w S0)
11. feature/onboarding, feature/settings          (S1 — wymagają datastore)
12. feature/home                                  (S1 szkielet, pełny w S2–S5)
13. feature/conversation                          (S2 — wymaga audio+ai+data)
14. feature/practice                              (S3–S5: exercises, vocabulary,
                                                   pronunciation, voicenotes)
15. feature/statistics                            (S5 — konsumuje wszystkie dane)
```

Reguły: `feature/*` zależą wyłącznie od `core/*` (nigdy od siebie nawzajem);
`app` zależy od wszystkich `feature/*` i skleja nawigację; komunikacja między
feature'ami wyłącznie przez nawigację i wspólne repozytoria w `core/data`.

---

## 2. Kamienie milowe

| Kamień | Po sprincie | Kryterium |
|---|---|---|
| **M0 "Szkielet"** | S0 | apka buduje się w CI, uruchamia, nawigacja między pustymi ekranami, theme light/dark |
| **M1 "Skonfigurowany użytkownik"** | S1 | pełny onboarding → home; ustawienia trwałe w DataStore |
| **M2 "Pierwsza rozmowa"** (serce produktu) | S2 | działająca rozmowa głosowa STT→AI→TTS zapisana w Room |
| **M3 "Pętla nauki zamknięta"** | S3 | rozmowa → analiza → błędy + słówka + podsumowanie |
| **M4 "SRS działa"** | S4 | ćwiczenia z błędów + fiszki z harmonogramem SM-2 |
| **M5 "MVP feature-complete"** | S5 | statystyki, wymowa, notatki, workery, raporty — kandydat do publikacji |
| **M6 "Cloud" (roadmapa)** | S6 | poza zakresem repo |

---

## 3. Sprinty

### Sprint S0 — Fundamenty

**Cel:** kompilujący się, wielomodułowy projekt z design systemem, nawigacją
i CI — baza pod wszystkie kolejne sprinty.

**Zakres (zadania → moduły):**

1. `build-logic/convention` — convention plugins (application, library, compose,
   hilt), version catalog (`libs.versions.toml`) ze stosem kanonicznym
   (Kotlin 2.0.21, AGP 8.7.3, Compose BOM 2024.12.01, Hilt 2.53.1 itd.),
   Java toolchain 17, minSdk 26 / targetSdk 35.
2. `core/common` — `Result`, dispatchery (`@Dispatcher(IO)`…), inicjalizacja Timber.
3. `core/designsystem` — `CoachTheme` (pełne role M3 light+dark wg etapu 7,
   dynamic color za flagą), typografia Inter, tokeny spacing/radius; komponenty:
   `CoachPrimaryButton`, `CoachCard`, `CoachTopBar`, `MicButton` (4 stany,
   animacja fali), `MessageBubble`, `StatTile`, `ProgressRing`, `LevelChip`,
   `EmptyState`, `LoadingIndicator`, `ErrorBanner` + preview `@PreviewLightDark`.
4. `core/domain` — modele domenowe (Conversation, Message, UserError,
   VocabularyItem, Exercise, PronunciationResult, VoiceNote, DailyStats,
   AiMemory, UserPreferences) + interfejsy repozytoriów (bez implementacji).
5. `core/testing` — reguły testowe, fabryki danych testowych.
6. `app` — `MainActivity`, `App` (Hilt), `NavHost` ze WSZYSTKIMI route'ami
   z briefu jako placeholdery, scaffold z dolną nawigacją (Home / Rozmowy /
   Nauka / Statystyki), splash z warunkiem onboarding/home (na sztywno → home).
7. CI (GitHub Actions): build + lint + testy jednostkowe + spotless/ktlint na PR.

**Definition of Done:** `./gradlew build` zielony lokalnie i w CI; wszystkie
komponenty design systemu mają preview w light+dark; nawigacja przechodzi przez
wszystkie 22 route'y; brak modułu z zależnością łamiącą reguły z sekcji 1.

**Ryzyka:** niekompatybilność wersji AGP/Kotlin/Compose (mitygacja: version
catalog przetestowany na starcie, wersje kanoniczne z briefu); przeinwestowanie
w convention plugins (mitygacja: kopiowanie sprawdzonych wzorców z Now in Android).

**Testy:** unit — brak logiki biznesowej, więc minimalne; screenshot/preview
komponentów designsystem; smoke test nawigacji (Compose UI test w `app`).

**Działająca wersja po S0:** aplikacja instaluje się i uruchamia; użytkownik
przełącza 4 zakładki dolnej nawigacji i widzi placeholdery ekranów w pełnym
motywie marki (light/dark), z działającym `MicButton` demo na placeholderze.

---

### Sprint S1 — Onboarding + Ustawienia + DataStore

**Cel:** użytkownik konfiguruje profil (poziom, cele, uprawnienia, klucz API),
a konfiguracja jest trwała i edytowalna.

**Zakres (zadania → moduły):**

1. `core/datastore` — `UserPreferences` (onboardingCompleted, englishLevel,
   dailyGoalMinutes, learningGoals, ttsVoice/speed, aiModel, theme) +
   szyfrowany zapis klucza API (security-crypto).
2. `core/data` — `UserPreferencesRepository` (impl na DataStore).
3. `core/network` — szkielet klienta OpenAI (Retrofit/OkHttp, autoryzacja
   Bearer, DTO `chat/completions`) + endpoint testu klucza.
4. `feature/onboarding` — WelcomeScreen, LevelSelectionScreen, GoalsScreen,
   PermissionsScreen (mikrofon + powiadomienia), ApiSetupScreen (walidacja
   klucza przez realne wywołanie); flow krokowy z progresem.
5. `feature/settings` — SettingsScreen, SettingsAiScreen, SettingsProfileScreen,
   SettingsDataScreen (na razie bez eksportu — sekcje danych placeholderem);
   wybór motywu Light/Dark/System + dynamic color.
6. `app` — SplashScreen z realną decyzją: `onboardingCompleted` → home | onboarding.

**Definition of Done:** świeża instalacja przechodzi pełny onboarding; po
restarcie aplikacja pamięta wszystko (DataStore); klucz API zapisany szyfrowanie
i weryfikowany online; zmiana motywu działa natychmiast; ViewModel-e pokryte
testami (Turbine + MockK).

**Ryzyka:** odmowa uprawnienia mikrofonu blokuje produkt (mitygacja: ekran
wyjaśniający + ponowna prośba + deep link do ustawień systemowych); koszt/wygoda
klucza użytkownika (mitygacja: instrukcja "jak zdobyć klucz", jasny komunikat
o backend proxy w przyszłości — wymóg briefu).

**Testy:** unit — repozytorium preferencji, walidator klucza, ViewModel-e
onboardingu; UI — Compose test flow onboardingu (happy path + brak uprawnienia).

**Działająca wersja po S1:** użytkownik instaluje apkę, przechodzi onboarding
(poziom, cele, uprawnienia, klucz), ląduje na Home z powitaniem i swoim celem
dziennym, może wszystko zmienić w Ustawieniach — konfiguracja przeżywa restart.

---

### Sprint S2 — Rozmowa głosowa + historia

**Cel:** serce produktu — pełna pętla głosowa STT → OpenAI → TTS z zapisem do
Room i przeglądaniem historii.

**Zakres (zadania → moduły):**

1. `core/database` — Room: `conversations`, `messages` (+ DAO, relacje,
   konwertery); baza `CoachDatabase`.
2. `core/audio` — `AudioRecorder`, `SpeechRecognizerManager` (Android
   `SpeechRecognizer`, partial results, confidence), `TextToSpeechManager`
   (US/GB, tempo z preferencji).
3. `core/ai` — `ConversationEngine`: system prompt (poziom ucznia, scenariusz,
   cele z DataStore), okno ostatnich N wiadomości (pamięć krótkoterminowa),
   wywołanie `chat/completions` (gpt-4o-mini), obsługa błędów 401/429/timeout.
4. `core/data` — `ConversationRepository` (Room jako źródło prawdy; każda
   wiadomość zapisana przed pokazaniem — wymóg briefu offline-first).
5. `feature/conversation` — ConversationScreen (maszyna stanów MicButtona
   idle→listening→processing→speaking, transkrypcja live, streaming odpowiedzi,
   fallback klawiaturowy, barge-in), ConversationListScreen (historia + wybór
   scenariusza), zalążek ConversationSummaryScreen (metryki bez analizy AI).
6. `feature/home` — hero CTA "Zacznij rozmowę" + karta ostatniej rozmowy (realne dane).

**Definition of Done:** rozmowa 5+ tur działa E2E na urządzeniu fizycznym;
latencja głos→głos < 4 s (cel z briefu) mierzona logami; rozmowa i transkrypcja
w całości w Room, historia dostępna offline; utrata sieci w trakcie rozmowy →
`ErrorBanner` z ponowieniem, bez utraty stanu; stany MicButton zgodne z design
systemem (etap 7).

**Ryzyka:** *największe w projekcie* — jakość/latencja `SpeechRecognizer` na
różnych urządzeniach (mitygacja: partial results, test na 3+ fizycznych
urządzeniach, fallback klawiatura); koszty/limity OpenAI (mitygacja: limit okna
kontekstu, retry z backoff dla 429); kolizje audio TTS↔STT (mitygacja: maszyna
stanów wyklucza równoczesny nasłuch i mowę, audio focus).

**Testy:** unit — `ConversationEngine` (budowa promptu, okno kontekstu, mapowanie
błędów; MockK na API), repozytorium (Room in-memory), ViewModel maszyny stanów
(Turbine); manualny protokół latencji na urządzeniach; UI test listy historii.

**Działająca wersja po S2:** użytkownik wybiera scenariusz, prowadzi realną
rozmowę głosową z tutorem AI (słyszy odpowiedzi, widzi transkrypcję), a po
zakończeniu znajduje rozmowę w historii — także offline.

---

### Sprint S3 — Analiza po rozmowie

**Cel:** zamknięcie pierwszej pętli nauki — z każdej rozmowy powstają błędy,
słownictwo i feedback.

**Zakres (zadania → moduły):**

1. `core/database` — tabele `user_errors`, `vocabulary_items`, `ai_memories` (+ DAO).
2. `core/ai` — `ConversationAnalyzer`: structured output (JSON) z listą błędów
   (kategorie GRAMMAR/VOCABULARY/PRONUNCIATION/FLUENCY: original, corrected,
   explanation), nowym słownictwem i podsumowaniem; `MemoryManager` v1
   (zapis faktów/celów/słabości do `ai_memories`, na razie bez embeddingów —
   wstrzykiwanie top-K po `importance`).
3. `core/network` — endpoint embeddings (`text-embedding-3-small`) — DTO
   przygotowane (użycie pełne w S5/S6).
4. `core/data` — `UserErrorRepository`, `VocabularyRepository`,
   `AiMemoryRepository`; wyniki analizy zapisywane lokalnie przed prezentacją.
5. `feature/conversation` — pełny ConversationSummaryScreen: feedback, karty
   błędów per kategoria, chipy nowych słówek (auto-dodane do vocabulary),
   CTA "Wygeneruj ćwiczenia" (na razie disabled z opisem — aktywne w S4).
6. `feature/home` — karta ostatniej rozmowy pokazuje liczbę błędów/słówek.
7. `app`/WorkManager — szkielet `MemoryConsolidationWorker` (enqueue po rozmowie).

**Definition of Done:** po zakończeniu rozmowy analiza generuje się w tle
(wskaźnik postępu), a ekran podsumowania pokazuje błędy/słówka/feedback;
niepoprawny JSON z LLM nie crashuje — retry + stan błędu z ponowieniem; wszystkie
dane analizy trwałe w Room; słówka z rozmowy widoczne w bazie (podgląd w S4 UI).

**Ryzyka:** niestabilność structured output LLM (mitygacja: response_format
JSON, walidacja schematu, 1 retry z poprawką promptu, degradacja do "samo
podsumowanie"); koszt drugiego wywołania LLM po każdej rozmowie (mitygacja:
jedna zbiorcza analiza zamiast wielu wywołań).

**Testy:** unit — parser/walidator JSON analizy (złośliwe przypadki), mapowanie
DTO→domena→encje, `MemoryManager` (selekcja top-K); integracyjne — DAO nowych
tabel; UI test ekranu podsumowania ze stanami loading/success/error.

**Działająca wersja po S3:** po każdej rozmowie użytkownik dostaje konkretną
wartość edukacyjną: listę swoich błędów z poprawkami i wyjaśnieniami, nowe
słówka i feedback tutora; AI w kolejnej rozmowie zna jego słabości.

---

### Sprint S4 — Ćwiczenia + SRS + fiszki

**Cel:** pełny moduł utrwalania: ćwiczenia generowane z błędów i fiszki
słownictwa, oba na harmonogramie SM-2.

**Zakres (zadania → moduły):**

1. `core/database` — tabele `exercises`, `exercise_attempts` (+ DAO); migracje.
2. `core/domain` — algorytm **SM-2** (`easeFactor` start 2.5, oceny 0–5,
   intervalDays 1→6→EF·n) jako czysty use case (`ScheduleRevisionUseCase`) —
   wspólny dla słówek i ćwiczeń (moduł revision).
3. `core/ai` — `ExerciseGenerator`: z `user_errors` generuje ćwiczenia typów
   FILL_GAP / MULTIPLE_CHOICE / TRANSLATION / SPEAKING (structured output).
4. `core/data` — `ExerciseRepository`, rozszerzenie `VocabularyRepository`
   o pola SRS (dueAt, easeFactor, repetitionCount).
5. `feature/practice` — PracticeHubScreen (kafle + licznik due), ExercisesScreen
   (filtr due/wszystkie), ExercisePlayerScreen (4 typy ćwiczeń, feedback,
   wynik serii, aktualizacja SRS), VocabularyScreen (lista, filtry statusów,
   wyszukiwarka, szczegóły w bottom sheet), VocabularyReviewScreen (fiszki
   z obrotem, oceny SM-2, podsumowanie sesji).
6. `feature/conversation` — aktywacja CTA "Wygeneruj ćwiczenia" w summary.
7. `feature/home` — kafle "Do powtórki dziś" (słówka + ćwiczenia due) z realnymi danymi.

**Definition of Done:** błąd z rozmowy → wygenerowane ćwiczenie → wykonanie →
poprawny nowy `dueAt` wg SM-2 (zweryfikowane testami tabelarycznymi); fiszki
przechodzą pełną sesję due z ocenami; statusy słówek NEW→LEARNING→MASTERED
zmieniają się wg progów powtórek; wszystko działa offline (poza generowaniem
nowych ćwiczeń).

**Ryzyka:** jakość generowanych ćwiczeń (mitygacja: few-shot przykłady
w promptach, walidacja: poprawna odpowiedź ∈ opcje, dystraktory ≠ odpowiedź);
błędna implementacja SM-2 psuje harmonogram długoterminowo (mitygacja: testy
tabelaryczne na sekwencjach ocen, property-based sanity: interval rośnie przy
ocenach ≥ 3).

**Testy:** unit — **SM-2 wyczerpująco** (sekwencje ocen, granice EF 1.3, reset
po ocenie < 3), walidator ćwiczeń, ViewModel-e playera i fiszek; integracyjne —
przepływ error→exercise→attempt→dueAt na Room in-memory; UI — sesja ćwiczeń
happy path.

**Działająca wersja po S4:** użytkownik rozmawia, a następnego dnia wraca i ma
na Home realne powtórki: rozwiązuje ćwiczenia zbudowane z własnych błędów
i powtarza fiszki własnych słówek — system planuje kolejne powtórki sam.

---

### Sprint S5 — Statystyki, wymowa, notatki, WorkManager, raporty

**Cel:** domknięcie MVP (kamień M5): widoczny postęp, trening wymowy, notatki
głosowe, automatyzacja w tle i eksport danych.

**Zakres (zadania → moduły):**

1. `core/database` — tabele `pronunciation_results`, `voice_notes`, `daily_stats`
   (+ DAO, agregacje); migracje.
2. `core/data` — `StatisticsRepository` (agregacja daily_stats ze wszystkich
   modułów), `PronunciationRepository`, `VoiceNoteRepository`; eksport JSON
   (settings/data — MVP synchronizacji wg briefu).
3. `core/ai` — feedback wymowy (LLM: expectedText vs recognizedText + score
   z confidence STT), generator raportu tygodniowego ze statystyk; embeddings
   dla `ai_memories` + podobieństwo kosinusowe w `MemoryManager` (pełny RAG
   lokalny przed rozmową).
4. `core/audio` — nagrywanie/odtwarzanie notatek (pliki lokalne), transkrypcja
   notatek przez `SpeechRecognizer`; kasowanie audio rozmów po transkrypcji
   (wymóg prywatności z briefu).
5. `feature/practice` — PronunciationScreen (kolejka słów z niskim confidence,
   próba, score, feedback), VoiceNotesScreen (nagrywanie, lista, odtwarzanie,
   transkrypcja, usuwanie).
6. `feature/statistics` — StatisticsScreen: StatTile (streak, minuty, słówka,
   skuteczność), wykresy (minuty/dzień, przyrost słówek), raport tygodniowy AI.
7. WorkManager — `RevisionSchedulerWorker` (codzienny: wyznacza due + notyfikacja),
   pełny `MemoryConsolidationWorker` (po rozmowie: konsolidacja + embeddingi).
8. `feature/home` — streak + `ProgressRing` celu dziennego na danych `daily_stats`.
9. `feature/settings` — SettingsDataScreen: realny eksport JSON + kasowanie danych.
10. Hardening release: R8, brak PII w logach release, przegląd dostępności
    (TalkBack, kontrast, touch targets 48dp), pomiar cold start < 2 s.

**Definition of Done:** wszystkie 22 ekrany briefu w pełni funkcjonalne;
statystyki zgadzają się z danymi źródłowymi (test agregacji); workery odpalają
się wg harmonogramu i po Dozie; notyfikacja o powtórkach prowadzi deep linkiem
do PracticeHub; eksport JSON importowalny (walidacja schematu); audyt a11y
i wydajności zaliczony; release build podpisany przechodzi pełny smoke test.

**Ryzyka:** wiarygodność score wymowy z confidence STT (mitygacja: komunikować
jako "wskazówkę", nie ocenę; feedback LLM opisowy); WorkManager vs producenci
(Doze/OEM killery — mitygacja: `setRequiresBatteryNotLow(false)`, test na
urządzeniach Xiaomi/Samsung); rozrost zakresu sprintu (mitygacja: wymowa
i notatki mają zdefiniowane minimum, wykresy prostymi composables zamiast
biblioteki).

**Testy:** unit — agregacje statystyk, similarity embeddingów, logika streak,
scoring wymowy; integracyjne — workery (WorkManager TestDriver), migracje Room
(MigrationTestHelper), eksport/parsowanie JSON; UI — statystyki i notatki;
manualne — checklista a11y + macierz urządzeń.

**Działająca wersja po S5 (= MVP, kamień M5):** kompletna pętla briefu —
użytkownik rozmawia, dostaje analizę, ćwiczy i powtarza wg SRS, trenuje wymowę,
nagrywa notatki, widzi streak/wykresy/raport tygodniowy, dostaje codzienne
przypomnienia i może wyeksportować wszystkie dane. Aplikacja gotowa do
zamkniętych testów (internal testing w Play).

---

### Sprint S6 — Roadmapa (POZA ZAKRESEM TEGO REPOZYTORIUM)

**Cel (przyszły):** wyjście poza MVP — konta i chmura oraz wyższa jakość audio/AI.

**Zakres (kierunkowy):**

1. **Synchronizacja chmurowa** (`sync`) — backend + konta, synchronizacja Room
   ↔ chmura (strategia konfliktów last-write-wins per encja), backend **proxy
   dla klucza OpenAI** (wymóg produkcyjny z briefu — klucz znika z urządzenia),
   Play In-App Updates.
2. **Whisper API** jako opcjonalne STT (wyższa jakość, koszt/latencja —
   przełącznik w SettingsAiScreen).
3. **OpenAI TTS** jako opcjonalny głos premium (naturalność vs koszt).
4. **Rozszerzony RAG** — indeks wektorowy dla pełnej historii rozmów i notatek,
   lepsza konsolidacja pamięci (deduplikacja, wygaszanie ważności), pamięć
   między urządzeniami po synchronizacji.

**Warunki wejścia:** stabilne MVP (S5) w produkcji, decyzja o modelu kosztowym
(subskrypcja vs własny klucz), projekt backendu (osobny dokument architektury).

**Ryzyka (do przyszłej analizy):** koszty infrastruktury i API, prywatność
danych ucznia w chmurze (RODO), migracja użytkowników z kluczy własnych na proxy.

---

## 4. Strategia testów per sprint — podsumowanie

| Sprint | Unit | Integracyjne | UI (Compose) | Manualne |
|---|---|---|---|---|
| S0 | minimalne (brak logiki) | — | smoke nawigacji | preview light/dark |
| S1 | ViewModel-e onboardingu/ustawień, repo preferencji | DataStore + szyfrowanie klucza | flow onboardingu | uprawnienia na 2 wersjach Androida |
| S2 | ConversationEngine, maszyna stanów, mapery | Room (conversations/messages) | lista historii | latencja E2E na 3 urządzeniach |
| S3 | parser analizy JSON, MemoryManager | DAO errors/vocabulary/memories | ekran summary (3 stany) | jakość analizy na próbkach rozmów |
| S4 | **SM-2 (tabelarycznie)**, walidator ćwiczeń, ViewModel-e | error→exercise→attempt E2E na Room | sesja ćwiczeń | jakość generowanych ćwiczeń |
| S5 | agregacje, streak, similarity, scoring | workery (TestDriver), **migracje Room**, eksport JSON | statystyki, notatki | a11y (TalkBack), cold start, macierz urządzeń |

Zasady stałe: pokrycie `core/domain` i `core/ai` priorytetowe (logika bez
Androida); `core/testing` dostarcza fabryki i reguły; testy migracji Room
obowiązkowe od pierwszej migracji (S3+); CI blokuje merge przy czerwonych testach.

---

## 5. Podsumowanie przyrostu wartości

| Po sprincie | Użytkownik może… |
|---|---|
| S0 | uruchomić apkę i obejrzeć nawigację + design system (wersja wewnętrzna) |
| S1 | skonfigurować profil, klucz i motyw — trwale |
| S2 | **odbyć prawdziwą rozmowę głosową z tutorem AI** i wrócić do historii |
| S3 | zobaczyć swoje błędy, słówka i feedback po każdej rozmowie |
| S4 | ćwiczyć na własnych błędach i powtarzać słówka wg SRS |
| S5 | śledzić postępy, trenować wymowę, nagrywać notatki, dostawać przypomnienia i raporty — **pełne MVP** |
| S6 | (roadmapa) synchronizować dane w chmurze i korzystać z Whisper/OpenAI TTS |
