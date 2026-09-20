# ETAP 12 — Struktura plików

> **To jest specyfikacja struktury plików** — kompletna lista plików Kotlin per moduł
> z jednolinijkową odpowiedzialnością. **Implementacja logiki nastąpi w etapie 18.**
> Nazwy zgodne z briefem ([00-brief-decyzje-projektowe.md](00-brief-decyzje-projektowe.md));
> pakiet bazowy `com.aienglishcoach`. Konwencja: `*Screen.kt` (composable ekranu),
> `*ViewModel.kt` (ViewModel + `*UiState` + ewentualne `*Effect` w tym samym pliku).

## 1. app — `com.aienglishcoach`

```
app/src/main/kotlin/com/aienglishcoach/
├── App.kt                              # @HiltAndroidApp; init Timber, WorkManager (HiltWorkerFactory), harmonogram RevisionSchedulerWorker
├── MainActivity.kt                     # @AndroidEntryPoint; edge-to-edge, setContent { CoachTheme { CoachApp() } }
├── MainViewModel.kt                    # stan startowy (onboarding czy home) + motyw z UserPreferences; MainUiState
├── ui/
│   ├── CoachApp.kt                     # scaffold aplikacji: Snackbar, bottom bar, hosting NavHost
│   └── CoachBottomBar.kt               # dolna nawigacja: Home / Rozmowy / Nauka / Statystyki
└── navigation/
    ├── CoachNavHost.kt                 # NavHost łączący grafy wszystkich feature'ów (trasy z briefu, sekcja 3)
    └── TopLevelDestination.kt          # enum zakładek bottom bar (route, ikona, etykieta)

app/src/test/kotlin/com/aienglishcoach/
└── MainViewModelTest.kt                # test decyzji startowej i motywu (Turbine)

app/src/androidTest/kotlin/com/aienglishcoach/
└── NavigationTest.kt                   # test przejść między zakładkami (Compose UI test)
```

## 2. core/common — `com.aienglishcoach.core.common`

```
core/common/src/main/kotlin/com/aienglishcoach/core/common/
├── result/
│   ├── Result.kt                       # sealed Result<T>: Success/Failure + operatory (map, onSuccess, onFailure)
│   └── CoachError.kt                   # typowane błędy: Network, Api, Auth, RateLimit, Speech, Unknown
├── dispatcher/
│   ├── CoachDispatchers.kt             # enum IO/Default + kwalifikator @Dispatcher
│   └── DispatchersModule.kt            # Hilt: @Provides dispatchery coroutines
└── util/
    ├── TimeUtils.kt                    # formatowanie czasu trwania (mm:ss), dat względnych
    └── StringExt.kt                    # drobne rozszerzenia String (normalizacja słów, capitalizacja)

core/common/src/test/kotlin/com/aienglishcoach/core/common/
├── ResultTest.kt                       # testy operatorów Result
└── TimeUtilsTest.kt                    # testy formatowania czasu
```

## 3. core/designsystem — `com.aienglishcoach.core.designsystem`

```
core/designsystem/src/main/kotlin/com/aienglishcoach/core/designsystem/
├── theme/
│   ├── Theme.kt                        # CoachTheme: light/dark/dynamic color, złożenie ColorScheme+Typography+Shapes
│   ├── Color.kt                        # palety z briefu (primary #4F46E5, secondary #14B8A6, ...) + role M3 light/dark
│   ├── Typography.kt                   # skala M3 na foncie Inter (display/headline/title/body/label)
│   ├── Shape.kt                        # corner radius: 12 karty, 16 arkusze, pełny FAB/przyciski głosowe
│   └── Spacing.kt                      # tokeny spacingu 4/8/12/16/24/32 (siatka 4dp)
├── icon/
│   └── CoachIcons.kt                   # aliasy ikon Material Symbols Rounded używanych w aplikacji
└── component/
    ├── CoachPrimaryButton.kt           # główny przycisk akcji (pełny radius, stany enabled/loading)
    ├── CoachCard.kt                    # karta bazowa (radius 12, elewacja, padding tokenowy)
    ├── CoachTopBar.kt                  # górny pasek z tytułem i akcjami (np. ikona ustawień na Home)
    ├── MicButton.kt                    # przycisk mikrofonu; stany idle/listening/processing/speaking + animacja fali
    ├── MessageBubble.kt                # dymek wiadomości (USER/ASSISTANT, tłumaczenie, akcja odsłuchu)
    ├── StatTile.kt                     # kafelek pojedynczej metryki (wartość, etykieta, trend)
    ├── ProgressRing.kt                 # pierścień postępu (cel dzienny, opanowanie)
    ├── LevelChip.kt                    # chip poziomu językowego (A2–C1)
    ├── EmptyState.kt                   # stan pusty listy (ilustracja, opis, CTA)
    ├── LoadingIndicator.kt             # wskaźnik ładowania spójny z motywem
    └── ErrorBanner.kt                  # baner błędu z akcją „ponów"

core/designsystem/src/androidTest/kotlin/com/aienglishcoach/core/designsystem/
└── MicButtonTest.kt                    # test stanów i semantyki MicButton (Compose UI test)
```

