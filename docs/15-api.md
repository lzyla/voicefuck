# ETAP 15 — API i integracje

> Zakres: OpenAI Chat Completions + Embeddings (`core/network`, Retrofit 2.11.0 /
> OkHttp 4.12.0 / Kotlinx Serialization), Android `SpeechRecognizer` i `TextToSpeech`
> (`core/audio`), obsługa błędów (mapowanie na domenowe `AiError`), synchronizacja
> (MVP: eksport/import JSON) i aktualizacje (roadmapa). Nazwy DTO — etap 13.

## 1. OpenAI — konfiguracja klienta

- Base URL: `https://api.openai.com/`
- Retrofit + `kotlinx-serialization` converter, `Json { ignoreUnknownKeys = true; coerceInputValues = true }`
- OkHttp: `connectTimeout 15 s`, `readTimeout 60 s` (LLM potrafi długo generować),
  `writeTimeout 30 s`
- Interceptory (kolejność): `AuthInterceptor` → `HttpLoggingInterceptor`
  (tylko debug, poziom `BASIC` — **nigdy** `BODY` z nagłówkami; `redactHeader("Authorization")`)

```kotlin
interface OpenAiApi {
    @POST("v1/chat/completions")
    suspend fun chatCompletions(@Body body: ChatCompletionRequest): ChatCompletionResponse

    @POST("v1/embeddings")
    suspend fun embeddings(@Body body: EmbeddingsRequest): EmbeddingsResponse
}
```

### 1.1 AuthInterceptor

```kotlin
class AuthInterceptor @Inject constructor(private val apiKeyStore: ApiKeyStore) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val key = apiKeyStore.getApiKey() ?: throw MissingApiKeyException()
        val request = chain.request().newBuilder()
            .header("Authorization", "Bearer $key")
            .header("Content-Type", "application/json")
            .build()
        return chain.proceed(request)
    }
}
```

Klucz czytany z `ApiKeyStore` (EncryptedSharedPreferences, etap 14) w momencie
żądania — zmiana klucza w ustawieniach działa natychmiast, bez restartu klienta.

## 2. OpenAI Chat Completions — `POST /v1/chat/completions`

Nagłówki: `Authorization: Bearer sk-…`, `Content-Type: application/json`.
Model domyślny `gpt-4o-mini` (konfigurowalny w `UserPreferences.aiModel`).

### 2.1 Rozmowa z tutorem (tekst swobodny)

Request:

```json
{
  "model": "gpt-4o-mini",
  "temperature": 0.7,
  "max_tokens": 220,
  "messages": [
    { "role": "system", "content": "You are Coach, a friendly English tutor... (pełny prompt: etap 16) ...Student memory:\n- GOAL: wants to pass a job interview in English\n- WEAKNESS: confuses past simple and present perfect" },
    { "role": "user", "content": "Hi! Today I want practice for my job interview." },
    { "role": "assistant", "content": "Great choice! Let's do a mock interview. Tell me — what position are you applying for?" },
    { "role": "user", "content": "I apply for a senior Android developer." }
  ]
}
```

Response:

```json
{
  "id": "chatcmpl-abc123",
  "object": "chat.completion",
  "model": "gpt-4o-mini",
  "choices": [
    {
      "index": 0,
      "message": {
        "role": "assistant",
        "content": "Nice — a senior Android developer! Quick tip: say \"I'm applying for a senior Android developer position.\" So, tell me about a project you're proud of."
      },
      "finish_reason": "stop"
    }
  ],
  "usage": { "prompt_tokens": 640, "completion_tokens": 48, "total_tokens": 688 }
}
```

### 2.2 Analiza rozmowy (structured output, `response_format: json_object`)

Request:

```json
{
  "model": "gpt-4o-mini",
  "temperature": 0.2,
  "response_format": { "type": "json_object" },
  "messages": [
    { "role": "system", "content": "You are an English conversation analyzer... Respond ONLY with a JSON object matching the schema... (pełny prompt: etap 16)" },
    { "role": "user", "content": "Student level: B1.\nTranscript:\nUSER: I apply for a senior Android developer.\nASSISTANT: ...\nUSER: In my last job I have worked with Kotlin three years." }
  ]
}
```

