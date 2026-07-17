# ETAP 10 — Wybór technologii

> Wersje kanoniczne pochodzą z briefu: [00-brief-decyzje-projektowe.md](00-brief-decyzje-projektowe.md), sekcja 5.
> Wszystkie wersje są zarządzane centralnie w version catalog (`gradle/libs.versions.toml`).

## 1. Wymagania bazowe

| Wymaganie | Wartość | Uzasadnienie |
|---|---|---|
| Java toolchain | **17** | wymagane przez AGP 8.x; Gradle Java Toolchain gwarantuje identyczną wersję JDK lokalnie i na CI niezależnie od zainstalowanego JDK |
| minSdk | **26** (Android 8.0) | pokrywa ~97% aktywnych urządzeń w PL; pełne API `TextToSpeech`/`SpeechRecognizer`, natywne `java.time` bez desugaringu, kanały powiadomień |
| targetSdk / compileSdk | **35** (Android 15) | zgodność z wymogami Google Play (target API level), dostęp do najnowszych API i zachowań (edge-to-edge domyślnie) |
| Kotlin | **2.0.21** + Compose Compiler Gradle Plugin | od Kotlin 2.0 kompilator Compose jest pluginem Kotlina (`org.jetbrains.kotlin.plugin.compose`) — wersja kompilatora Compose zawsze zgodna z wersją Kotlina, koniec ręcznego `kotlinCompilerExtensionVersion`; kompilator K2 = szybsze buildy |

## 2. Tabela technologii

| Technologia | Wersja | Rola w projekcie | Uzasadnienie (zgodne z rekomendacjami Google) |
|---|---|---|---|
| Kotlin | 2.0.21 | język całego projektu | jedyny język first-class na Androidzie; K2 poprawia czasy kompilacji i inferencję; coroutines/Flow to fundament UDF |
| AGP (Android Gradle Plugin) | 8.7.3 | system budowania | wersja zgodna z compileSdk 35 i Javą 17; wsparcie dla convention plugins i konfiguracji per-module |
| Jetpack Compose (BOM) | 2024.12.01 | cały UI (100% Compose, zero XML) | rekomendowany przez Google toolkit UI; BOM synchronizuje wersje wszystkich artefaktów Compose; deklaratywny UI naturalnie pasuje do `UiState`/UDF |
| Material 3 | z BOM | design system (kolory, typografia, komponenty) | aktualny język projektowania Google; dynamic color (Android 12+), pełne role kolorów wymagane przez brief (sekcja 10) |
| Navigation Compose | 2.8.5 | nawigacja między ekranami (`CoachNavHost`) | oficjalne rozwiązanie nawigacji dla Compose; type-safe routes, deep linki, argumenty (`conversation/{id}`) |
| Hilt | 2.53.1 | dependency injection | rekomendowany przez Google DI dla Androida; integracje z ViewModel (`@HiltViewModel`), Navigation, WorkManager (`@HiltWorker`); walidacja grafu w czasie kompilacji |
| Room | 2.6.1 | lokalna baza (10 tabel kanonicznych), **źródło prawdy** | rekomendowana przez Google warstwa persystencji; weryfikacja SQL w kompilacji, natywne `Flow` (obserwacja zmian → reaktywny UiState), transakcje, migracje |
| DataStore (Preferences) | 1.1.1 | `UserPreferences`: onboarding, poziom, cele, TTS, model AI, motyw | oficjalny następca SharedPreferences; API coroutines/Flow, transakcyjność, brak blokowania UI |
| security-crypto | stabilna | szyfrowanie klucza OpenAI użytkownika (Android Keystore) | klucz API nie może leżeć plaintext; MasterKey w Keystore + szyfrowany storage (wymóg briefu, sekcja 12) |
| Retrofit | 2.11.0 | klient HTTP dla OpenAI API (deklaratywny `OpenAiApi`) | de facto standard; deklaratywne interfejsy, wsparcie suspend functions, konwertery kotlinx-serialization |
| OkHttp | 4.12.0 | warstwa transportowa: interceptory (auth, logowanie), timeouty | fundament Retrofita; interceptor autoryzacji (Bearer z szyfrowanego storage), kontrola timeoutów pod latencję <4 s |
| Kotlinx Serialization | 1.7.3 | JSON: DTO OpenAI, structured output analiz, kolumny json w Room, eksport danych | patrz sekcja 5 |
| Coroutines + Flow | 1.9.0 | asynchroniczność i strumienie we wszystkich warstwach | rekomendowany model współbieżności; `Flow` z Room/DataStore → `StateFlow<UiState>`; structured concurrency w `viewModelScope` |
| WorkManager | 2.10.0 | `RevisionSchedulerWorker` (codzienne powtórki + powiadomienie), `MemoryConsolidationWorker` (konsolidacja pamięci po rozmowie) | rekomendowane API do odraczalnej, gwarantowanej pracy w tle; constraints (sieć), backoff, przeżywa restart urządzenia; integracja z Hilt |
| Coil | 2.7.0 | ładowanie obrazów (awatary scenariuszy, ilustracje) | rekomendowana biblioteka obrazów dla Compose; Kotlin-first, oparta na coroutines, lekka |
| Timber | 5.0.1 | logowanie | standard logowania; drzewa per build type — w release brak logów z PII (wymóg briefu) |
| JUnit4 | 4.x | framework testów jednostkowych | standard ekosystemu Android; wspierany przez AndroidX Test i Compose testing |
| MockK | 1.13.14 | mockowanie w testach (repozytoria, API, silniki AI) | Kotlin-first: wsparcie `suspend`, `object`, funkcji rozszerzających — czego Mockito nie robi natywnie |
| Turbine | 1.2.0 | testowanie `Flow`/`StateFlow` (UiState, strumienie Room) | proste, deterministyczne asercje na strumieniach — kluczowe przy architekturze reaktywnej |
| Android `SpeechRecognizer` | systemowe API | STT w rozmowach, wymowie i notatkach (MVP) | patrz sekcja 4 |
| Android `TextToSpeech` | systemowe API | głos tutora (US/GB, regulowane tempo) (MVP) | patrz sekcja 4 |

