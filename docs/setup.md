# Uruchomienie projektu

## Wymagania

- JDK 17+ (toolchain projektu wymaga 17; działa też pod JDK 21 jako launcher).
- Android SDK: `platforms;android-35`, `build-tools` (dowolna zgodna z AGP 8.7.3).
- Gradle 8.14.3 (wrapper w repo; jeśli `./gradlew` nie może pobrać dystrybucji
  przez brak dostępu do `services.gradle.org`, użyj systemowego `gradle` o tej
  samej wersji — `gradle --version`).

## Konfiguracja lokalna

```bash
echo "sdk.dir=/absolute/path/to/android-sdk" > local.properties
```

`local.properties` jest w `.gitignore` — nigdy nie commituj ścieżek lokalnych ani
kluczy.

## Budowanie

```bash
./gradlew assembleDebug        # APK debug, wszystkie moduły
./gradlew build                 # pełny build + testy + lint
```

## Testy

```bash
./gradlew test                  # testy jednostkowe (JVM) wszystkich modułów
./gradlew testDebugUnitTest      # jw., warianty Android debug
./gradlew connectedAndroidTest   # testy instrumentalne (wymaga emulatora/urządzenia)
```

## Uruchomienie na urządzeniu/emulatorze

```bash
./gradlew installDebug
```

Przy pierwszym uruchomieniu aplikacja poprowadzi przez onboarding (poziom, cele,
uprawnienie mikrofonu, klucz API OpenAI). Klucz API można też wpisać/zmienić
później w **Ustawienia → AI** — jest przechowywany wyłącznie lokalnie w
zaszyfrowanym magazynie (`EncryptedSharedPreferences`, patrz `docs/database.md`).

## Zmienne konfiguracyjne

- Bazowy URL OpenAI: `core/network/build.gradle.kts` →
  `buildConfigField("String", "OPENAI_BASE_URL", ...)`. Do zmiany na własny
  backend proxy w produkcji (patrz `docs/api.md`, sekcja bezpieczeństwa).
- Domyślny model AI: `UserPreferences.DEFAULT_AI_MODEL` (`gpt-4o-mini`),
  zmienialny w Ustawieniach.

## Typowe problemy

- **Brak `local.properties` / `sdk.dir`** → Gradle nie znajdzie SDK; utwórz plik
  jak wyżej.
- **Wrapper nie może pobrać Gradle** (środowisko bez dostępu do
  `services.gradle.org`) → użyj `gradle` zainstalowanego systemowo w tej samej
  wersji (`gradle/wrapper/gradle-wrapper.properties`).
- **Brak `ANDROID_HOME`/pakietów SDK** → zainstaluj `platforms;android-35` przez
  `sdkmanager` z Android Command Line Tools.