Response — `choices[0].message.content` to string z JSON-em parsowanym do
`ConversationAnalysisDto`:

```json
{
  "summary": "Rozmowa rekrutacyjna: uczeń opowiadał o doświadczeniu w Androidzie...",
  "errors": [
    {
      "category": "GRAMMAR",
      "original": "In my last job I have worked with Kotlin three years.",
      "corrected": "In my last job I worked with Kotlin for three years.",
      "explanation": "Zakończony okres w przeszłości (last job) wymaga Past Simple, nie Present Perfect; przed okresem czasu potrzebne 'for'."
    }
  ],
  "vocabulary": [
    {
      "word": "apply for",
      "translation": "ubiegać się o",
      "definition": "to formally ask for a job or position",
      "example": "I'm applying for a senior developer position."
    }
  ],
  "overall_feedback": "Dobra płynność! Popracuj nad Past Simple vs Present Perfect."
}
```

### 2.3 Generowanie ćwiczeń (structured output)

Request (skrócony):

```json
{
  "model": "gpt-4o-mini",
  "temperature": 0.4,
  "response_format": { "type": "json_object" },
  "messages": [
    { "role": "system", "content": "You are an English exercise generator... (etap 16)" },
    { "role": "user", "content": "Student level: B1. Generate 4 exercises from these errors:\n[{\"id\":\"err-1\",\"category\":\"GRAMMAR\",\"original\":\"I have worked there three years\",\"corrected\":\"I worked there for three years\"}]" }
  ]
}
```

Response (content → `GeneratedExercisesDto`):

```json
{
  "exercises": [
    {
      "type": "FILL_GAP",
      "question": "In my previous job I ___ (work) with Kotlin for three years.",
      "options": [],
      "correct_answer": "worked",
      "explanation": "Zakończony okres w przeszłości → Past Simple.",
      "source_error_id": "err-1"
    },
    {
      "type": "MULTIPLE_CHOICE",
      "question": "Which sentence is correct?",
      "options": [
        "I have worked there three years ago.",
        "I worked there for three years.",
        "I am working there since three years."
      ],
      "correct_answer": "I worked there for three years.",
      "explanation": "Past Simple + 'for' dla zakończonego okresu.",
      "source_error_id": "err-1"
    }
  ]
}
```

### 2.4 Konsolidacja pamięci (structured output)

Request user-message: transkrypcja rozmowy + istniejące wspomnienia.
Response (content → `MemoryExtractionDto`):

```json
{
  "memories": [
    { "kind": "FACT", "content": "Works as an Android developer, mainly with Kotlin.", "importance": 0.8 },
    { "kind": "GOAL", "content": "Preparing for a senior Android developer job interview in English.", "importance": 0.9 },
    { "kind": "WEAKNESS", "content": "Confuses Past Simple with Present Perfect for finished time periods.", "importance": 0.7 }
  ]
}
```

### 2.5 Tygodniowy raport

Request user-message: zagregowane `daily_stats` + top błędy + nowe słówka z 7 dni.
Response: zwykły tekst markdown (bez `response_format`) — podsumowanie po polsku,
zapisywane i pokazywane w `StatisticsScreen`. Przykład content:

```
## Twój tydzień z angielskim 🎯
Rozmawiałeś 5 dni (86 minut, +20% vs poprzedni tydzień)...
**Największy postęp:** Past Simple — 8/10 poprawnych użyć...
**Do pracy:** przyimki czasu (for/since)...
```

## 3. OpenAI Embeddings — `POST /v1/embeddings`

Request:

```json
{
  "model": "text-embedding-3-small",
  "input": ["Confuses Past Simple with Present Perfect for finished time periods."]
}
```

Response:

