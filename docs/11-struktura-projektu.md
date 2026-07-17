# ETAP 11 — Struktura projektu

> Zgodne z briefem ([00-brief-decyzje-projektowe.md](00-brief-decyzje-projektowe.md), sekcja 4)
> i architekturą z etapu 9. Pakiet bazowy: `com.aienglishcoach`.
> Szczegółowa lista plików Kotlin per moduł — etap 12.

## 1. Drzewo katalogów repozytorium

```
ai-english-coach/
├── .github/
│   └── workflows/
│       ├── build.yml                  # CI: build + lint + testy jednostkowe na PR
│       └── release.yml                # budowa podpisanego AAB przy tagu wydania
├── build-logic/                       # logika budowania (wzorzec Now in Android)
│   ├── convention/                    # moduł z pluginami konwencji
│   │   ├── src/main/kotlin/           # AndroidApplication/Library/Compose/Feature/Hilt/Jvm ConventionPlugin
│   │   └── build.gradle.kts           # rejestracja pluginów (gradlePlugin { plugins { ... } })
│   ├── settings.gradle.kts            # podpina version catalog do build-logic
│   └── gradle.properties
├── gradle/
│   ├── libs.versions.toml             # version catalog — JEDYNE źródło wersji bibliotek
│   └── wrapper/                       # Gradle wrapper (gradle-wrapper.properties, jar)
├── app/                               # moduł aplikacji: punkt wejścia, nawigacja, scaffold
│   └── src/main/kotlin/com/aienglishcoach/...
├── core/                              # moduły współdzielone (nie-feature)
│   ├── common/                        # Result, dispatchery, utils, Timber — czysty fundament
│   ├── designsystem/                  # Theme, Colors, Typography, komponenty Coach*
│   ├── domain/                        # modele domenowe, interfejsy repo, use case'y (czysty Kotlin)
│   ├── database/                      # Room: CoachDatabase, encje, DAO, konwertery
│   ├── datastore/                     # DataStore: UserPreferences + szyfrowany klucz API
│   ├── network/                       # Retrofit/OkHttp: OpenAiApi, DTO, interceptory
│   ├── data/                          # implementacje repozytoriów, mapery, workery, DI
│   ├── audio/                         # AudioRecorder, SpeechRecognizerManager, TextToSpeechManager
│   ├── ai/                            # prompty, ConversationEngine, analiza, generatory, embeddings
│   └── testing/                       # wspólne utils testowe (reguły, fabryki danych, fake'i)
├── feature/                           # moduły funkcjonalne (ekrany + ViewModele)
│   ├── onboarding/                    # splash, welcome, poziom, cele, uprawnienia, klucz API
│   ├── home/                          # HomeScreen: streak, cel dzienny, szybki start, due
│   ├── conversation/                  # lista rozmów, aktywna rozmowa, podsumowanie
│   ├── practice/                      # hub: ćwiczenia + słownictwo + wymowa + notatki głosowe
│   ├── statistics/                    # StatisticsScreen: wykresy i wskaźniki
│   └── settings/                      # ustawienia: główne, AI, profil, dane
├── docs/                              # dokumentacja projektowa (etapy 00–17)
├── scripts/                           # skrypty pomocnicze (setup, sprawdzenia CI, release)
│   ├── setup.sh                       # bootstrap środowiska dev (SDK, git hooks)
│   └── check.sh                       # lokalny odpowiednik CI: lint + testy
├── build.gradle.kts                   # root: rejestracja pluginów (alias, apply false)
├── settings.gradle.kts                # includeBuild("build-logic") + include wszystkich modułów
├── gradle.properties                  # JVM args, configuration cache, AndroidX
├── gradlew / gradlew.bat              # wrapper
└── .gitignore
```

Struktura wewnętrzna każdego modułu Androidowego:

```
<moduł>/
├── build.gradle.kts                   # tylko: plugin konwencji + zależności specyficzne
└── src/
    ├── main/kotlin/com/aienglishcoach/<ścieżka.pakietu>/
    ├── main/AndroidManifest.xml       # tam gdzie wymagany (app, audio — uprawnienia)
    ├── test/kotlin/...                # testy jednostkowe (JUnit4, MockK, Turbine)
    └── androidTest/kotlin/...         # testy instrumentowane (Room, Compose UI)
```

## 2. Odpowiedzialność modułów Gradle

### build-logic/convention
Pluginy konwencji: `aienglishcoach.android.application`, `aienglishcoach.android.library`,
`aienglishcoach.android.library.compose`, `aienglishcoach.android.feature`,
`aienglishcoach.hilt`, `aienglishcoach.jvm.library`. Konfigurują compileSdk 35 / minSdk 26,
Java 17 toolchain, Kotlin 2.0.21 + compose compiler plugin, Hilt, testy.
**Nie może zawierać:** żadnego kodu aplikacji ani zasobów — wyłącznie logika budowania.