## 4. core/domain — `com.aienglishcoach.core.domain` (czysty Kotlin)

```
core/domain/src/main/kotlin/com/aienglishcoach/core/domain/
├── model/
│   ├── Conversation.kt                 # rozmowa: id, title, scenario, startedAt/endedAt, durationSec, status, summary
│   ├── Message.kt                      # wiadomość: role (enum MessageRole USER/ASSISTANT), content, translation?, audioPath?
│   ├── UserError.kt                    # błąd ucznia: kategoria (ErrorCategory), original, corrected, explanation, resolvedAt?
│   ├── VocabularyItem.kt               # słówko: word, translation, definition, example, status, pola SRS (easeFactor, intervalDays, repetitionCount, dueAt)
│   ├── Exercise.kt                     # ćwiczenie: type (ExerciseType), question, options, correctAnswer, explanation, pola SRS
│   ├── ExerciseAttempt.kt              # próba: exerciseId, userAnswer, isCorrect, attemptedAt
│   ├── PronunciationResult.kt          # wynik wymowy: word, expectedText, recognizedText, score 0–100, feedback
│   ├── VoiceNote.kt                    # notatka głosowa: title, audioPath, transcription?, durationSec
│   ├── DailyStats.kt                   # statystyki dnia: conversationSec, messagesSent, wordsLearned, exercisesDone/Correct, pronunciationAvg
│   ├── AiMemory.kt                     # wspomnienie AI: kind (MemoryKind), content, embedding?, importance
│   ├── UserPreferences.kt              # preferencje: onboardingCompleted, englishLevel, dailyGoalMinutes, learningGoals, ttsVoice/speed, aiModel, theme
│   ├── ConversationAnalysis.kt         # wynik analizy rozmowy: błędy + nowe słówka + podsumowanie + feedback
│   ├── RevisionItem.kt                 # pozycja powtórki due (słówko lub ćwiczenie) + typ
│   ├── StatisticsOverview.kt           # agregat dla ekranu statystyk: streak, serie dzienne, skuteczność
│   └── enums/
│       ├── MessageRole.kt              # USER / ASSISTANT
│       ├── ErrorCategory.kt            # GRAMMAR / VOCABULARY / PRONUNCIATION / FLUENCY
│       ├── ExerciseType.kt             # FILL_GAP / MULTIPLE_CHOICE / TRANSLATION / SPEAKING
│       ├── VocabularyStatus.kt         # NEW / LEARNING / MASTERED
│       ├── MemoryKind.kt               # FACT / PREFERENCE / WEAKNESS / GOAL / PROGRESS
│       ├── ConversationStatus.kt       # ACTIVE / ENDED / ANALYZED
│       └── EnglishLevel.kt             # A2 / B1 / B2 / C1
├── repository/
│   ├── ConversationRepository.kt       # kontrakt: obserwacja rozmów/wiadomości, start, wysłanie wiadomości, zakończenie
│   ├── UserErrorRepository.kt          # kontrakt: zapis/odczyt błędów ucznia, oznaczanie resolved
│   ├── VocabularyRepository.kt         # kontrakt: słówka, filtry statusu, aktualizacja SRS, pozycje due
│   ├── ExerciseRepository.kt           # kontrakt: ćwiczenia, próby, pozycje due, aktualizacja SRS
│   ├── PronunciationRepository.kt      # kontrakt: zapis/odczyt wyników wymowy
│   ├── VoiceNoteRepository.kt          # kontrakt: notatki głosowe CRUD
│   ├── StatisticsRepository.kt         # kontrakt: daily_stats — inkrementy i agregaty, streak
│   ├── AiMemoryRepository.kt           # kontrakt: wspomnienia AI + wyszukiwanie podobieństwa (RAG)
│   ├── UserPreferencesRepository.kt    # kontrakt: preferencje (Flow), zapis, klucz API (szyfrowany)
│   └── AiRepository.kt                 # kontrakt operacji AI: odpowiedź tutora, analiza, generacja ćwiczeń, embeddingi, feedback wymowy, konsolidacja
└── usecase/
    ├── conversation/
    │   ├── StartConversationUseCase.kt         # tworzy rozmowę (scenariusz) i zwraca id; pierwsza kwestia tutora
    │   ├── SendMessageUseCase.kt               # zapisuje wiadomość USER, uzyskuje i zapisuje odpowiedź ASSISTANT (zapis przed emisją)
    │   ├── EndConversationUseCase.kt           # zamyka rozmowę (endedAt, durationSec), kolejkuje analizę i konsolidację
    │   ├── ObserveConversationUseCase.kt       # Flow rozmowy + wiadomości dla ConversationScreen
    │   ├── GetConversationsUseCase.kt          # Flow historii rozmów dla ConversationListScreen
    │   └── AnalyzeConversationUseCase.kt       # analiza po rozmowie: błędy→user_errors, słówka→vocabulary_items, summary→conversations
    ├── exercise/
    │   ├── GenerateExercisesUseCase.kt         # generuje ćwiczenia z nierozwiązanych błędów (ExerciseGenerator) i zapisuje
    │   ├── GetDueExercisesUseCase.kt           # Flow ćwiczeń due (dueAt <= teraz) dla ExercisesScreen
    │   └── SubmitExerciseAnswerUseCase.kt      # ocenia odpowiedź, zapisuje próbę, przelicza SRS (SM-2), aktualizuje daily_stats
    ├── revision/
    │   ├── GetDueRevisionsUseCase.kt           # scala due słówka + ćwiczenia w listę RevisionItem (Home, worker)
    │   └── ScheduleDailyRevisionsUseCase.kt    # logika dzienna workera: wyznacza due, decyduje o powiadomieniu
    ├── vocabulary/
    │   ├── SaveVocabularyItemUseCase.kt        # zapisuje słówko (ręczne lub z analizy) z inicjalizacją SRS
    │   ├── GetVocabularyUseCase.kt             # Flow słówek z filtrami statusu dla VocabularyScreen
    │   └── ReviewVocabularyItemUseCase.kt      # ocena fiszki 0–5 → przeliczenie SM-2, zmiana statusu, dueAt
    ├── pronunciation/
    │   ├── EvaluatePronunciationUseCase.kt     # porównuje expected/recognized + confidence → score i feedback; zapisuje wynik
    │   └── GetPronunciationHistoryUseCase.kt   # Flow historii wyników wymowy (trend słowa)
    ├── voicenote/
    │   ├── SaveVoiceNoteUseCase.kt             # zapisuje notatkę (audio + transkrypcja + czas trwania)
    │   ├── GetVoiceNotesUseCase.kt             # Flow listy notatek głosowych
    │   └── DeleteVoiceNoteUseCase.kt           # usuwa notatkę wraz z plikiem audio
    ├── statistics/
    │   ├── GetStatisticsUseCase.kt             # buduje StatisticsOverview (streak, wykresy, skuteczność) dla StatisticsScreen
    │   ├── GetHomeDashboardUseCase.kt          # agregat dla Home: streak, postęp celu dziennego, liczba due
    │   └── GetWeeklyReportUseCase.kt           # tygodniowe podsumowanie postępów generowane przez LLM ze statystyk
    ├── memory/
    │   ├── ConsolidateMemoryUseCase.kt         # po rozmowie: LLM wyciąga fakty/cele/słabości → embedding → ai_memories
    │   └── RetrieveMemoriesUseCase.kt          # top-K wspomnień (podobieństwo kosinusowe) do wstrzyknięcia w prompt
    ├── preferences/
    │   ├── GetUserPreferencesUseCase.kt        # Flow UserPreferences (motyw, poziom, cele, TTS, model)
    │   ├── UpdateUserPreferencesUseCase.kt     # zapis zmian preferencji (poziom, cele, głos, model, motyw)
    │   ├── ValidateApiKeyUseCase.kt            # testowe wywołanie API → walidacja i szyfrowany zapis klucza
    │   └── CompleteOnboardingUseCase.kt        # zapis wyników onboardingu i flagi onboardingCompleted
    └── data/
        ├── ExportDataUseCase.kt                # eksport wszystkich danych lokalnych do pliku JSON (SettingsDataScreen)
        └── ClearAllDataUseCase.kt              # kasuje bazę, DataStore i pliki audio (z potwierdzeniem w UI)

core/domain/src/test/kotlin/com/aienglishcoach/core/domain/usecase/
├── SendMessageUseCaseTest.kt           # kolejność zapisów i ścieżka błędu sieci (MockK)
├── AnalyzeConversationUseCaseTest.kt   # mapowanie analizy na błędy/słówka/summary
├── SubmitExerciseAnswerUseCaseTest.kt  # poprawność przeliczeń SRS i zapisu próby
├── ReviewVocabularyItemUseCaseTest.kt  # przejścia statusów NEW→LEARNING→MASTERED
├── GetDueRevisionsUseCaseTest.kt       # scalanie i sortowanie pozycji due
└── EvaluatePronunciationUseCaseTest.kt # wyliczanie score z confidence + feedback
```