```json
{
  "object": "list",
  "data": [
    { "object": "embedding", "index": 0, "embedding": [0.0123, -0.0456, "...1536 floatów..."] }
  ],
  "model": "text-embedding-3-small",
  "usage": { "prompt_tokens": 14, "total_tokens": 14 }
}
```

`input` jako lista — batch dla wielu wspomnień w jednym żądaniu (taniej i szybciej).
Wynik: `FloatArray(1536)` → BLOB w `ai_memories.embedding` (etap 14).

## 4. Obsługa błędów HTTP → domenowy `AiError`

Sealed w `core/domain`:

```kotlin
sealed interface AiError {
    data object InvalidApiKey : AiError        // 401
    data object RateLimited : AiError          // 429 (po wyczerpaniu retry)
    data object ServerError : AiError          // 5xx
    data object Timeout : AiError              // SocketTimeoutException
    data object Network : AiError              // IOException / brak sieci
    data object MissingApiKey : AiError        // brak klucza w ApiKeyStore
    data class InvalidResponse(val cause: String) : AiError  // niesparsowalny JSON structured output
    data class Unknown(val code: Int?, val message: String?) : AiError
}
```

Mapowanie w `core/data` (`safeAiCall { ... }` zwraca `Result<T, AiError>` z `core/common`):

| Sytuacja | HTTP / wyjątek | `AiError` | Zachowanie |
|---|---|---|---|
| Zły/odwołany klucz | 401 | `InvalidApiKey` | bez retry; UI: banner + link do `settings/ai` |
| Rate limit / brak środków | 429 | `RateLimited` | **retry z exponential backoff**: 3 próby, 1 s → 2 s → 4 s (+ jitter ±20%); honoruj nagłówek `Retry-After` jeśli obecny; po porażce komunikat "spróbuj za chwilę" |
| Błąd serwera OpenAI | 500/502/503 | `ServerError` | retry jak dla 429 (max 2 próby) |
| Timeout | `SocketTimeoutException` | `Timeout` | 1 ponowienie dla wywołań idempotentnych (analiza, embeddings); rozmowa: bez auto-retry (ryzyko podwójnej odpowiedzi), przycisk "Ponów" |
| Brak sieci | `UnknownHostException`/`IOException` | `Network` | tryb offline — patrz degradacja (etap 16) |
| Zły JSON z LLM | `SerializationException` | `InvalidResponse` | 1 ponowienie z dopiskiem "Return valid JSON only."; potem fail |
| Inne 4xx | np. 400, 404 (zły model) | `Unknown(code,…)` | log Timber (bez treści promptu w release), komunikat ogólny |

Retry realizowany interceptorem/wrapperem na poziomie repozytorium (nie w UI).
Workery (`MemoryConsolidationWorker`) zwracają `Result.retry()` przy
`RateLimited`/`ServerError`/`Network` — WorkManager doda własny backoff.

## 5. Android SpeechRecognizer (STT) — `core/audio/SpeechRecognizerManager`

Darmowe, niska latencja, on-device/Google — zgodnie z briefem (Whisper API = roadmapa).

### 5.1 Konfiguracja Intentu

```kotlin
val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")            // lub en-GB wg ttsVoice
    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)         // live transkrypcja w UI
    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1500L)
}
val recognizer = SpeechRecognizer.createSpeechRecognizer(context)  // wymaga RECORD_AUDIO
recognizer.setRecognitionListener(listener)
recognizer.startListening(intent)
```

### 5.2 RecognitionListener → `Flow<SttEvent>`

Manager opakowuje callbacki w `callbackFlow`:

```kotlin
sealed interface SttEvent {
    data object ReadyForSpeech : SttEvent
    data object BeginningOfSpeech : SttEvent
    data class RmsChanged(val rmsDb: Float) : SttEvent           // animacja fali MicButton
    data class Partial(val text: String) : SttEvent               // onPartialResults
    data class Final(val text: String, val confidence: Float?) : SttEvent
    data class Error(val error: SttError) : SttEvent
}
```