### app
Punkt wejścia: `App` (`@HiltAndroidApp`, inicjalizacja Timber i WorkManagera),
`MainActivity` (`@AndroidEntryPoint`, edge-to-edge), `CoachNavHost` (wszystkie trasy
z briefu, sekcja 3), scaffold z dolną nawigacją (Home / Rozmowy / Nauka / Statystyki),
`MainViewModel` (decyzja splash: onboarding czy home; motyw).
**Nie może zawierać:** logiki biznesowej, ekranów feature'ów, dostępu do Room/sieci.

### core/common
`Result<T>` i typy błędów (`CoachError`), kwalifikatory dispatcherów + `DispatchersModule`,
drobne utils (formatowanie czasu/dat), inicjalizacja Timber.
**Nie może zawierać:** logiki domenowej, UI, zależności do innych modułów projektu
(jest liściem grafu).

### core/designsystem
`CoachTheme` (Material 3, light/dark/dynamic color), tokeny: `Color.kt`, `Typography.kt`
(Inter), `Shape.kt`, spacing; komponenty kanoniczne: `CoachPrimaryButton`, `CoachCard`,
`CoachTopBar`, `MicButton`, `MessageBubble`, `StatTile`, `ProgressRing`, `LevelChip`,
`EmptyState`, `LoadingIndicator`, `ErrorBanner`; ikony.
**Nie może zawierać:** ViewModeli, use case'ów, modeli domenowych, stringów feature'ów —
komponenty są czysto prezentacyjne (dane wchodzą parametrami).

### core/domain
Modele domenowe (czysty Kotlin), interfejsy repozytoriów, use case'y (konwencja
`operator fun invoke`), enumy domenowe (`ErrorCategory`, `ExerciseType`, `MessageRole`,
`VocabularyStatus`, `MemoryKind`, `EnglishLevel`).
**Nie może zawierać:** ŻADNYCH zależności Androida (`android.*`, `androidx.*`), Room,
Retrofit, Hilt-android (dozwolone `javax.inject.Inject`), serializacji, UI. Budowany
pluginem `aienglishcoach.jvm.library`.

### core/database
`CoachDatabase` (Room 2.6.1), 10 encji kanonicznych (tabele z briefu, sekcja 6),
DAO per encja, `TypeConverters` (Instant/LocalDate, enumy, JSON opcji, BLOB embeddingów),
migracje, `DatabaseModule` (Hilt).
**Nie może zawierać:** modeli domenowych ani maperów do nich (mapery żyją w `core/data`),
logiki biznesowej, wywołań sieciowych. DAO i encje są `internal`-friendly — konsumowane
wyłącznie przez `core/data`.

### core/datastore
`UserPreferencesDataSource` (DataStore Preferences 1.1.1): onboardingCompleted,
englishLevel, dailyGoalMinutes, learningGoals, ttsVoice/speed, aiModel, theme;
`EncryptedApiKeyStorage` (security-crypto + Android Keystore) na klucz OpenAI.
**Nie może zawierać:** danych relacyjnych (to Room), logiki domenowej, UI.

### core/network
`OpenAiApi` (Retrofit 2.11.0): `POST /v1/chat/completions`, `POST /v1/embeddings`;
DTO żądań/odpowiedzi (Kotlinx Serialization 1.7.3); interceptory OkHttp 4.12.0
(autoryzacja Bearer, logowanie tylko debug); `NetworkModule`.
**Nie może zawierać:** logiki promptów (to `core/ai`), zapisu do bazy, modeli domenowych
w sygnaturach publicznych (wyłącznie DTO), klucza API na sztywno.

### core/data
Implementacje wszystkich repozytoriów domenowych (`*RepositoryImpl`), mapery
Entity↔Domain↔DTO, workery WorkManagera (`RevisionSchedulerWorker`,
`MemoryConsolidationWorker`, `@HiltWorker`), `DataModule` (`@Binds` repo).
Realizuje offline-first: zapis wyników AI do Room **przed** emisją.
**Nie może zawierać:** UI/Compose, ViewModeli, definicji encji/DTO (tylko ich użycie),
promptów (deleguje do `core/ai`).

### core/audio
`AudioRecorder` (nagrywanie notatek, pliki audio), `SpeechRecognizerManager`
(STT: partial results, confidence, stany), `TextToSpeechManager` (TTS: głos US/GB,
tempo, kolejka wypowiedzi), `AudioModule`. Manifest z `RECORD_AUDIO`.
**Nie może zawierać:** wywołań AI/sieci, zapisu do bazy, UI (stany eksponowane jako `Flow`).

### core/ai
Silniki AI: `ConversationEngine`, `PromptBuilder`, `ErrorAnalyzer`, `ExerciseGenerator`,
`MemoryManager` + `EmbeddingsClient` (RAG), `PronunciationAnalyzer`, `RevisionEngine` +
`Sm2Scheduler` (SM-2), `StatisticsEngine`, `AiModule`. Konsumuje `OpenAiApi` i interfejsy
z `core/domain`.
**Nie może zawierać:** UI, DAO/encji Room (dostęp do danych wyłącznie przez interfejsy
domenowe), przechowywania klucza API.