## 3. Dlaczego natywnie (Android), a nie KMP/Flutter

1. **Rdzeń produktu to głos:** `SpeechRecognizer`, `TextToSpeech`, `AudioRecord`/`MediaRecorder`,
   focus audio, uprawnienia mikrofonu — to głębokie API platformy. W Flutterze/KMP każda z tych
   integracji to warstwa mostków (plugin/expect-actual), która zwiększa latencję prac i ryzyko
   błędów dokładnie w najważniejszej funkcji aplikacji.
2. **Jedna platforma docelowa:** grupa docelowa (dorośli Polacy, MVP) jest obsługiwana na
   Androidzie; główny argument za KMP/Flutter — współdzielenie kodu między platformami —
   nie istnieje. Płacilibyśmy koszt abstrakcji bez zysku.
3. **Pełny dostęp do rekomendowanego stosu Google:** Compose, Room z Flow, Hilt, WorkManager,
   DataStore — dojrzały, spójnie zintegrowany zestaw z oficjalnym wsparciem i dokumentacją.
4. **Latencja i UX głosu:** cel <4 s odpowiedzi głosowej wymaga precyzyjnej kontroli cyklu
   STT→LLM→TTS bez narzutu mostków JS/Dart czy dodatkowej maszyny wirtualnej.
5. **Furtka na przyszłość:** czysto kotlinowy `core/domain` (i duża część `core/ai`) jest
   strukturalnie gotowy do migracji na KMP, jeśli w roadmapie pojawi się iOS — decyzja
   natywna nie zamyka tej drogi.

## 4. Dlaczego systemowe SpeechRecognizer/TextToSpeech w MVP, a Whisper/OpenAI TTS później

**MVP — systemowe:**

- **Koszt zero:** STT i TTS Androida są darmowe; Whisper API i OpenAI TTS są płatne per użycie —
  przy codziennych rozmowach koszty rosłyby liniowo z zaangażowaniem (odwrotnie do interesu produktu).