- `onResults(bundle)`: `RESULTS_RECOGNITION` (lista hipotez) +
  `CONFIDENCE_SCORES` (FloatArray 0.0–1.0, może być null/`-1` gdy silnik nie
  wspiera) — bierzemy hipotezę [0]; confidence zapisywany do analizy wymowy (etap 16).
- `onPartialResults(bundle)`: bieżący tekst do `ConversationUiState.partialTranscript`.

### 5.3 Obsługa błędów STT

| Kod | Znaczenie | Reakcja |
|---|---|---|
| `ERROR_NO_MATCH` | nic nie rozpoznano | łagodny komunikat "Nie usłyszałem — spróbuj jeszcze raz", auto-restart nasłuchu (max 2×) |
| `ERROR_SPEECH_TIMEOUT` | cisza | jak wyżej, powrót do stanu idle |
| `ERROR_AUDIO` | problem z audio | komunikat + stan idle |
| `ERROR_INSUFFICIENT_PERMISSIONS` | brak RECORD_AUDIO | nawigacja do prośby o uprawnienie (PermissionsScreen flow) |
| `ERROR_RECOGNIZER_BUSY` | poprzednia sesja aktywna | `cancel()` + retry po 300 ms |
| `ERROR_NETWORK` / `ERROR_NETWORK_TIMEOUT` | silnik online bez sieci | komunikat offline; STT bywa on-device — próbujemy raz jeszcze |
| `ERROR_CLIENT` | błąd klienta | `destroy()` + recreate recognizera |

Zawsze: `SpeechRecognizer.isRecognitionAvailable(context)` sprawdzane przy starcie;
`destroy()` w `onCleared`/lifecycle. Recognizer działa tylko na main thread —
manager wymusza `Dispatchers.Main`.

## 6. Android TextToSpeech (TTS) — `core/audio/TextToSpeechManager`

### 6.1 Inicjalizacja i wybór głosu

```kotlin
private var tts: TextToSpeech? = null

fun initialize(onReady: () -> Unit) {
    tts = TextToSpeech(context) { status ->
        if (status == TextToSpeech.SUCCESS) {
            applyPreferences()   // locale + rate z UserPreferences
            onReady()
        } else { /* TtsError.InitFailed */ }
    }
}

private fun applyPreferences() {
    val locale = when (prefs.ttsVoice) {
        TtsVoice.US -> Locale.US
        TtsVoice.UK -> Locale.UK
    }
    val result = tts?.setLanguage(locale)
    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
        tts?.language = Locale.US   // fallback
    }
    // opcjonalnie konkretny Voice (naturalniejszy, sieciowy):
    tts?.voices?.filter { it.locale == locale && !it.isNetworkConnectionRequired }
        ?.minByOrNull { it.latency }?.let { tts?.voice = it }
    tts?.setSpeechRate(prefs.ttsSpeed)   // 0.5–1.5, default 1.0; wolniej dla A2/B1
}
```

### 6.2 Mówienie + UtteranceProgressListener

```kotlin
fun speak(text: String, utteranceId: String): Flow<TtsEvent> = callbackFlow {
    tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
        override fun onStart(id: String) { trySend(TtsEvent.Started(id)) }      // MicButton → speaking
        override fun onDone(id: String) { trySend(TtsEvent.Done(id)); close() } // → auto-nasłuch STT
        override fun onError(id: String, errorCode: Int) { trySend(TtsEvent.Error(id, errorCode)); close() }
        override fun onRangeStart(id: String, start: Int, end: Int, frame: Int) {
            trySend(TtsEvent.Highlight(start, end))   // podświetlanie czytanego fragmentu
        }
    })
    tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    awaitClose { tts?.stop() }
}
```

Zasady: `stop()` gdy użytkownik dotknie `MicButton` (barge-in), `shutdown()` przy
zniszczeniu; kolejkowanie `QUEUE_FLUSH` (nowa odpowiedź przerywa starą);
`onDone` startuje automatyczne nasłuchiwanie STT (pętla konwersacji).

## 7. Synchronizacja

### 7.1 MVP — eksport/import JSON przez SAF