## 5. core/database — `com.aienglishcoach.core.database`

```
core/database/src/main/kotlin/com/aienglishcoach/core/database/
├── CoachDatabase.kt                    # @Database: 10 encji, wersja, TypeConverters, exportSchema
├── entity/
│   ├── ConversationEntity.kt           # tabela conversations (kolumny wg briefu, sekcja 6)
│   ├── MessageEntity.kt                # tabela messages; FK→conversations, indeks conversationId
│   ├── UserErrorEntity.kt              # tabela user_errors; FK→conversations (nullable)
│   ├── VocabularyItemEntity.kt         # tabela vocabulary_items; pola SRS + dueAt (indeks)
│   ├── ExerciseEntity.kt               # tabela exercises; options jako JSON, sourceErrorId FK
│   ├── ExerciseAttemptEntity.kt        # tabela exercise_attempts; FK→exercises
│   ├── PronunciationResultEntity.kt    # tabela pronunciation_results
│   ├── VoiceNoteEntity.kt              # tabela voice_notes
│   ├── DailyStatsEntity.kt             # tabela daily_stats; date jako PK
│   └── AiMemoryEntity.kt               # tabela ai_memories; embedding jako BLOB (nullable)
├── dao/
│   ├── ConversationDao.kt              # insert/update, observe listy i pojedynczej rozmowy z wiadomościami (@Transaction)
│   ├── MessageDao.kt                   # insert, observeByConversation, okno ostatnich N wiadomości
│   ├── UserErrorDao.kt                 # insert(list), observe, błędy nierozwiązane, markResolved
│   ├── VocabularyDao.kt                # upsert, observe z filtrem statusu, due (dueAt <= :now), aktualizacja pól SRS
│   ├── ExerciseDao.kt                  # insert(list), observe, due, aktualizacja pól SRS
│   ├── ExerciseAttemptDao.kt           # insert próby, historia prób ćwiczenia, skuteczność
│   ├── PronunciationResultDao.kt       # insert, observe historii, średnia score per słowo
│   ├── VoiceNoteDao.kt                 # insert/delete, observe listy
│   ├── DailyStatsDao.kt                # upsert dnia, inkrementy pól, zakres dat (wykresy), dane do streaka
│   └── AiMemoryDao.kt                  # insert/update, wszystkie wspomnienia (do rankingu podobieństwa), usuwanie
├── converter/
│   ├── InstantConverter.kt             # Instant <-> Long (epoch millis)
│   ├── LocalDateConverter.kt           # LocalDate <-> String ISO (PK daily_stats)
│   ├── StringListConverter.kt          # List<String> <-> JSON (options ćwiczeń, learningGoals)
│   └── EmbeddingConverter.kt           # FloatArray <-> ByteArray BLOB (embeddingi ai_memories)
└── di/
    └── DatabaseModule.kt               # Hilt: @Provides CoachDatabase + wszystkie DAO

core/database/src/androidTest/kotlin/com/aienglishcoach/core/database/
├── CoachDatabaseTest.kt                # tworzenie bazy, integralność FK, konwertery end-to-end
├── ConversationDaoTest.kt              # relacja rozmowa—wiadomości, @Transaction
├── VocabularyDaoTest.kt                # zapytania due i filtrów statusu
└── DailyStatsDaoTest.kt                # upsert/inkrementy i zakresy dat
```