- **Latencja:** rozpoznawanie on-device/Google działa strumieniowo (partial results — na tym
  opiera się animacja „fali" i stan `listening`), bez uploadu audio; TTS startuje natychmiast.
  Whisper wymaga nagrania → wysłania pliku → odpowiedzi, co dokłada sekundy do pętli rozmowy.
- **Offline/prywatność:** on-device STT działa bez sieci i nie wysyła audio poza urządzenie
  (wymóg briefu: audio kasowane po transkrypcji, opt-in na wysyłkę do AI).
- **Bonus produktowy:** `SpeechRecognizer` zwraca **confidence scores**, które zasilają
  moduł wymowy (pronunciation) — dostajemy sygnał jakości wymowy bez dodatkowego API.
- **Prostota MVP:** brak zarządzania plikami audio w chmurze, brak kolejek uploadu.

**Roadmapa (S6) — Whisper / OpenAI TTS:**

- Whisper: wyższa dokładność dla mowy z silnym polskim akcentem i słabych mikrofonów;
  lepsza interpunkcja transkrypcji notatek.
- OpenAI TTS: naturalniejszy, cieplejszy głos tutora → wyższa immersja.
- Architektura już to przewiduje: `SpeechRecognizerManager` i `TextToSpeechManager`
  w `core/audio` są jedynymi punktami styku — podmiana implementacji nie dotyka
  domeny ani UI (reguła zależności z etapu 9).

## 5. Dlaczego Kotlinx Serialization, a nie Moshi/Gson

- **Bez refleksji, z pluginem kompilatora:** serializery generowane w czasie kompilacji —
  szybciej w runtime, brak problemów z R8/ProGuard (Gson opiera się na refleksji i wymaga
  reguł keep; klasa zminifikowana = cichy błąd parsowania).
- **Kotlin-first:** natywne wsparcie `data class`, wartości domyślnych, nullability,
  `sealed class` — Gson potrafi ominąć konstruktor i wstawić `null` w pole non-null;
  Moshi wymaga codegen (KSP) lub refleksji, by dorównać.
- **Jedna biblioteka do wszystkiego:** DTO OpenAI (w tym structured output JSON analiz
  błędów i ćwiczeń), kolumna `options(json)` w tabeli `exercises` (TypeConverter),
  eksport/backup danych do JSON (SettingsDataScreen) — wspólny `Json` konfigurowany
  raz (`ignoreUnknownKeys = true` — odporność na ewolucję API OpenAI).
- **Oficjalny kierunek:** projekt JetBrains, pierwszorzędny konwerter w Retrofit 2.11
  (`converter-kotlinx-serialization` w samym Retrofit), używany w Now in Android.
- Gson jest w trybie utrzymaniowym; Moshi jest dobry, ale dublowałby możliwości bez przewagi.

## 6. Dlaczego convention plugins (build-logic)

Wzorzec z Now in Android: katalog `build-logic/convention` z pluginami
`aienglishcoach.android.application`, `aienglishcoach.android.library`,
`aienglishcoach.android.library.compose`, `aienglishcoach.android.feature`,
`aienglishcoach.hilt`, `aienglishcoach.jvm.library`.

- **DRY dla ~18 modułów:** compileSdk/minSdk, Java 17 toolchain, opcje Kotlina, konfiguracja
  Compose, Hilt i testów zdefiniowane **raz**; plik `build.gradle.kts` feature-modułu ma
  kilka linii (plugin + zależności specyficzne).
- **Typowany Kotlin zamiast copy-paste:** logika budowania w `build-logic` jest kompilowana
  i testowalna, w przeciwieństwie do `subprojects {}` / `allprojects {}` (antywzorzec —
  łamie izolację projektów i configuration cache).
- **Egzekwowanie architektury:** plugin `aienglishcoach.android.feature` automatycznie
  dodaje zależności do `core/domain`, `core/designsystem`, Hilt i Compose — nowy feature
  z definicji ma poprawny kształt (patrz etap 11, sekcja „nowy feature moduł").
- **Version catalog (`gradle/libs.versions.toml`):** jedno źródło wersji dla wszystkich
  modułów i dla samego build-logic; aktualizacja biblioteki = zmiana jednej linii.
- **Wydajność:** spójna konfiguracja sprzyja configuration cache i build cache Gradle.

## 7. Podsumowanie zgodności ze stosem rekomendowanym przez Google

Cały stos (Compose + Material 3, ViewModel + StateFlow, Room, DataStore, Hilt, WorkManager,
Navigation Compose, coroutines/Flow, modularyzacja z convention plugins) odpowiada 1:1
aktualnym oficjalnym rekomendacjom z „Guide to app architecture" i wzorcowemu projektowi
Now in Android. Jedyne zewnętrzne zależności spoza AndroidX to sprawdzone standardy
społeczności: Retrofit/OkHttp, Coil, Timber, MockK, Turbine.
