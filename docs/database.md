# Baza danych (Room) — jak zaimplementowano

Jedna baza `CoachDatabase` (`core/database/src/main/kotlin/.../CoachDatabase.kt`),
wersja schematu 1, `exportSchema = true` (schematy trafiają do `core/database/schemas/`
i pozwalają na testy migracji w przyszłości).

## Tabele

| Tabela | Encja | Klucz obcy | Indeksy |
|---|---|---|---|
| `conversations` | `ConversationEntity` | — | `started_at` |
| `messages` | `MessageEntity` | `conversation_id` → conversations (CASCADE) | `(conversation_id, created_at)` |
| `user_errors` | `UserErrorEntity` | `conversation_id` → conversations (SET NULL) | `conversation_id`, `resolved_at` |
| `vocabulary_items` | `VocabularyItemEntity` | — | `word` (unique), `due_at` |
| `exercises` | `ExerciseEntity` | `source_error_id` → user_errors (SET NULL) | `due_at`, `source_error_id` |
| `exercise_attempts` | `ExerciseAttemptEntity` | `exercise_id` → exercises (CASCADE) | `exercise_id` |
| `pronunciation_results` | `PronunciationResultEntity` | — | `created_at` |
| `voice_notes` | `VoiceNoteEntity` | — | — |
| `daily_stats` | `DailyStatsEntity` | — (PK = `date` jako ISO-8601 string) | — |
| `ai_memories` | `AiMemoryEntity` | — | `kind` |

Wszystkie znaczniki czasu przechowywane są jako `Long` (epoch millis); DAO przyjmują/
zwracają `Long`, mapowanie na `kotlinx.datetime.Instant` odbywa się w warstwie
`core/data/mapper/*Mappers.kt` — encje Room nigdy nie wyciekają poza `core/database` +
`core/data`.

## Serializacja pól złożonych

- `Exercise.options` (`List<String>`) → kolumna `options: String` jako JSON
  (kotlinx.serialization `ListSerializer(String.serializer())`).
- `AiMemory.embedding` (`FloatArray`, embeddingi 1536-wymiarowe) → kolumna
  `embedding: ByteArray?`, little-endian float32 (`FloatArray.toByteArray()` /
  `ByteArray.toFloatArray()` w `core/data/mapper/MiscMappers.kt`).

## SRS (SM-2) w schemacie

`vocabulary_items` i `exercises` mają identyczny zestaw kolumn SRS:
`ease_factor` (Double, start 2.5), `interval_days` (Int), `repetition_count` (Int),
`due_at` (Long). Logika przeliczania — `core/domain/revision/Sm2Scheduler.kt`
(patrz `docs/ai.md`).

## Enumy jako String

Wszystkie enumy domenowe (`ConversationScenario`, `ConversationStatus`, `MessageRole`,
`ErrorCategory`, `VocabularyStatus`, `ExerciseType`, `MemoryKind`) są zapisywane jako
nazwa (`String`) w kolumnie. Mapowanie `enumOrDefault(name, default)` w
`core/data/mapper/ConversationMappers.kt` degraduje nieznaną wartość do bezpiecznego
domyślnego enuma zamiast rzucać wyjątkiem — chroni to przed crashem po dodaniu nowej
wartości enuma w starszej wersji danych.

## DAO

Każda tabela ma dedykowany DAO w `core/database/dao/` zwracający `Flow<...>` dla
obserwacji reaktywnej (`observeAll`, `observeDueCount`) i `suspend fun` dla operacji
jednorazowych. `AiMemoryDao.prune(maxCount)` usuwa najstarsze/najmniej istotne
wpisy jednym zapytaniem SQL (`ORDER BY importance ASC, updated_at ASC LIMIT ...`).

## DataStore (poza Room)

`UserPreferences` (poziom, cele, cel dzienny, głos/tempo TTS, model AI, motyw,
dynamic color, przypomnienia) żyje w Preferences DataStore
(`core/datastore/UserPreferencesDataSource.kt`) — nie w Room, bo to pojedynczy
rekord ustawień bez potrzeby zapytań SQL, a DataStore daje transakcyjne `edit {}`
i natywny `Flow` bez boilerplate'u DAO.

## Klucz API — osobny, zaszyfrowany magazyn

Klucz OpenAI **nie** jest w DataStore ani w Room. `core/datastore/ApiKeyStore.kt`
używa `EncryptedSharedPreferences` (Android Keystore, AES-256-GCM/SIV) w osobnym
pliku `secure_credentials`, jawnie wykluczonym z backupu w
`app/src/main/res/xml/backup_rules.xml` i `data_extraction_rules.xml`.

## Kasowanie danych

`SettingsRepositoryImpl.wipeAllData()`: `database.clearAllTables()` +
skasowanie katalogu `files/audio` (notatki głosowe) + `apiKeyStore.clear()` +
`userPreferencesDataSource.clear()`. Wywoływane z ekranu Ustawienia → Dane po
potwierdzeniu w dialogu (RODO — prawo do usunięcia danych).