## 6. core/datastore — `com.aienglishcoach.core.datastore`

```
core/datastore/src/main/kotlin/com/aienglishcoach/core/datastore/
├── UserPreferencesDataSource.kt        # DataStore Preferences: klucze i Flow (onboarding, poziom, cele, TTS, model, motyw)
├── EncryptedApiKeyStorage.kt           # zapis/odczyt klucza OpenAI (security-crypto + Android Keystore)
└── di/
    └── DataStoreModule.kt              # Hilt: @Provides DataStore<Preferences> + EncryptedApiKeyStorage

core/datastore/src/test/kotlin/com/aienglishcoach/core/datastore/
└── UserPreferencesDataSourceTest.kt    # odczyt/zapis kluczy i wartości domyślnych (testowy DataStore)
```

## 7. core/network — `com.aienglishcoach.core.network`

```
core/network/src/main/kotlin/com/aienglishcoach/core/network/
├── api/
│   └── OpenAiApi.kt                    # Retrofit: POST /v1/chat/completions, POST /v1/embeddings (suspend)
├── dto/
│   ├── ChatCompletionRequest.kt        # żądanie chat: model (gpt-4o-mini), messages, temperature, response_format
│   ├── ChatCompletionResponse.kt       # odpowiedź chat: choices, message, usage
│   ├── ChatMessageDto.kt               # element messages: role + content
│   ├── EmbeddingsRequest.kt            # żądanie embeddings: model (text-embedding-3-small), input
│   ├── EmbeddingsResponse.kt           # odpowiedź embeddings: data[].embedding (List<Float>)
│   └── OpenAiErrorResponse.kt          # struktura błędu API (message, type, code) do mapowania na CoachError
├── interceptor/
│   └── AuthInterceptor.kt              # dokleja nagłówek Authorization: Bearer <klucz z EncryptedApiKeyStorage>
└── di/
    └── NetworkModule.kt                # Hilt: Json (ignoreUnknownKeys), OkHttp (interceptory, timeouty), Retrofit, OpenAiApi

core/network/src/test/kotlin/com/aienglishcoach/core/network/
├── OpenAiApiTest.kt                    # serializacja żądań/odpowiedzi na MockWebServer
└── AuthInterceptorTest.kt              # obecność i format nagłówka autoryzacji
```