`SettingsDataScreen` → eksport pełnego stanu do pliku JSON wybieranego przez
Storage Access Framework (`ACTION_CREATE_DOCUMENT`, mime `application/json`),
import przez `ACTION_OPEN_DOCUMENT`. Bez uprawnień do storage.

Format (`ExportData`, wersjonowany):

```json
{
  "schemaVersion": 1,
  "exportedAt": "2026-07-17T10:00:00Z",
  "conversations": [ ... ], "messages": [ ... ], "userErrors": [ ... ],
  "vocabularyItems": [ ... ], "exercises": [ ... ], "exerciseAttempts": [ ... ],
  "pronunciationResults": [ ... ], "voiceNotes": [ ... ],
  "dailyStats": [ ... ], "aiMemories": [ ... ],
  "preferences": { "englishLevel": "B1", "dailyGoalMinutes": 10, ... }
}
```

- **Klucz API NIE jest eksportowany.** Embeddingi eksportowane jako Base64
  (albo pomijane i przeliczane po imporcie — decyzja: pomijane, tańszy plik,
  koszt przeliczenia ~centy). Pliki audio nie wchodzą do eksportu (MVP).
- Import: walidacja `schemaVersion`, transakcja Room, strategia *replace-all*
  po potwierdzeniu użytkownika.

### 7.2 Roadmapa (S6) — backend proxy + konta

Własny backend (proxy do OpenAI + auth): konto użytkownika, klucz OpenAI po
stronie serwera, synchronizacja delta (updatedAt + soft delete), rozwiązywanie
konfliktów last-write-wins per rekord (UUID PK z etapu 14 to umożliwia bez zmian schematu).

## 8. Aktualizacje — Play In-App Updates (roadmapa)

Po publikacji w Play: `AppUpdateManager` z trybem **flexible** (pobieranie w tle,
snackbar "Zainstaluj") dla zwykłych wydań i **immediate** dla krytycznych
(np. zmiana API OpenAI łamiąca klienta). MVP dystrybuowany bez tego mechanizmu.

## 9. Bezpieczeństwo — podsumowanie

1. Klucz API w **EncryptedSharedPreferences** (Keystore AES256-GCM) — etap 14.
2. Nagłówek `Authorization` dodawany **wyłącznie interceptorem** w `core/network`;
   żaden inny moduł nie widzi klucza.
3. Klucz **nigdy nie logowany**: logging interceptor tylko w debug, poziom BASIC,
   `redactHeader("Authorization")`; Timber w release bez logów sieciowych; klucz
   poza eksportem JSON i poza crash reportami.
4. Tylko HTTPS (`api.openai.com`); `android:usesCleartextTraffic="false"`.
5. UI pokazuje jedynie maskę `sk-...xxxx`; pole wpisywania z `PasswordVisualTransformation`.
6. **Rekomendacja produkcyjna (obowiązkowa przed skalowaniem):** backend proxy —
   klucz OpenAI wyłącznie po stronie serwera, aplikacja uwierzytelnia się tokenem
   użytkownika; limity per konto, kontrola kosztów, rotacja klucza bez wydania
   aplikacji. Klucz w aplikacji klienckiej (nawet szyfrowany at-rest) jest
   dostępny dla właściciela urządzenia — model MVP „użytkownik podaje własny
   klucz" jest akceptowalny tylko dlatego, że to *jego* klucz i jego koszty.

## 10. Checklista zgodności

- [x] Endpointy i modele zgodne z briefem (rozdz. 8): `/v1/chat/completions`
  (gpt-4o-mini), `/v1/embeddings` (text-embedding-3-small).
- [x] Structured output `response_format: json_object` dla analiz.
- [x] Mapowanie 401/429/5xx/timeout → `AiError`, retry z backoff dla 429/5xx.
- [x] STT: SpeechRecognizer z partial results i confidence; TTS: Locale.US/UK,
  speech rate, UtteranceProgressListener.
- [x] Sync MVP = eksport JSON (SAF); proxy + In-App Updates w roadmapie.
