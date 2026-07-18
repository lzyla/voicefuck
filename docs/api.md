# Integracje API — jak zaimplementowano

## OpenAI Chat Completions

`core/network/api/OpenAiApi.kt` — Retrofit, `POST v1/chat/completions`.
`core/network/OpenAiDataSource.kt` to jedyny punkt wejścia używany przez resztę
aplikacji (`core/ai/OpenAiTutorService.kt`) — nie wywołuje się `OpenAiApi` bezpośrednio.

Odpowiedzialności `OpenAiDataSource`:
- buduje `ChatCompletionRequest` (model, wiadomości, temperatura, opcjonalnie
  `response_format: json_object` dla wywołań strukturalnych),
- **retry z exponential backoff** (max 2 próby, start 1000ms, mnożnik ×2) dla
  429 (`RateLimited`) i 5xx (`AiService`),
- mapuje wyjątki na `AppError`: 401/403→`InvalidApiKey`, 429→`RateLimited`,
  5xx→`AiService`, `IOException`→`Network`, pusta odpowiedź→`AiService`,
  brak klucza (`MissingApiKeyException` z interceptora)→`MissingApiKey`.

## Authorization

`core/network/interceptor/AuthInterceptor.kt` dokleja nagłówek `Authorization: Bearer`
do każdego requestu, pobierając klucz przez `ApiKeyProvider` (bridge do
`core/datastore/ApiKeyStore`, spięty w `core/data/di/DataModule`). Brak klucza →
`MissingApiKeyException` rzucany **przed** wysłaniem requestu (nigdy nie leci pusty
nagłówek). Klucz nigdy nie trafia do logów — `HttpLoggingInterceptor` jest tylko na
poziomie `BASIC` (bez body/nagłówków) i tylko w debug (`BuildConfig.DEBUG`).

## Wywołania strukturalne (JSON)

`OpenAiTutorService.structured<T, R>(...)` to generyczna funkcja pomocnicza: wysyła
prompt z `response_format=json_object`, parsuje odpowiedź `kotlinx.serialization.json`
do wewnętrznego DTO (`core/ai/dto/StructuredOutputDtos.kt`, widoczność `internal` —
nigdy nie wycieka poza `core/ai`), po czym mapuje do modelu domenowego. Błąd
parsowania (np. model zwrócił tekst zamiast JSON) → `AppError.AiService`, **nigdy
wyjątek** — złapane w `try/catch` wewnątrz `structured`.

Cztery wywołania strukturalne: `analyzeConversation`, `generateExercises`,
`extractMemories` — każde ma dedykowany system prompt w
`core/ai/prompt/PromptBuilder.kt` (pełne treści w `docs/ai.md`).

## OpenAI Embeddings

`POST v1/embeddings`, model `text-embedding-3-small` (1536 wymiarów) —
`OpenAiDataSource.embed(texts, model)` zwraca `List<FloatArray>` posortowane wg
`index` z odpowiedzi. Używane wyłącznie przez `core/ai/MemoryManager.kt`
(patrz `docs/ai.md`).

## Android SpeechRecognizer (STT)

`core/audio/SpeechRecognizerManager.kt` implementuje
`domain.service.SpeechToTextService` jako `callbackFlow` emitujący
`SpeechEvent`: `ReadyForSpeech`, `Partial(text)` (live), `RmsChanged(level)`
(do animacji fali w `MicButton`), `Result(text, confidence)`,
`Error(AppError.SpeechRecognition(reason))`. Język rozpoznawania: `en-US`
(`RecognizerIntent.EXTRA_LANGUAGE`), `EXTRA_PARTIAL_RESULTS = true`.

Mapowanie błędów recognizer → `SpeechErrorReason`: `ERROR_NO_MATCH`→`NO_MATCH`,
`ERROR_SPEECH_TIMEOUT`→`TIMEOUT`, `ERROR_AUDIO`→`AUDIO`,
`ERROR_RECOGNIZER_BUSY`→`RECOGNIZER_BUSY`,
`ERROR_INSUFFICIENT_PERMISSIONS`→`AppError.MicrophonePermissionDenied` (osobna
gałąź, bo UI reaguje inaczej — pokazuje prośbę o uprawnienie, nie komunikat błędu).

## Android TextToSpeech (TTS)

`core/audio/TextToSpeechManager.kt` implementuje `TextToSpeechService`.
Inicjalizacja leniwa i asynchroniczna (`CompletableDeferred<Boolean>`), `speak(text)`
zawiesza korutynę do `UtteranceProgressListener.onDone/onError`
(`suspendCancellableCoroutine`), `configure(voice, speechRate)` ustawia
`Locale.US`/`Locale.UK` i tempo (klamrowane do 0.5–1.5) z ustawień użytkownika.

## Synchronizacja / kopie zapasowe

MVP: brak backendu — dane żyją tylko lokalnie w Room. Android Auto Backup jest
włączony (`android:allowBackup="true"`), ale plik zaszyfrowanego klucza API jest
z niego jawnie wykluczony (`backup_rules.xml`, `data_extraction_rules.xml`).
Eksport/import JSON i synchronizacja chmurowa to roadmapa — patrz `docs/roadmap.md`
i `docs/15-api.md` (sekcja projektowa).

## Aktualizacje aplikacji

Brak w MVP (dystrybucja poza Play Store w tej fazie). Play In-App Updates —
roadmapa.