## 8. core/data — `com.aienglishcoach.core.data`

```
core/data/src/main/kotlin/com/aienglishcoach/core/data/
├── repository/
│   ├── ConversationRepositoryImpl.kt   # rozmowy/wiadomości: DAO + ConversationEngine; zapis USER i ASSISTANT przed emisją
│   ├── UserErrorRepositoryImpl.kt      # błędy ucznia: zapis wyników analizy, odczyt, markResolved
│   ├── VocabularyRepositoryImpl.kt     # słówka: DAO + aktualizacje SRS z RevisionEngine
│   ├── ExerciseRepositoryImpl.kt       # ćwiczenia i próby: DAO + zapis wygenerowanych ćwiczeń
│   ├── PronunciationRepositoryImpl.kt  # wyniki wymowy: zapis i historia
│   ├── VoiceNoteRepositoryImpl.kt      # notatki: DAO + zarządzanie plikami audio na dysku
│   ├── StatisticsRepositoryImpl.kt     # daily_stats: inkrementy, agregaty, wyliczanie streaka
│   ├── AiMemoryRepositoryImpl.kt       # wspomnienia: DAO + ranking podobieństwa kosinusowego (RAG)
│   ├── UserPreferencesRepositoryImpl.kt# preferencje: UserPreferencesDataSource + EncryptedApiKeyStorage
│   └── AiRepositoryImpl.kt             # operacje AI: delegacja do silników core/ai, mapowanie błędów na Result
├── mapper/
│   ├── ConversationMapper.kt           # ConversationEntity/MessageEntity <-> Conversation/Message
│   ├── UserErrorMapper.kt              # UserErrorEntity <-> UserError
│   ├── VocabularyMapper.kt             # VocabularyItemEntity <-> VocabularyItem
│   ├── ExerciseMapper.kt               # ExerciseEntity/ExerciseAttemptEntity <-> Exercise/ExerciseAttempt
│   ├── PronunciationMapper.kt          # PronunciationResultEntity <-> PronunciationResult
│   ├── VoiceNoteMapper.kt              # VoiceNoteEntity <-> VoiceNote
│   ├── StatsMapper.kt                  # DailyStatsEntity <-> DailyStats
│   ├── AiMemoryMapper.kt               # AiMemoryEntity <-> AiMemory (BLOB <-> FloatArray)
│   └── PreferencesMapper.kt            # Preferences (DataStore) <-> UserPreferences
├── worker/
│   ├── RevisionSchedulerWorker.kt      # @HiltWorker, codzienny: GetDueRevisions/ScheduleDailyRevisions → powiadomienie o powtórkach
│   ├── MemoryConsolidationWorker.kt    # @HiltWorker, po rozmowie: AnalyzeConversation + ConsolidateMemory (constraint: sieć, backoff)
│   └── WorkScheduler.kt                # rejestracja periodic/unique work (nazwy, constraints, polityki)
└── di/
    └── DataModule.kt                   # Hilt: @Binds wszystkie interfejsy repo → implementacje

core/data/src/test/kotlin/com/aienglishcoach/core/data/
├── repository/
│   ├── ConversationRepositoryImplTest.kt  # kolejność: zapis lokalny → AI → zapis → emisja; błędy sieci
│   ├── AiMemoryRepositoryImplTest.kt      # poprawność rankingu podobieństwa kosinusowego
│   └── StatisticsRepositoryImplTest.kt    # inkrementy dnia i wyliczanie streaka
└── mapper/
    └── MappersTest.kt                     # symetria mapowań Entity <-> Domain
```

## 9. core/audio — `com.aienglishcoach.core.audio`

```
core/audio/src/main/kotlin/com/aienglishcoach/core/audio/
├── AudioRecorder.kt                    # nagrywanie audio do pliku (notatki głosowe): start/stop, amplituda, czas
├── SpeechRecognizerManager.kt          # cykl życia SpeechRecognizer: start/stopListening, partial results, confidence, stany jako Flow
├── TextToSpeechManager.kt              # cykl życia TextToSpeech: init, głos US/GB, tempo, speak/stop, zdarzenia zakończenia jako Flow
├── model/
│   ├── SpeechRecognitionState.kt       # sealed: Idle / Listening(partial) / Result(text, confidence) / Error
│   └── TtsState.kt                     # sealed: Idle / Speaking(utteranceId) / Done / Error
└── di/
    └── AudioModule.kt                  # Hilt: @Provides menedżery audio (@Singleton, context aplikacji)

core/audio/src/test/kotlin/com/aienglishcoach/core/audio/
└── SpeechRecognitionStateTest.kt       # przejścia maszyny stanów rozpoznawania
```