### core/testing
Wspólne narzędzia testowe: `MainDispatcherRule`, fabryki danych testowych (`TestData`),
fake'i repozytoriów, utilsy Turbine.
**Nie może zawierać:** kodu produkcyjnego; nigdy nie jest zależnością `implementation`
konfiguracji main (tylko `testImplementation`/`androidTestImplementation`).

### feature/onboarding · home · conversation · practice · statistics · settings
Każdy: composables ekranów, ViewModele z `UiState`, wewnętrzna nawigacja obszaru
(np. graf `practice/*`). `feature/practice` obejmuje cztery pod-obszary briefu:
exercises + vocabulary + pronunciation + voicenotes.
**Nie mogą zawierać:** zależności do innego feature'a, DAO/encji/DTO, klientów HTTP,
logiki domenowej (tylko wywołania use case'ów), własnych definicji theme.

## 3. Tabela zależności: moduł → zależy od

| Moduł | Zależy od (projektowo) | Uwagi |
|---|---|---|
| `build-logic/convention` | — | includeBuild; poza grafem runtime |
| `app` | wszystkie `feature/*`, `core/data`, `core/designsystem`, `core/common`, (`core/datastore` — motyw/onboarding via MainViewModel przez domain) | jedyny moduł znający całość grafu |
| `feature/onboarding` | `core/domain`, `core/designsystem`, `core/common` | + `core/audio` NIE — uprawnienia mikrofonu przez API systemowe |
| `feature/home` | `core/domain`, `core/designsystem`, `core/common` | |
| `feature/conversation` | `core/domain`, `core/designsystem`, `core/common`, `core/audio` | audio: mikrofon + TTS w rozmowie |
| `feature/practice` | `core/domain`, `core/designsystem`, `core/common`, `core/audio` | audio: wymowa + notatki głosowe |
| `feature/statistics` | `core/domain`, `core/designsystem`, `core/common` | |
| `feature/settings` | `core/domain`, `core/designsystem`, `core/common` | test TTS przez use case/domain |
| `core/domain` | `core/common` (**api**) | czysty Kotlin (jvm.library) |
| `core/data` | `core/domain`, `core/database`, `core/datastore`, `core/network`, `core/ai`, `core/common` | wszystkie jako `implementation` |
| `core/database` | `core/common` | Room self-contained |
| `core/datastore` | `core/common` | |
| `core/network` | `core/common` | |
| `core/ai` | `core/domain`, `core/network`, `core/common` | dane przez interfejsy domenowe |
| `core/audio` | `core/common` | |
| `core/designsystem` | — (tylko Compose/M3) | liść UI |
| `core/common` | — | liść |
| `core/testing` | `core/domain`, `core/common` | tylko konfiguracje testowe |

Reguły `api` vs `implementation` — patrz etap 9, sekcja 8.2. Jedyny kanoniczny `api`:
`core/domain` → `core/common` (typ `Result` w sygnaturach publicznych).

## 4. Zasady dodawania nowego feature modułu

1. **Kwalifikacja:** nowy moduł feature powstaje, gdy obszar ma własny zestaw ekranów
   i trasę najwyższego poziomu (jak w briefie, sekcja 2–3). Pojedynczy ekran wewnątrz
   istniejącego obszaru → dodaj go do istniejącego feature'a (wzorzec: `practice`).
2. **Utworzenie:** katalog `feature/<nazwa>` + wpis `include(":feature:<nazwa>")`
   w `settings.gradle.kts`; pakiet `com.aienglishcoach.feature.<nazwa>`.
3. **build.gradle.kts — minimalny:**
   ```kotlin
   plugins {
       alias(libs.plugins.aienglishcoach.android.feature) // dodaje: library+compose+hilt,
   }                                                      // core/domain, core/designsystem, core/common
   dependencies {
       // wyłącznie zależności specyficzne, np. implementation(projects.core.audio)
   }
   ```
4. **Zawartość obowiązkowa:** `navigation/<Nazwa>Navigation.kt` (stałe tras + `NavGraphBuilder.<nazwa>Screen(...)`),
   `<Ekran>Screen.kt`, `<Ekran>ViewModel.kt` z `UiState`; testy ViewModelu.
5. **Podpięcie:** jedyna zmiana poza modułem to `app`: wpis w `CoachNavHost.kt`
   (+ ewentualnie pozycja bottom bar). Żaden inny moduł nie może się zmienić.
6. **Zakazy (egzekwowane w code review + `internal`):** zależność do innego
   `feature/*`; import `core/database`, `core/network`, `core/datastore`, `core/ai`,
   `core/data`; własne kolory/typografia poza design systemem; logika domenowa
   w ViewModelu (należy do use case'ów).
7. **Nawigacja między feature'ami:** wyłącznie przez callbacki nawigacyjne
   (`onNavigateToX: () -> Unit`) wywoływane z `CoachNavHost` — feature nie zna tras
   innych feature'ów.
