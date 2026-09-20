# Roadmapa

Zaimplementowany zakres tego repozytorium odpowiada sprintom **S0–S5** z
`docs/17-plan-implementacji.md`: fundamenty (build-logic, moduły core, design
system, nawigacja), onboarding + ustawienia, rozmowa głosowa (STT/TTS + OpenAI +
Room), analiza po rozmowie (błędy, słownictwo, podsumowanie), ćwiczenia + SRS +
fiszki, statystyki + wymowa + notatki głosowe + WorkManager.

## S6 i dalej (nie zaimplementowane w tym repo)

- **Material 3 Expressive (pełne przyjęcie)** — `MaterialExpressiveTheme`,
  `expressiveLightColorScheme`/`expressiveDarkColorScheme`, stabilny `MotionScheme`
  i rozszerzona skala kształtów (`MaterialShapes`) istnieją na razie tylko w
  alpha (material3 1.5.0-alphaNN, stan na lipiec 2026). Obecnie przyjęte są
  stabilne elementy tego ducha (pełny zestaw ról `ColorScheme`, "miększa" skala
  promieni, fizyka sprężynowa na przejściach `MicButton`) — patrz
  `docs/architecture.md`. Migracja na właściwe API Expressive po stabilizacji
  1.5.0.
- **Synchronizacja chmurowa** — konta użytkowników, backend synchronizujący
  Room ↔ chmura, rozwiązywanie konfliktów offline-first (patrz szkic w
  `docs/15-api.md`). Na tym etapie jedyna trwałość danych to lokalna baza Room.
- **Backend proxy dla klucza OpenAI** — produkcyjnie klucz API nie powinien być
  wpisywany przez użytkownika i trzymany na urządzeniu (nawet zaszyfrowany), tylko
  chowany za własnym serwerem proxy, który dolicza limity/koszty per użytkownik.
  MVP celowo używa klucza użytkownika, żeby uniknąć budowy backendu przed
  walidacją produktu.
- **Whisper API / OpenAI TTS** jako alternatywa dla systemowego
  `SpeechRecognizer`/`TextToSpeech` — wyższa jakość rozpoznawania akcentu i
  bardziej naturalny głos kosztem opóźnienia i kosztu API. Architektura już to
  przewiduje: `SpeechToTextService`/`TextToSpeechService` to interfejsy domenowe,
  więc dodanie alternatywnej implementacji (`WhisperSpeechToTextService` itp.)
  nie wymaga zmian w `feature/*`.
- **Rozszerzony RAG** — obecny `MemoryManager` liczy podobieństwo kosinusowe w
  Kotlinie nad wszystkimi wspomnieniami (wystarczające przy dziesiątkach–setkach
  wpisów na użytkownika). Przy większej skali: wektorowa baza danych (np. lokalny
  indeks ANN) zamiast liniowego przeszukania.
- **Play In-App Updates** i dystrybucja przez Google Play.
- **Testy migracji Room** — pierwsza migracja pojawi się przy pierwszej zmianie
  schematu w wersji produkcyjnej; infrastruktura (`exportSchema = true`,
  `core/database/schemas/`) jest już gotowa.
- **CI/CD** — pipeline GitHub Actions (`./gradlew test lint assembleDebug` na PR,
  release build + podpisywanie na tag).
- **Rozszerzone testy UI** — więcej testów Compose (`androidTest`) dla pełnych
  przepływów ekranowych, obecnie pokryty jest reprezentatywny komponent
  (`MicButton`) i logika ViewModeli.

## Priorytetyzacja

Kolejność powyżej odzwierciedla ryzyko: backend proxy i synchronizacja chmurowa
mają największy wpływ na model kosztów i bezpieczeństwo produkcyjne, więc
naturalnie poprzedzają szerszą dystrybucję (Play Store, In-App Updates).