## 10. core/ai — `com.aienglishcoach.core.ai`

```
core/ai/src/main/kotlin/com/aienglishcoach/core/ai/
├── ConversationEngine.kt               # orkiestracja odpowiedzi tutora: pamięć (RAG) + prompt + okno N wiadomości → chat completions
├── PromptBuilder.kt                    # składanie system promptów: persona tutora, poziom ucznia, scenariusz, wspomnienia, format JSON analiz
├── ErrorAnalyzer.kt                    # analiza transkryptu po rozmowie → lista błędów (kategoria, oryginał, poprawka, wyjaśnienie) + słówka + summary
├── ExerciseGenerator.kt                # generuje ćwiczenia (FILL_GAP/MULTIPLE_CHOICE/TRANSLATION/SPEAKING) z błędów ucznia (structured JSON)
├── MemoryManager.kt                    # pamięć długoterminowa: konsolidacja po rozmowie (LLM→JSON), pobieranie top-K przez podobieństwo kosinusowe
├── EmbeddingsClient.kt                 # wywołania /v1/embeddings (text-embedding-3-small) + cosineSimilarity(FloatArray, FloatArray)
├── PronunciationAnalyzer.kt            # score 0–100 z porównania expected/recognized + confidence STT; feedback AI dla niskich wyników
├── RevisionEngine.kt                   # logika powtórek: scalanie due słówek/ćwiczeń, priorytety, limity dzienne
├── Sm2Scheduler.kt                     # czysty algorytm SM-2: (easeFactor start 2.5, ocena 0–5, intervalDays 1→6→EF*n) → nowe pola SRS
├── StatisticsEngine.kt                 # wyliczenia statystyk: streak, agregaty tygodniowe, dane wykresów, prompt raportu tygodniowego
├── model/
│   ├── AnalysisResult.kt               # @Serializable struktura structured output analizy rozmowy (błędy, słówka, summary)
│   ├── GeneratedExercise.kt            # @Serializable struktura structured output wygenerowanego ćwiczenia
│   ├── MemoryExtraction.kt             # @Serializable struktura structured output konsolidacji pamięci (kind, content, importance)
│   └── Sm2Result.kt                    # wynik przeliczenia SM-2: easeFactor, intervalDays, repetitionCount, dueAt
└── di/
    └── AiModule.kt                     # Hilt: @Provides silniki AI (@Singleton)

core/ai/src/test/kotlin/com/aienglishcoach/core/ai/
├── Sm2SchedulerTest.kt                 # tablica przypadków SM-2 (oceny 0–5, progresja 1→6→EF*n, spadki EF)
├── PromptBuilderTest.kt                # zawartość promptów: poziom, scenariusz, wstrzyknięte wspomnienia
├── EmbeddingsClientTest.kt             # poprawność podobieństwa kosinusowego (wektory znane)
├── ErrorAnalyzerTest.kt                # parsowanie structured output na AnalysisResult (odporność na braki pól)
├── ExerciseGeneratorTest.kt            # mapowanie GeneratedExercise na Exercise (walidacja typów)
├── PronunciationAnalyzerTest.kt        # wyliczanie score dla przypadków brzegowych
└── StatisticsEngineTest.kt             # streak i agregaty na danych syntetycznych
```

## 11. core/testing — `com.aienglishcoach.core.testing`

```
core/testing/src/main/kotlin/com/aienglishcoach/core/testing/
├── rule/
│   └── MainDispatcherRule.kt           # reguła JUnit4 podmieniająca Dispatchers.Main na testowy
├── data/
│   └── TestData.kt                     # fabryki modeli domenowych (rozmowy, słówka, ćwiczenia, wspomnienia)
└── repository/
    ├── FakeConversationRepository.kt   # fake in-memory do testów ViewModeli feature/conversation
    ├── FakeVocabularyRepository.kt     # fake in-memory do testów fiszek i list słówek
    ├── FakeExerciseRepository.kt       # fake in-memory do testów sesji ćwiczeń
    └── FakeUserPreferencesRepository.kt# fake preferencji do testów onboardingu/ustawień
```

## 12. feature/onboarding — `com.aienglishcoach.feature.onboarding`

