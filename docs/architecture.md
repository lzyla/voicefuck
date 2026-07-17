# Architektura (jak zaimplementowano)

Ten dokument opisuje architekturę **faktycznie zaimplementowanego** kodu. Uzasadnienie
decyzji i szerszy kontekst projektowy — patrz `docs/09-architektura-techniczna.md`.

## Warstwy

```
presentation (feature/*)   Compose UI + ViewModel (StateFlow<UiState>)
        │  wywołuje
domain (core/domain)       modele, interfejsy repo/serwisów, use case'y — czysty Kotlin
        │  implementowane przez
data (core/data)           repozytoria: mapują Entity/DTO ↔ Domain
        │  używa
database / datastore / network / audio / ai   szczegóły infrastruktury
```

Reguła zależności: `core/domain` nie zależy od żadnego innego modułu (moduł
`aienglishcoach.jvm.library` — czysty JVM, bez Androida). Wszystkie interfejsy
repozytoriów (`ConversationRepository`, `VocabularyRepository`, ...) i serwisów
(`AiTutorService`, `SpeechToTextService`, `TextToSpeechService`, `AudioRecorderService`,
`MemoryRetrievalService`, `BackgroundScheduler`) są zdefiniowane w `core/domain/.../repository`
i `.../service`. Implementacje żyją w `core/data`, `core/audio`, `core/ai` i `app`
(`WorkManagerScheduler`) i są spięte przez moduły Hilt (`@Binds`).

## MVVM

Każdy ekran ma `@HiltViewModel` z:
- niemutowalnym `data class *UiState` wystawianym jako `StateFlow`,
- metodami publicznymi jako jedynym sposobem na zmianę stanu (unidirectional data flow),
- `viewModelScope.launch` do wywołań `suspend`.

Przykład (`feature/conversation/ConversationViewModel.kt`) — maszyna stanów rozmowy:

```
ScenarioSelection → (start) → Speaking(greeting) → Idle
Idle --(tap)--> Listening --(final STT)--> Processing --(AI reply)--> Speaking --> Idle
```

Dotknięcie mikrofonu podczas `Speaking` przerywa TTS i przechodzi w `Listening`
(`onMicTapped()` w `ConversationViewModel`).

## Use Case'y

Konwencja: klasa z `operator fun invoke(...)`, jedna odpowiedzialność, wstrzykiwana
przez konstruktor (`@Inject`). Przykłady w `core/domain/.../usecase/`:

- `conversation/`: `StartConversationUseCase`, `SendMessageUseCase`,
  `EndConversationUseCase`, `AnalyzeConversationUseCase`, `TranslateMessageUseCase`.
- `exercise/`: `GenerateExercisesUseCase`, `GetDueExercisesUseCase`,
  `SubmitExerciseAnswerUseCase` (ocenia odpowiedź, aktualizuje SM-2, rozwiązuje błąd
  źródłowy po 2 poprawnych powtórzeniach).
- `vocabulary/`: `SaveVocabularyItemUseCase`, `GetDueVocabularyUseCase`,
  `ReviewVocabularyItemUseCase`.
- `pronunciation/`: `EvaluatePronunciationUseCase` (podobieństwo Levenshteina +
  confidence rozpoznawania mowy + opcjonalny feedback AI).
- `memory/ConsolidateMemoryUseCase`, `statistics/ObserveStatisticsUseCase`,
  `home/ObserveHomeSummaryUseCase`, `settings/*`.

`AnalyzeConversationUseCase` i `SendMessageUseCase` pokazują wzorzec offline-first:
wiadomość użytkownika i utworzenie rozmowy zapisywane są w Room **przed** wywołaniem
AI, więc awaria sieci nigdy nie gubi danych wejściowych użytkownika.

## Dependency Injection (Hilt)

Moduły `@Module @InstallIn(SingletonComponent::class)` per warstwa:
`core/database/di/DatabaseModule`, `core/datastore/di/DataStoreModule`,
`core/network/di/NetworkModule`, `core/data/di/DataModule` (wiąże repozytoria +
`ApiKeyProvider` dla OkHttp), `core/audio/di/AudioModule`, `core/ai/di/AiModule`,
`app/di/AppModule` (wiąże `BackgroundScheduler` → `WorkManagerScheduler`).

## Offline-first

Room (`CoachDatabase`) jest źródłem prawdy. Wyniki AI (analiza, ćwiczenia, pamięć)
są zapisywane lokalnie zanim UI je pokaże. `core/network/OpenAiDataSource` mapuje
błędy HTTP na domenowy `AppError` (401/403→`InvalidApiKey`, 429→`RateLimited` z
retry+backoff, 5xx→`AiService`, `IOException`→`Network`) — ViewModel nigdy nie widzi
wyjątków transportowych.

## Moduły Gradle i konwencje

`build-logic/convention` definiuje 7 pluginów konwencji (patrz
`docs/11-struktura-projektu.md` dla pełnego uzasadnienia):

| Plugin | Zastosowanie |
|---|---|
| `aienglishcoach.jvm.library` | `core/common`, `core/domain`, `core/testing` (bez Androida) |
| `aienglishcoach.android.library` | `core/database`, `core/datastore`, `core/network`, `core/data`, `core/audio` |
| `aienglishcoach.android.library.compose` | `core/designsystem` |
| `aienglishcoach.android.feature` | wszystkie `feature/*` (Compose + Hilt + domain/designsystem/common) |
| `aienglishcoach.android.hilt` | dodaje Hilt+KSP do dowolnego modułu Android |
| `aienglishcoach.android.room` | Room + KSP + eksport schematu (`core/database`) |
| `aienglishcoach.android.application` | `app` (jedyny moduł z `com.android.application`) |

Ważna decyzja: `AndroidRoomConventionPlugin` eksponuje `room-runtime`/`room-ktx`
jako `api` (nie `implementation`), bo `CoachDatabase` (publiczne API `core/database`)
dziedziczy po `RoomDatabase` — moduły zależne muszą widzieć ten typ na classpath.

## Nawigacja

`app/navigation/CoachNavHost.kt` — jeden `NavHost`, dolna nawigacja z 4 zakładkami
(Home/Rozmowy/Nauka/Statystyki, zgodnie z `docs/00-brief-decyzje-projektowe.md`),
Ustawienia dostępne z górnego paska Home. Wszystkie trasy w obiekcie `Routes`.
