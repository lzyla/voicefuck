# AI English Coach

Natywna aplikacja Android do nauki mówionego języka angielskiego przez codzienne,
spersonalizowane rozmowy głosowe z tutorem AI. Tutor pamięta ucznia (cele, słabości,
kontekst), analizuje każdą rozmowę, generuje ćwiczenia z realnych błędów i prowadzi
harmonogram powtórek (SRS) dla słownictwa i ćwiczeń.

Pełna dokumentacja projektowa (analiza produktu, user stories, architektura
informacji, UX/UI, Figma, architektura techniczna, model danych, plan sprintów)
znajduje się w [`docs/00-brief-decyzje-projektowe.md`](docs/00-brief-decyzje-projektowe.md)
i plikach `docs/01`–`docs/17`. Ten README opisuje **zaimplementowany kod**.

## Stos technologiczny

Kotlin 2.0 · Jetpack Compose + Material 3 · Hilt · Room · DataStore ·
Retrofit/OkHttp + Kotlinx Serialization · Coroutines/Flow · Navigation Compose ·
WorkManager · Timber · JUnit4/MockK/Turbine.

minSdk 26 · target/compileSdk 35 · Java toolchain 17.

## Struktura modułów

```
app/                      punkt wejścia: MainActivity, App, nawigacja, workery, DI
build-logic/convention/   pluginy konwencji Gradle (application/library/compose/hilt/room/jvm)
core/common/              Result/AppError, DispatcherProvider — bez Androida
core/domain/               modele domenowe, interfejsy repo/serwisów, use case'y, SM-2 — bez Androida
core/designsystem/        Theme, Typography, komponenty Compose (MicButton, CoachCard, ...)
core/database/            Room: encje, DAO, CoachDatabase
core/datastore/           DataStore (UserPreferences) + szyfrowany klucz API
core/network/             Retrofit/OkHttp klient OpenAI, DTO, mapowanie błędów
core/data/                implementacje repozytoriów, mapery Entity/DTO ↔ Domain
core/audio/                SpeechRecognizer, TextToSpeech, MediaRecorder
core/ai/                   prompty, OpenAiTutorService, MemoryManager (RAG)
core/testing/              MainDispatcherRule, TestDispatcherProvider
feature/onboarding/        powitanie → poziom → cele → uprawnienia → klucz API
feature/home/              dashboard: streak, cel dzienny, skróty do powtórek
feature/conversation/      rozmowa głosowa, historia, analiza po rozmowie
feature/practice/          ćwiczenia, słownictwo (SRS), wymowa, notatki głosowe
feature/statistics/        statystyki i wykresy
feature/settings/          profil, głos AI, klucz API, wygląd, dane
feature/learningpath/      ścieżka nauki: jednostki, lekcje, fiszki, ćwiczenia (statyczna treść)
feature/chat/              czat tekstowy z tutorem (ten sam backend co rozmowa głosowa)
feature/profile/           profil, odznaki, ekran "Go Pro"
```

Zależności modułów są jednokierunkowe: `feature/*` → `core/domain` + `core/designsystem`
+ `core/common`; implementacje (`core/data`, `core/audio`, `core/ai`) są spięte przez Hilt,
a moduły feature nigdy nie zależą od siebie nawzajem. Szczegóły w
[`docs/architecture.md`](docs/architecture.md).

## Szybki start

Zobacz [`docs/setup.md`](docs/setup.md) — krótko:

```bash
echo "sdk.dir=/path/to/android-sdk" > local.properties
./gradlew assembleDebug
./gradlew test        # testy jednostkowe wszystkich modułów
```

Klucz API OpenAI wpisuje się w aplikacji (onboarding lub Ustawienia → AI) — jest
przechowywany wyłącznie lokalnie, w zaszyfrowanym magazynie (`EncryptedSharedPreferences`).

## Dokumentacja

| Temat | Plik |
|---|---|
| Architektura (Clean Architecture, MVVM, DI, offline-first) | [`docs/architecture.md`](docs/architecture.md) |
| Baza danych (schemat Room, migracje) | [`docs/database.md`](docs/database.md) |
| Integracje API (OpenAI, STT/TTS) | [`docs/api.md`](docs/api.md) |
| Moduł AI (prompty, pamięć/RAG, SRS) | [`docs/ai.md`](docs/ai.md) |
| Uruchomienie projektu | [`docs/setup.md`](docs/setup.md) |
| Rozwój projektu (dodawanie modułu/ekranu) | [`docs/development.md`](docs/development.md) |
| Roadmapa | [`docs/roadmap.md`](docs/roadmap.md) |
| Pełna dokumentacja projektowa (etapy 1–17) | [`docs/`](docs/) — pliki `00`–`17` |
| Redesign "Liquid Glass" (2026-07-17) — co realne, co makieta | [`docs/18-liquid-glass-redesign.md`](docs/18-liquid-glass-redesign.md) |

## Status

Zaimplementowany zakres odpowiada sprintom S0–S5 z [planu implementacji](docs/17-plan-implementacji.md):
fundamenty, onboarding, rozmowa głosowa, analiza po rozmowie, ćwiczenia + SRS,
statystyki, wymowa, notatki głosowe, WorkManager. Synchronizacja chmurowa,
Whisper/OpenAI TTS i rozszerzony RAG to roadmapa (S6) — patrz [`docs/roadmap.md`](docs/roadmap.md).