```
feature/onboarding/src/main/kotlin/com/aienglishcoach/feature/onboarding/
├── navigation/
│   └── OnboardingNavigation.kt         # trasy: splash, onboarding/welcome|level|goals|permissions|api + graf
├── splash/
│   ├── SplashScreen.kt                 # ekran startowy; kieruje do onboardingu lub home wg stanu preferencji
│   └── SplashViewModel.kt              # SplashUiState: Loading / GoOnboarding / GoHome (GetUserPreferencesUseCase)
├── welcome/
│   └── WelcomeScreen.kt                # statyczne wprowadzenie do produktu (bez ViewModelu)
├── level/
│   ├── LevelSelectionScreen.kt         # wybór poziomu A2–C1 (LevelChip, samoocena)
│   └── LevelSelectionViewModel.kt      # LevelSelectionUiState: wybrany poziom, zapis wyboru
├── goals/
│   ├── GoalsScreen.kt                  # cele nauki (multi-select) + cel dzienny w minutach (slider)
│   └── GoalsViewModel.kt               # GoalsUiState: cele, minuty; walidacja i zapis
├── permissions/
│   ├── PermissionsScreen.kt            # prośba o mikrofon i powiadomienia z wyjaśnieniem
│   └── PermissionsViewModel.kt         # PermissionsUiState: statusy uprawnień, obsługa odmowy
└── api/
    ├── ApiSetupScreen.kt               # wprowadzenie klucza OpenAI, test połączenia, informacja o bezpieczeństwie
    └── ApiSetupViewModel.kt            # ApiSetupUiState: klucz, walidacja (ValidateApiKeyUseCase), CompleteOnboarding

feature/onboarding/src/test/kotlin/com/aienglishcoach/feature/onboarding/
├── SplashViewModelTest.kt              # decyzja onboarding/home wg flagi onboardingCompleted
└── ApiSetupViewModelTest.kt            # ścieżki walidacji klucza: sukces, błąd auth, brak sieci
```

## 13. feature/home — `com.aienglishcoach.feature.home`

```
feature/home/src/main/kotlin/com/aienglishcoach/feature/home/
├── navigation/
│   └── HomeNavigation.kt               # trasa home + wpięcie w graf
├── HomeScreen.kt                       # streak, ProgressRing celu dziennego, szybki start rozmowy, karta powtórek due
└── HomeViewModel.kt                    # HomeUiState (GetHomeDashboardUseCase, GetDueRevisionsUseCase); efekt: nawigacje

feature/home/src/test/kotlin/com/aienglishcoach/feature/home/
└── HomeViewModelTest.kt                # budowa dashboardu i reakcja na zmiany danych (Turbine)
```

## 14. feature/conversation — `com.aienglishcoach.feature.conversation`

```
feature/conversation/src/main/kotlin/com/aienglishcoach/feature/conversation/
├── navigation/
│   └── ConversationNavigation.kt       # trasy: conversations, conversation/{id}, conversation/{id}/summary
├── list/
│   ├── ConversationListScreen.kt       # historia rozmów (tytuł, scenariusz, czas trwania, data) + start nowej
│   └── ConversationListViewModel.kt    # ConversationListUiState (GetConversationsUseCase, StartConversationUseCase)
├── active/
│   ├── ConversationScreen.kt           # aktywna rozmowa: MessageBubble lista, MicButton, partial transcript, pasek stanu
│   ├── ConversationViewModel.kt        # ConversationUiState + ConversationEffect; orkiestracja STT→SendMessage→TTS
│   └── ScenarioPickerSheet.kt          # arkusz wyboru scenariusza rozmowy przed startem
└── summary/
    ├── ConversationSummaryScreen.kt    # analiza po rozmowie: podsumowanie, błędy wg kategorii, nowe słówka, CTA do ćwiczeń
    └── ConversationSummaryViewModel.kt # SummaryUiState (AnalyzeConversationUseCase / obserwacja wyników analizy)

feature/conversation/src/test/kotlin/com/aienglishcoach/feature/conversation/
├── ConversationViewModelTest.kt        # cykl mic: idle→listening→processing→speaking; błąd sieci nie gubi wiadomości
└── ConversationSummaryViewModelTest.kt # stany Loading/Success/Error analizy
```

## 15. feature/practice — `com.aienglishcoach.feature.practice`

```
feature/practice/src/main/kotlin/com/aienglishcoach/feature/practice/
├── navigation/
│   └── PracticeNavigation.kt           # trasy: practice, practice/exercises[/session], practice/vocabulary[/review], practice/pronunciation, practice/notes
├── hub/
│   ├── PracticeHubScreen.kt            # hub „Nauka": karty ćwiczeń, słownictwa, wymowy, notatek + liczniki due
│   └── PracticeHubViewModel.kt         # PracticeHubUiState: liczniki due per obszar (GetDueRevisionsUseCase)
├── exercises/
│   ├── ExercisesScreen.kt              # lista ćwiczeń due/wszystkie z filtrem typu, start sesji
│   ├── ExercisesViewModel.kt           # ExercisesUiState (GetDueExercisesUseCase, GenerateExercisesUseCase)
│   ├── ExercisePlayerScreen.kt         # wykonywanie serii: pytanie, warianty/odpowiedź, feedback po każdej, pasek postępu
│   └── ExercisePlayerViewModel.kt      # ExercisePlayerUiState (SubmitExerciseAnswerUseCase); wynik sesji
├── vocabulary/
│   ├── VocabularyScreen.kt             # lista słówek + filtry statusu (NEW/LEARNING/MASTERED), dodawanie ręczne
│   ├── VocabularyViewModel.kt          # VocabularyUiState (GetVocabularyUseCase, SaveVocabularyItemUseCase)
│   ├── VocabularyReviewScreen.kt       # fiszki SRS: przód/tył, oceny 0–5, animacja odwrócenia
│   └── VocabularyReviewViewModel.kt    # VocabularyReviewUiState (ReviewVocabularyItemUseCase); kolejka due
├── pronunciation/
│   ├── PronunciationScreen.kt          # trening wymowy: słowo docelowe, odsłuch TTS, nagranie, score i feedback
│   └── PronunciationViewModel.kt       # PronunciationUiState (EvaluatePronunciationUseCase + SpeechRecognizerManager/TTS)
└── voicenotes/
    ├── VoiceNotesScreen.kt             # nagrywanie notatki (MicButton) + lista z odtwarzaniem i transkrypcją
    └── VoiceNotesViewModel.kt          # VoiceNotesUiState (Save/Get/DeleteVoiceNoteUseCase + AudioRecorder)

feature/practice/src/test/kotlin/com/aienglishcoach/feature/practice/
├── ExercisePlayerViewModelTest.kt      # przebieg sesji, zliczanie poprawnych, aktualizacja SRS
├── VocabularyReviewViewModelTest.kt    # kolejka fiszek due i skutki ocen
└── PronunciationViewModelTest.kt       # cykl nagranie→ocena→wynik; błędy STT
```

