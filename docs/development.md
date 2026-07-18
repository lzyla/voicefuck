# Rozwój projektu

## Zasady ogólne

- Jedna odpowiedzialność na plik/klasę, SOLID/DRY/KISS (patrz `CLAUDE`/instrukcja
  projektu — "Standard kodu").
- `core/domain` pozostaje czystym Kotlinem — żadnego importu `android.*` ani
  bibliotek Androida. Jeśli use case potrzebuje czegoś z platformy, definiuje
  interfejs w `domain/service` lub `domain/repository`, implementację dodajesz
  w warstwie `data`/`audio`/`ai`/`app`.
- Moduły `feature/*` nigdy nie zależą od siebie nawzajem — komunikacja tylko
  przez nawigację (`app/navigation/CoachNavHost.kt`) i wspólne moduły `core/*`.
- Nowe stałe UI (kolory, odstępy, typografia) idą do `core/designsystem`, nie do
  poszczególnych ekranów.

## Dodawanie nowego modułu `feature/*`

1. `feature/<nazwa>/build.gradle.kts`:
   ```kotlin
   plugins { alias(libs.plugins.aienglishcoach.android.feature) }
   android { namespace = "com.aienglishcoach.feature.<nazwa>" }
   ```
2. Dodaj wpis w `settings.gradle.kts` (`include(":feature:<nazwa>")`).
3. Ekran jako `@Composable fun XScreen(...)`, ViewModel jako `@HiltViewModel class XViewModel @Inject constructor(...)`.
4. Stringi UI wyłącznie po polsku w `src/main/res/values/strings.xml`, dostęp
   przez `stringResource(...)` — nigdy hardkodowany tekst w Composable.
5. Zarejestruj trasę w `app/navigation/CoachNavHost.kt` (`Routes` + `composable(...)`).
6. Dodaj zależność w `app/build.gradle.kts` (`implementation(projects.feature.<nazwa>)`).

## Dodawanie nowego use case'a

1. Interfejs repozytorium/serwisu (jeśli potrzebny) w `core/domain/repository` lub
   `core/domain/service`.
2. Klasa use case w `core/domain/usecase/<moduł>/XUseCase.kt` z `operator fun invoke`.
3. Implementacja repozytorium w `core/data/repository/XRepositoryImpl.kt` + `@Binds`
   w `core/data/di/DataModule.kt`.
4. Test jednostkowy w `core/domain/src/test/...` (MockK dla zależności, bez
   Androida — moduł domain jest czystym JVM, testy są szybkie).

## Dodawanie tabeli Room

1. Encja w `core/database/entity/`, DAO w `core/database/dao/` (metody zwracające
   `Flow` dla obserwacji, `suspend fun` dla operacji jednorazowych).
2. Dodaj encję do `@Database(entities = [...])` w `CoachDatabase.kt` — **podnieś
   wersję schematu** i przygotuj `Migration` (na tym etapie projektu wersja 1,
   pierwsza migracja pojawi się przy pierwszej zmianie schematu w produkcji).
3. Mapper `Entity ↔ Domain` w `core/data/mapper/`.
4. `@Provides` DAO w `core/database/di/DatabaseModule.kt`.

## Konwencje testowe

- `core/testing` dostarcza `MainDispatcherRule` (podmienia `Dispatchers.Main` na
  `UnconfinedTestDispatcher`) i `TestDispatcherProvider`.
- ViewModel testy: MockK na use case'ach/serwisach, `@get:Rule val mainDispatcherRule = MainDispatcherRule()`.
- Testy czystej logiki domenowej (SM-2, mapery, use case'y) nie potrzebują
  Robolectric/instrumentacji — moduł `core/domain` jest JVM-only.
- Testy UI Compose (`androidTest`) tylko dla komponentów z realną logiką
  interakcji (np. `MicButtonTest` w `core/designsystem`).

## Styl kodu

- 4 spacje wcięcia, trailing commas w wieloliniowych wywołaniach/konstruktorach
  (spójne z resztą kodu, ułatwia diffy).
- KDoc tylko tam, gdzie wyjaśnia **dlaczego**, nie **co** (np. dlaczego `api` a nie
  `implementation` dla Room w convention pluginie) — zobacz istniejące pliki jako
  wzorzec.
- Błędy domenowe zawsze jako `AppError` (`core/common/result/AppError.kt`), nigdy
  surowe wyjątki przekazywane do UI.

## CI (do skonfigurowania)

Zalecany pipeline: `./gradlew test lint assembleDebug` na każdym PR. Repo nie
zawiera jeszcze workflow GitHub Actions — do dodania w `.github/workflows/`.