## 16. feature/statistics — `com.aienglishcoach.feature.statistics`

```
feature/statistics/src/main/kotlin/com/aienglishcoach/feature/statistics/
├── navigation/
│   └── StatisticsNavigation.kt         # trasa statistics + wpięcie w graf
├── StatisticsScreen.kt                 # StatTile'e (streak, minuty, słówka, skuteczność), wykresy tygodniowe, raport LLM
├── StatisticsViewModel.kt              # StatisticsUiState (GetStatisticsUseCase, GetWeeklyReportUseCase)
└── component/
    ├── WeeklyBarChart.kt               # wykres słupkowy minut rozmów / ćwiczeń per dzień (Canvas Compose)
    └── TrendLineChart.kt               # wykres liniowy trendu (np. średnia wymowa) (Canvas Compose)

feature/statistics/src/test/kotlin/com/aienglishcoach/feature/statistics/
└── StatisticsViewModelTest.kt          # budowa UiState z agregatów; raport tygodniowy Loading/Success/Error
```

## 17. feature/settings — `com.aienglishcoach.feature.settings`

```
feature/settings/src/main/kotlin/com/aienglishcoach/feature/settings/
├── navigation/
│   └── SettingsNavigation.kt           # trasy: settings, settings/ai, settings/profile, settings/data
├── SettingsScreen.kt                   # lista sekcji ustawień: AI, profil, dane, motyw, o aplikacji
├── SettingsViewModel.kt                # SettingsUiState (GetUserPreferencesUseCase, UpdateUserPreferencesUseCase — motyw)
├── ai/
│   ├── SettingsAiScreen.kt             # wybór modelu (domyślnie gpt-4o-mini), zmiana klucza API, głos i tempo TTS
│   └── SettingsAiViewModel.kt          # SettingsAiUiState (ValidateApiKeyUseCase, UpdateUserPreferencesUseCase)
├── profile/
│   ├── SettingsProfileScreen.kt        # edycja poziomu (LevelChip) i celów nauki + celu dziennego
│   └── SettingsProfileViewModel.kt     # SettingsProfileUiState (UpdateUserPreferencesUseCase)
└── data/
    ├── SettingsDataScreen.kt           # eksport danych do JSON, kasowanie danych (dialog potwierdzenia)
    └── SettingsDataViewModel.kt        # SettingsDataUiState (ExportDataUseCase, ClearAllDataUseCase); efekty: share/snackbar

feature/settings/src/test/kotlin/com/aienglishcoach/feature/settings/
├── SettingsAiViewModelTest.kt          # walidacja i zapis klucza; zmiana modelu i głosu
└── SettingsDataViewModelTest.kt        # eksport (sukces/błąd IO) i potwierdzenie kasowania
```

## 18. Podsumowanie ilościowe

| Moduł | Pliki main | Pliki testowe |
|---|---|---|
| app | 6 | 2 |
| core/common | 6 | 2 |
| core/designsystem | 17 | 1 |
| core/domain | 55 | 6 |
| core/database | 26 | 4 |
| core/datastore | 3 | 1 |
| core/network | 9 | 2 |
| core/data | 24 | 4 |
| core/audio | 6 | 1 |
| core/ai | 15 | 7 |
| core/testing | 7 | — |
| feature/* (6 modułów) | 44 | 10 |

> Przypomnienie: powyższa lista to **specyfikacja struktury** (nazwy plików, pakiety,
> odpowiedzialności). Treść klas, sygnatury i logika powstają w **etapie 18** zgodnie
> z planem sprintów S0–S5 (brief, sekcja 11).
