# ETAP 7 — UI / Design System

> Dokument zgodny z kanonicznym briefem: `docs/00-brief-decyzje-projektowe.md`.
> Wszystkie nazwy ekranów, tras, komponentów i wartości kolorów pochodzą z briefu.
> Stos UI: **Jetpack Compose + Material 3**, font **Inter**, siatka **4dp**,
> ikony **Material Symbols Rounded**.

---

## Spis treści

1. [Low Fidelity — wireframe'y wszystkich ekranów](#1-low-fidelity--wireframey)
2. [High Fidelity — opis wyglądu każdego ekranu](#2-high-fidelity--opisy-ekranów)
3. [Design System](#3-design-system)
   - 3.1 Paleta kolorów (role Material 3, light + dark)
   - 3.2 Skala typograficzna M3 (Inter)
   - 3.3 Ikony (Material Symbols Rounded)
   - 3.4 Komponenty kanoniczne (specyfikacje)
   - 3.5 Grid i spacing (tokeny)
   - 3.6 Elevation (poziomy M3)
   - 3.7 Motion (czasy, easing, animacje)
   - 3.8 Dark Mode i Light Mode (zasady)
   - 3.9 Dynamic color

---

## 1. Low Fidelity — wireframe'y

Konwencja adnotacji: `◄ nazwa komponentu / uwaga`. Każdy box = obszar dotykowy lub
sekcja layoutu. Szerokość referencyjna: telefon (Pixel 8, 412dp).

### 1.1 SplashScreen (`splash`)

```
┌────────────────────────────────┐
│                                │
│                                │
│           ┌──────┐             │
│           │ LOGO │             │  ◄ logo aplikacji (ikona mikrofonu + "AI")
│           └──────┘             │
│        AI English Coach        │  ◄ nazwa produktu (headlineMedium)
│                                │
│            ◌ ◌ ◌               │  ◄ LoadingIndicator (subtelny)
│                                │
│                                │
└────────────────────────────────┘
Logika: brak UI interaktywnego; po odczycie DataStore →
onboarding/welcome (pierwsze uruchomienie) lub home.
```

### 1.2 WelcomeScreen (`onboarding/welcome`)

```
┌────────────────────────────────┐
│                                │
│      ┌──────────────────┐      │
│      │   ILUSTRACJA     │      │  ◄ hero: rozmowa z AI (ilustracja/emoji 🎙️)
│      │   (rozmowa)      │      │
│      └──────────────────┘      │
│                                │
│   Mów po angielsku             │  ◄ headlineLarge
│   codziennie — z tutorem AI    │
│                                │
│   Realistyczne rozmowy głosowe,│  ◄ bodyLarge, 2–3 linie wartości
│   analiza błędów, powtórki SRS │
│   i pamięć Twoich postępów.    │
│                                │
│   ● ○ ○                        │  ◄ page indicator (3 slajdy wartości)
│                                │
│ ┌────────────────────────────┐ │
│ │       Zaczynamy  →         │ │  ◄ CoachPrimaryButton (pełna szerokość)
│ └────────────────────────────┘ │
└────────────────────────────────┘
```

### 1.3 LevelSelectionScreen (`onboarding/level`)

```
┌────────────────────────────────┐
│ ←  Krok 1 z 4                  │  ◄ CoachTopBar (back + progres kroków)
│ ▓▓▓▓░░░░░░░░░░░░               │  ◄ LinearProgressIndicator (25%)
│                                │
│  Jaki jest Twój poziom?        │  ◄ headlineMedium
│  Wybierz samoocenę — AI        │  ◄ bodyMedium (onSurfaceVariant)
│  dopasuje trudność rozmów.     │
│                                │
│ ┌────────────────────────────┐ │
│ │ (A2)  Podstawowy           │ │  ◄ CoachCard wybieralna + LevelChip
│ │ Proste zdania, codzienne…  │ │
│ ├────────────────────────────┤ │
│ │ (B1)  Średnio zaawansowany │ │  ◄ stan selected = obrys primary
│ ├────────────────────────────┤ │
│ │ (B2)  Wyższy średni        │ │
│ ├────────────────────────────┤ │
│ │ (C1)  Zaawansowany         │ │
│ └────────────────────────────┘ │
│                                │
│ ┌────────────────────────────┐ │
│ │          Dalej             │ │  ◄ CoachPrimaryButton (disabled do wyboru)
│ └────────────────────────────┘ │
└────────────────────────────────┘
```

### 1.4 GoalsScreen (`onboarding/goals`)

```
┌────────────────────────────────┐
│ ←  Krok 2 z 4                  │  ◄ CoachTopBar
│ ▓▓▓▓▓▓▓▓░░░░░░░░               │  ◄ 50%
│                                │
│  Po co uczysz się mówić?       │  ◄ headlineMedium
│  Możesz wybrać kilka celów.    │
│                                │
│ ┌───────────┐ ┌───────────┐    │
│ │ ☐ Praca   │ │ ☐ Podróże │    │  ◄ FilterChip multi-select (flow layout)
│ └───────────┘ └───────────┘    │
│ ┌───────────┐ ┌────────────┐   │
│ │ ☐ Egzamin │ │ ☐ Emigracja│   │
│ └───────────┘ └────────────┘   │
│ ┌────────────┐ ┌───────────┐   │
│ │ ☐ Rozmowy  │ │ ☐ Kultura │   │
│ └────────────┘ └───────────┘   │
│                                │
│  Dzienny cel rozmów            │  ◄ titleMedium
│  ○──────●───────○              │  ◄ Slider: 5 / 10 / 15 / 20 min
│      10 minut dziennie         │  ◄ wartość (titleLarge, primary)
│                                │
│ ┌────────────────────────────┐ │
│ │          Dalej             │ │  ◄ CoachPrimaryButton
│ └────────────────────────────┘ │
└────────────────────────────────┘
```

### 1.5 PermissionsScreen (`onboarding/permissions`)

```
┌────────────────────────────────┐
│ ←  Krok 3 z 4                  │  ◄ CoachTopBar
│ ▓▓▓▓▓▓▓▓▓▓▓▓░░░░               │  ◄ 75%
│                                │
│  Potrzebujemy dostępu          │  ◄ headlineMedium
│                                │
│ ┌────────────────────────────┐ │
│ │ [🎙]  Mikrofon             │ │  ◄ CoachCard: ikona mic + opis
│ │ Do rozmów głosowych i      │ │
│ │ analizy wymowy.   [Zezwól] │ │  ◄ przycisk / check po nadaniu
│ ├────────────────────────────┤ │
│ │ [🔔]  Powiadomienia        │ │  ◄ ikona notifications
│ │ Przypomnienia o powtórkach │ │
│ │ i celu dziennym.  [Zezwól] │ │  ◄ opcjonalne (można pominąć)
│ └────────────────────────────┘ │
│                                │
│  Audio przetwarzamy lokalnie   │  ◄ bodySmall, nota prywatności
│  i kasujemy po transkrypcji.   │
│                                │
│ ┌────────────────────────────┐ │
│ │          Dalej             │ │  ◄ CoachPrimaryButton (aktywny gdy mic OK)
│ └────────────────────────────┘ │
│          Pomiń powiadomienia   │  ◄ TextButton
└────────────────────────────────┘
```

### 1.6 ApiSetupScreen (`onboarding/api`)

```
┌────────────────────────────────┐
│ ←  Krok 4 z 4                  │  ◄ CoachTopBar
│ ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓               │  ◄ 100%
│                                │
│  Podłącz OpenAI                │  ◄ headlineMedium
│  MVP używa Twojego klucza API. │  ◄ bodyMedium + link "Jak zdobyć klucz?"
│                                │
│ ┌────────────────────────────┐ │
│ │ Klucz API        [👁]      │ │  ◄ OutlinedTextField (password toggle)
│ │ sk-••••••••••••••          │ │
│ └────────────────────────────┘ │
│                                │
│ ┌────────────────────────────┐ │
│ │ ⓘ Klucz jest szyfrowany i  │ │  ◄ CoachCard informacyjna (secondaryContainer)
│ │ nie opuszcza urządzenia    │ │
│ │ poza wywołaniami OpenAI.   │ │
│ └────────────────────────────┘ │
│                                │
│ [ Testuj połączenie ]          │  ◄ OutlinedButton → LoadingIndicator → ✓/✗
│                                │
│ ┌────────────────────────────┐ │
│ │     Zakończ konfigurację   │ │  ◄ CoachPrimaryButton (aktywny po teście OK)
│ └────────────────────────────┘ │
└────────────────────────────────┘
```

### 1.7 HomeScreen (`home`)

```
┌────────────────────────────────┐
│  Cześć, Marta! 👋        [⚙]   │  ◄ CoachTopBar: powitanie + ikona settings
│                                │
│ ┌────────────────────────────┐ │
│ │  🔥 7        ┌────────┐    │ │  ◄ CoachCard "dziś": streak (tertiary)
│ │  dni streak  │ ◔ 6/10 │    │ │  ◄ ProgressRing celu dziennego (minuty)
│ │              │  min   │    │ │
│ └────────────────────────────┘ │
│                                │
│ ┌────────────────────────────┐ │
│ │  🎙  Zacznij rozmowę       │ │  ◄ hero CTA (primaryContainer, duży)
│ │  Scenariusz: "Small talk   │ │  ◄ sugerowany scenariusz od AI
│ │  w pracy"          [Start] │ │
│ └────────────────────────────┘ │
│                                │
│  Do powtórki dziś              │  ◄ titleMedium
│ ┌─────────────┐ ┌────────────┐ │
│ │ 12 słówek   │ │ 5 ćwiczeń  │ │  ◄ 2× StatTile → nawigacja do review
│ │ [Powtórz]   │ │ [Rozwiąż]  │ │
│ └─────────────┘ └────────────┘ │
│                                │
│  Ostatnia rozmowa              │  ◄ titleMedium
│ ┌────────────────────────────┐ │
│ │ "W restauracji" · 8 min    │ │  ◄ CoachCard → summary
│ │ 3 błędy · 5 nowych słówek  │ │
│ └────────────────────────────┘ │
│                                │
│ [🏠 Home][💬 Rozmowy][📚 Nauka][📊 Statystyki]  ◄ NavigationBar (4 zakładki)
└────────────────────────────────┘
```

### 1.8 ConversationListScreen (`conversations`)

```
┌────────────────────────────────┐
│  Rozmowy                       │  ◄ CoachTopBar (tytuł zakładki)
│                                │
│ ┌────────────────────────────┐ │
│ │ 🔍 Szukaj w rozmowach      │ │  ◄ SearchBar (opcjonalny filtr)
│ └────────────────────────────┘ │
│  Dziś                          │  ◄ nagłówek sekcji (labelLarge)
│ ┌────────────────────────────┐ │
│ │ W restauracji              │ │  ◄ CoachCard pozycji listy:
│ │ 8 min · 12 wiadomości      │ │    tytuł, meta, chip scenariusza
│ │ (Scenariusz) 3 błędy       │ │
│ ├────────────────────────────┤ │
│ │ Small talk w pracy         │ │
│ │ 5 min · 8 wiadomości       │ │
│ └────────────────────────────┘ │
│  Wczoraj                       │
│ ┌────────────────────────────┐ │
│ │ Rozmowa swobodna …         │ │
│ └────────────────────────────┘ │
│                                │
│                        ┌─────┐ │
│                        │ 🎙+ │ │  ◄ FAB "nowa rozmowa" → wybór scenariusza
│                        └─────┘ │    (bottom sheet) → conversation/{id}
│ [🏠][💬][📚][📊]               │  ◄ NavigationBar
└────────────────────────────────┘
Pusty stan: EmptyState (ikona forum, "Brak rozmów — zacznij pierwszą!").
```

### 1.9 ConversationScreen (`conversation/{id}`)

```
┌────────────────────────────────┐
│ ←  W restauracji     ⏱ 04:32 ⋮ │  ◄ CoachTopBar: back, tytuł, timer, menu
│                                │    (menu: zakończ, przełącz tłumaczenia)
│ ┌──────────────────────┐       │
│ │ AI: Good evening!    │       │  ◄ MessageBubble variant=assistant (lewa)
│ │ Table for two?  [🔊] │       │    akcja: odtwórz TTS ponownie
│ └──────────────────────┘       │
│ │ pl: Dobry wieczór…   │       │  ◄ tłumaczenie (zwijane, bodySmall)
│                                │
│       ┌──────────────────────┐ │
│       │ USER: Yes, please. A │ │  ◄ MessageBubble variant=user (prawa)
│       │ table by the window. │ │
│       └──────────────────────┘ │
│                                │
│ ┌──────────────────────┐       │
│ │ AI: ▊▊▋ …            │       │  ◄ streaming/typing indicator
│ └──────────────────────┘       │
│                                │
│      「 Słucham… 」            │  ◄ etykieta stanu MicButton (labelLarge)
│    ~~~~~ fala audio ~~~~~      │  ◄ wizualizacja fali (stan listening)
│                                │
│   [⌨]      ((🎙))       [⏹]   │  ◄ klawiatura (fallback) | MicButton | stop
└────────────────────────────────┘
Brak dolnej nawigacji (tryb immersyjny rozmowy).
```

### 1.10 ConversationSummaryScreen (`conversation/{id}/summary`)

```
┌────────────────────────────────┐
│ ←  Podsumowanie rozmowy        │  ◄ CoachTopBar
│                                │
│ ┌────────────────────────────┐ │
│ │ "W restauracji"            │ │  ◄ CoachCard nagłówkowa:
│ │ 8 min · 12 wiadomości      │ │    metryki + ocena ogólna AI
│ │ Płynność: ████░ dobra      │ │
│ └────────────────────────────┘ │
│                                │
│  Feedback od tutora            │  ◄ titleMedium
│ ┌────────────────────────────┐ │
│ │ Świetnie użyłaś czasu      │ │  ◄ podsumowanie LLM (summary)
│ │ przeszłego! Popracujmy nad…│ │
│ └────────────────────────────┘ │
│                                │
│  Błędy (3)                     │  ◄ titleMedium + licznik
│ ┌────────────────────────────┐ │
│ │ (GRAMMAR)                  │ │  ◄ karta błędu: kategoria (chip),
│ │ ✗ "I have went"            │ │    oryginał (przekreślony, error),
│ │ ✓ "I have gone"            │ │    poprawka (secondary), wyjaśnienie
│ │ Present perfect używa…     │ │
│ └────────────────────────────┘ │
│  Nowe słówka (5)               │
│ ┌──────────┐ ┌──────────┐      │
│ │ reserve  │ │ waiter   │ …    │  ◄ chipy słówek → dodane do vocabulary
│ └──────────┘ └──────────┘      │
│ ┌────────────────────────────┐ │
│ │  Wygeneruj ćwiczenia (3)   │ │  ◄ CoachPrimaryButton → ExercisesScreen
│ └────────────────────────────┘ │
│          Wróć do Home          │  ◄ TextButton
└────────────────────────────────┘
```

### 1.11 PracticeHubScreen (`practice`)

```
┌────────────────────────────────┐
│  Nauka                         │  ◄ CoachTopBar
│                                │
│ ┌────────────────────────────┐ │
│ │ 📅 Dziś do zrobienia       │ │  ◄ CoachCard zbiorcza due:
│ │ 12 słówek · 5 ćwiczeń      │ │    suma z SRS (revision)
│ └────────────────────────────┘ │
│                                │
│ ┌─────────────┐ ┌────────────┐ │
│ │ ✏️ Ćwiczenia │ │ 🃏 Słówka  │ │  ◄ 4 kafle nawigacyjne (CoachCard):
│ │ 5 due       │ │ 12 due     │ │    ćwiczenia / słownictwo
│ ├─────────────┤ ├────────────┤ │
│ │ 🗣 Wymowa    │ │ 🎤 Notatki │ │    wymowa / notatki głosowe
│ │ 4 do trenin.│ │ 9 notatek  │ │
│ └─────────────┘ └────────────┘ │
│                                │
│  Słabe strony (z pamięci AI)   │  ◄ titleMedium
│ ┌────────────────────────────┐ │
│ │ • Present perfect          │ │  ◄ lista z ai_memories (WEAKNESS)
│ │ • Wymowa "th"              │ │
│ └────────────────────────────┘ │
│                                │
│ [🏠][💬][📚][📊]               │  ◄ NavigationBar (aktywna: Nauka)
└────────────────────────────────┘
```

### 1.12 ExercisesScreen (`practice/exercises`)

```
┌────────────────────────────────┐
│ ←  Ćwiczenia                   │  ◄ CoachTopBar
│                                │
│ [ Due (5) ]  [ Wszystkie ]     │  ◄ SegmentedButton / TabRow filtr
│                                │
│ ┌────────────────────────────┐ │
│ │ (FILL_GAP)                 │ │  ◄ CoachCard: typ (chip), pytanie
│ │ "I ___ to London twice."   │ │    skrócone, źródło (błąd z rozmowy),
│ │ z: "W restauracji"  due 🕐 │ │    status due
│ ├────────────────────────────┤ │
│ │ (MULTIPLE_CHOICE)          │ │
│ │ Wybierz poprawną formę…    │ │
│ ├────────────────────────────┤ │
│ │ (TRANSLATION)              │ │
│ │ Przetłumacz: "Chciałbym…"  │ │
│ └────────────────────────────┘ │
│                                │
│ ┌────────────────────────────┐ │
│ │   Rozpocznij serię (5)     │ │  ◄ CoachPrimaryButton →
│ └────────────────────────────┘ │    practice/exercises/session
└────────────────────────────────┘
Pusty stan: EmptyState ("Brak ćwiczeń — porozmawiaj, a AI je wygeneruje").
```

### 1.13 ExercisePlayerScreen (`practice/exercises/session`)

```
┌────────────────────────────────┐
│ ✕   Ćwiczenie 2 z 5            │  ◄ CoachTopBar: zamknij + postęp
│ ▓▓▓▓▓▓░░░░░░░░░               │  ◄ LinearProgressIndicator
│                                │
│  (FILL_GAP)                    │  ◄ chip typu ćwiczenia
│                                │
│  Uzupełnij lukę:               │  ◄ titleMedium
│  "I ______ to London twice."   │  ◄ pytanie (headlineSmall)
│                                │
│ ┌────────────┐ ┌────────────┐  │
│ │ have gone  │ │ have been  │  │  ◄ opcje odpowiedzi (karty wybieralne)
│ ├────────────┤ ├────────────┤  │    lub TextField (TRANSLATION),
│ │ went       │ │ was going  │  │    lub MicButton (SPEAKING)
│ └────────────┘ └────────────┘  │
│                                │
│ ┌────────────────────────────┐ │
│ │ ✓ Dobrze! "have been" bo…  │ │  ◄ panel feedbacku po odpowiedzi
│ └────────────────────────────┘ │    (secondaryContainer / errorContainer)
│                                │
│ ┌────────────────────────────┐ │
│ │      Sprawdź / Dalej       │ │  ◄ CoachPrimaryButton (2 stany)
│ └────────────────────────────┘ │
└────────────────────────────────┘
Ostatni krok → ekran wyniku serii (X/5, aktualizacja SRS) → powrót.
```

### 1.14 VocabularyScreen (`practice/vocabulary`)

```
┌────────────────────────────────┐
│ ←  Słownictwo                  │  ◄ CoachTopBar
│                                │
│ ┌────────────────────────────┐ │
│ │ 🔍 Szukaj słówka           │ │  ◄ SearchBar
│ └────────────────────────────┘ │
│ [Wszystkie][Nowe][W nauce][Opanowane]  ◄ FilterChips (status)
│                                │
│ ┌────────────────────────────┐ │
│ │ reserve        (NEW)   🔊  │ │  ◄ wiersz: słowo, status chip,
│ │ rezerwować                 │ │    tłumaczenie, TTS, due
│ ├────────────────────────────┤ │
│ │ waiter      (LEARNING) 🔊  │ │
│ │ kelner · powtórka: jutro   │ │
│ ├────────────────────────────┤ │
│ │ fluent     (MASTERED)  🔊  │ │
│ │ płynny                     │ │
│ └────────────────────────────┘ │
│                                │
│ ┌────────────────────────────┐ │
│ │   Powtórz due (12) 🃏      │ │  ◄ CoachPrimaryButton →
│ └────────────────────────────┘ │    practice/vocabulary/review
└────────────────────────────────┘
Tap na wiersz → bottom sheet ze szczegółami (definicja, przykład, źródło rozmowy).
```

### 1.15 VocabularyReviewScreen (`practice/vocabulary/review`)

```
┌────────────────────────────────┐
│ ✕   Fiszki  3 / 12             │  ◄ CoachTopBar + postęp
│ ▓▓▓▓░░░░░░░░░░░               │
│                                │
│ ┌────────────────────────────┐ │
│ │                            │ │
│ │        reserve   🔊        │ │  ◄ fiszka — przód: słowo EN + TTS
│ │                            │ │    (tap = obrót 3D na tył)
│ │   „Dotknij, aby odwrócić"  │ │
│ │                            │ │
│ └────────────────────────────┘ │
│  — po odwróceniu: —            │
│ │ rezerwować                 │ │  ◄ tył: tłumaczenie, definicja,
│ │ "I'd like to reserve…"     │ │    przykład z rozmowy
│                                │
│  Jak Ci poszło?                │  ◄ oceny SM-2 (0–5 zmapowane na 4 przyciski)
│ [Nie wiem][Trudne][Dobre][Łatwe] │ ◄ error / tertiary / secondary / primary
│                                │
└────────────────────────────────┘
Koniec sesji → podsumowanie (ile opanowane, następna powtórka).
```

### 1.16 PronunciationScreen (`practice/pronunciation`)

```
┌────────────────────────────────┐
│ ←  Wymowa                      │  ◄ CoachTopBar
│                                │
│  Trenuj słowo                  │  ◄ titleMedium
│ ┌────────────────────────────┐ │
│ │       thoroughly           │ │  ◄ słowo (displaySmall)
│ │      /ˈθʌrəli/   [🔊]      │ │  ◄ transkrypcja IPA + wzorzec TTS
│ └────────────────────────────┘ │
│                                │
│           ((🎙))               │  ◄ MicButton (nagraj próbę)
│      ~~~~ fala ~~~~            │
│                                │
│ ┌────────────────────────────┐ │
│ │  Wynik: 72/100  ◔          │ │  ◄ ProgressRing ze score
│ │  Usłyszano: "toroli"       │ │  ◄ recognizedText vs expectedText
│ │  💡 Język między zębami    │ │  ◄ feedback AI
│ │  przy "th"…    [Spróbuj ↻] │ │
│ └────────────────────────────┘ │
│                                │
│  Kolejka treningu (4)          │  ◄ lista słów z niskim confidence STT
│ ┌────────────────────────────┐ │
│ │ thoroughly · 72  ▸         │ │
│ │ vegetable  · 65  ▸         │ │
│ └────────────────────────────┘ │
└────────────────────────────────┘
```

### 1.17 VoiceNotesScreen (`practice/notes`)

```
┌────────────────────────────────┐
│ ←  Notatki głosowe             │  ◄ CoachTopBar
│                                │
│ ┌────────────────────────────┐ │
│ │ Pomysł na small talk       │ │  ◄ wiersz notatki: tytuł, czas,
│ │ 0:42 · dziś 14:20    [▶]   │ │    odtwarzanie
│ │ "Remember to ask about…"   │ │  ◄ transkrypcja (2 linie, zwijana)
│ ├────────────────────────────┤ │
│ │ Słówka z filmu             │ │
│ │ 1:15 · wtorek        [▶]   │ │
│ ├────────────────────────────┤ │
│ │ (bez transkrypcji)   [▶]   │ │  ◄ stan: transkrypcja w toku/brak
│ └────────────────────────────┘ │
│                                │
│                                │
│           ((🎙))               │  ◄ MicButton — nagrywanie nowej notatki
│      「 Nagrywam… 0:12 」      │  ◄ stan listening + licznik czasu
│                                │
└────────────────────────────────┘
Pusty stan: EmptyState ("Nagraj pierwszą notatkę głosową").
Swipe wiersza → usuń (z potwierdzeniem).
```

### 1.18 StatisticsScreen (`statistics`)

```
┌────────────────────────────────┐
│  Statystyki                    │  ◄ CoachTopBar
│  [Tydzień][Miesiąc][Wszystko]  │  ◄ SegmentedButton zakresu
│                                │
│ ┌──────────┐ ┌──────────┐      │
│ │ 🔥 7     │ │ ⏱ 84 min │      │  ◄ 4× StatTile (grid 2×2):
│ │ streak   │ │ rozmów   │      │    streak, minuty, słówka,
│ ├──────────┤ ├──────────┤      │    skuteczność ćwiczeń
│ │ 📖 46    │ │ ✅ 82%   │      │
│ │ słówek   │ │ ćwiczeń  │      │
│ └──────────┘ └──────────┘      │
│                                │
│  Minuty rozmów                 │  ◄ titleMedium
│ ┌────────────────────────────┐ │
│ │ ▂▄▆▁▅▇▃  (wykres słupkowy) │ │  ◄ bar chart 7 dni (primary)
│ │ Pn Wt Śr Cz Pt So Nd       │ │
│ └────────────────────────────┘ │
│  Słownictwo — przyrost         │
│ ┌────────────────────────────┐ │
│ │ ／～／ (wykres liniowy)     │ │  ◄ line chart (secondary)
│ └────────────────────────────┘ │
│  Raport tygodniowy AI          │
│ ┌────────────────────────────┐ │
│ │ "W tym tygodniu najwięcej… │ │  ◄ CoachCard z raportem LLM
│ └────────────────────────────┘ │
│ [🏠][💬][📚][📊]               │  ◄ NavigationBar (aktywna: Statystyki)
└────────────────────────────────┘
```

### 1.19 SettingsScreen (`settings`)

```
┌────────────────────────────────┐
│ ←  Ustawienia                  │  ◄ CoachTopBar
│                                │
│  KONTO I NAUKA                 │  ◄ nagłówek sekcji (labelLarge)
│ ┌────────────────────────────┐ │
│ │ 👤 Profil i cele        ▸  │ │  ◄ → settings/profile
│ │ Poziom B1 · 10 min/dzień   │ │
│ ├────────────────────────────┤ │
│ │ ✨ AI i głos            ▸  │ │  ◄ → settings/ai
│ │ gpt-4o-mini · głos US      │ │
│ └────────────────────────────┘ │
│  APLIKACJA                     │
│ ┌────────────────────────────┐ │
│ │ 🎨 Motyw                   │ │  ◄ dialog: Light/Dark/System
│ │ Systemowy                  │ │    + przełącznik dynamic color
│ ├────────────────────────────┤ │
│ │ 🔔 Powiadomienia       ⬤   │ │  ◄ Switch (powtórki, cel dzienny)
│ └────────────────────────────┘ │
│  DANE                          │
│ ┌────────────────────────────┐ │
│ │ 💾 Dane i eksport       ▸  │ │  ◄ → settings/data
│ └────────────────────────────┘ │
│  Wersja 1.0.0 (S5)             │  ◄ bodySmall, wyśrodkowane
└────────────────────────────────┘
```

### 1.20 SettingsAiScreen (`settings/ai`)

```
┌────────────────────────────────┐
│ ←  AI i głos                   │  ◄ CoachTopBar
│                                │
│  MODEL                         │
│ ┌────────────────────────────┐ │
│ │ Model rozmów               │ │  ◄ ExposedDropdownMenu:
│ │ gpt-4o-mini            ▾   │ │    gpt-4o-mini (domyślny) / inne
│ ├────────────────────────────┤ │
│ │ Klucz API                  │ │  ◄ pole maskowane sk-•••• +
│ │ sk-••••…f3k   [Zmień]      │ │    akcja zmiany + test połączenia
│ └────────────────────────────┘ │
│  GŁOS TUTORA (TTS)             │
│ ┌────────────────────────────┐ │
│ │ Akcent    (US) (GB)        │ │  ◄ SegmentedButton US/GB
│ ├────────────────────────────┤ │
│ │ Tempo mowy                 │ │  ◄ Slider 0.5×–1.5×
│ │ ○────●────○   1.0×         │ │
│ ├────────────────────────────┤ │
│ │ [🔊 Posłuchaj próbki]      │ │  ◄ OutlinedButton — próbka TTS
│ └────────────────────────────┘ │
└────────────────────────────────┘
```

### 1.21 SettingsProfileScreen (`settings/profile`)

```
┌────────────────────────────────┐
│ ←  Profil i cele               │  ◄ CoachTopBar
│                                │
│  POZIOM ANGIELSKIEGO           │
│ ┌────────────────────────────┐ │
│ │ (A2) (B1) (B2) (C1)        │ │  ◄ 4× LevelChip (single select,
│ │       ▲ wybrany            │ │    aktualny = selected)
│ └────────────────────────────┘ │
│  CELE NAUKI                    │
│ ┌────────────────────────────┐ │
│ │ ☑ Praca  ☐ Podróże         │ │  ◄ FilterChips jak w onboardingu
│ │ ☑ Rozmowy ☐ Egzamin …      │ │
│ └────────────────────────────┘ │
│  CEL DZIENNY                   │
│ ┌────────────────────────────┐ │
│ │ ○──────●───────○  10 min   │ │  ◄ Slider 5–20 min
│ └────────────────────────────┘ │
│                                │
│ ┌────────────────────────────┐ │
│ │          Zapisz            │ │  ◄ CoachPrimaryButton
│ └────────────────────────────┘ │
└────────────────────────────────┘
```

### 1.22 SettingsDataScreen (`settings/data`)

```
┌────────────────────────────────┐
│ ←  Dane i eksport              │  ◄ CoachTopBar
│                                │
│  TWOJE DANE                    │
│ ┌────────────────────────────┐ │
│ │ 14 rozmów · 46 słówek      │ │  ◄ CoachCard podsumowania danych
│ │ 23 ćwiczenia · 9 notatek   │ │
│ └────────────────────────────┘ │
│  EKSPORT                       │
│ ┌────────────────────────────┐ │
│ │ 📤 Eksportuj do JSON    ▸  │ │  ◄ eksport lokalny (MVP sync)
│ │ Ostatni: 12.07.2026        │ │
│ └────────────────────────────┘ │
│  SYNCHRONIZACJA (ROADMAPA)     │
│ ┌────────────────────────────┐ │
│ │ ☁️ Synchronizacja chmurowa │ │  ◄ pozycja disabled + badge "Wkrótce"
│ │ Wkrótce                    │ │
│ └────────────────────────────┘ │
│  STREFA NIEBEZPIECZNA          │  ◄ nagłówek w kolorze error
│ ┌────────────────────────────┐ │
│ │ 🗑 Usuń wszystkie dane     │ │  ◄ czerwona pozycja → dialog
│ └────────────────────────────┘ │    potwierdzenia (wpisz "USUŃ")
└────────────────────────────────┘
```

---

## 2. High Fidelity — opisy ekranów

Wspólne zasady dla wszystkich ekranów:

- Tło ekranu: `background` (`#FAFAFF` light / `#121218` dark); karty na
  `surfaceContainerLow`/`surfaceContainer`.
- Padding boczny treści: **16dp**; odstępy między sekcjami: **24dp**; wewnątrz kart: **16dp**.
- `CoachTopBar` = M3 `TopAppBar` (small lub center-aligned), tło `surface`,
  tytuł `titleLarge`, ikony `onSurfaceVariant`.
- Dolna `NavigationBar` (M3): tło `surfaceContainer`, aktywny element — ikona w
  pigułce `secondaryContainer`, label `labelMedium`; ikony: `home`, `forum`,
  `school`, `monitoring`.
- Touch targets min. **48dp**, kontrast AA (zgodnie z wymaganiami niefunkcjonalnymi briefu).

### 2.1 SplashScreen

Pełnoekranowe tło `background`; w ciemnym motywie `#121218`. Logo — okrąg 96dp w
`primaryContainer` z ikoną `mic` 48dp w `onPrimaryContainer`; pod nim nazwa
"AI English Coach" w `headlineMedium` (`onBackground`). `LoadingIndicator`
(wariant dots, `primary`) 32dp pod nazwą. Fade-out całości (200 ms, emphasized
decelerate) przy przejściu do celu nawigacji. Brak paska systemowego — edge-to-edge.

### 2.2 WelcomeScreen

Layout kolumnowy, wyśrodkowany. Ilustracja hero w kontenerze 280dp z tłem
`primaryContainer` o promieniu 24dp. Nagłówek `headlineLarge` (`onBackground`),
opis `bodyLarge` (`onSurfaceVariant`), maks. 3 linie. `HorizontalPager` z trzema
slajdami wartości (rozmowy / analiza / powtórki), page indicator: kropki 8dp —
aktywna `primary`, nieaktywne `surfaceVariant`. Na dole `CoachPrimaryButton`
"Zaczynamy" (pełna szerokość, wysokość 56dp) dosunięty do safe area z marginesem 16dp.

### 2.3 LevelSelectionScreen

`CoachTopBar` z ikoną `arrow_back` i tekstem "Krok 1 z 4" (`titleMedium`). Pod
paskiem `LinearProgressIndicator` (4dp, `primary` na `surfaceVariant`). Lista
czterech `CoachCard` wybieralnych (radius 12dp): w stanie domyślnym tło
`surfaceContainerLow`, obrys `outlineVariant` 1dp; w stanie **selected** obrys
`primary` 2dp, tło `primaryContainer` z 8% nasyceniem (M3 selected container),
`LevelChip` w kolorze pełnym. Każda karta: `LevelChip` (A2–C1) po lewej, tytuł
poziomu `titleMedium`, opis `bodyMedium` (`onSurfaceVariant`). `CoachPrimaryButton`
"Dalej" disabled (kontener `onSurface` 12%, treść `onSurface` 38%) dopóki brak wyboru.

### 2.4 GoalsScreen

Chipy celów: M3 `FilterChip` w `FlowRow` (gap 8dp) — niewybrany: obrys `outline`,
label `onSurfaceVariant`; wybrany: tło `secondaryContainer`, ikona `check` 18dp,
label `onSecondaryContainer`. Sekcja celu dziennego oddzielona 24dp: `titleMedium`,
M3 `Slider` z krokami (5/10/15/20), track `primary`/`surfaceVariant`; wartość
"10 minut dziennie" w `titleLarge` kolorem `primary`, wyśrodkowana. CTA jak w 2.3.

### 2.5 PermissionsScreen

Dwie `CoachCard` (radius 12dp, tło `surfaceContainerLow`). W każdej: ikona w
kółku 40dp `primaryContainer` (`mic` / `notifications`), tytuł `titleMedium`,
opis `bodyMedium`, po prawej `OutlinedButton` "Zezwól"; po nadaniu uprawnienia
przycisk zamienia się w ikonę `check_circle` (`secondary`) z animacją fade-through
(250 ms). Nota prywatności `bodySmall` (`onSurfaceVariant`) z ikoną `lock` 16dp.
Powiadomienia opcjonalne — `TextButton` "Pomiń powiadomienia" (`primary`).

### 2.6 ApiSetupScreen

`OutlinedTextField` (M3) z labelem "Klucz API", `visualTransformation` password,
trailing icon `visibility`/`visibility_off`; obrys focus `primary`, błąd walidacji
(`error` + supporting text). Karta informacyjna: tło `secondaryContainer`, ikona
`info` (`onSecondaryContainer`), tekst `bodySmall`. "Testuj połączenie" =
`OutlinedButton`; podczas testu w miejscu labelu `LoadingIndicator` 18dp; wynik:
ikona `check_circle` (`secondary`) lub `ErrorBanner` nad przyciskiem
("Nieprawidłowy klucz lub brak sieci"). `CoachPrimaryButton` "Zakończ konfigurację"
aktywny po pozytywnym teście; kliknięcie zapisuje klucz (szyfrowany DataStore),
ustawia `onboardingCompleted` i nawiguje do `home` z wyczyszczeniem back stacku.

### 2.7 HomeScreen

- **TopBar:** powitanie "Cześć, {imię}!" `titleLarge`, po prawej `IconButton`
  `settings` (`onSurfaceVariant`).
- **Karta "dziś":** `CoachCard` (radius 12dp, tło `surfaceContainerLow`, elevation 1).
  Po lewej streak: ikona `local_fire_department` 32dp w `tertiary`, liczba
  `displaySmall` (`onSurface`), podpis `labelMedium`. Po prawej `ProgressRing`
  72dp: tor `surfaceVariant`, progres `primary`, w środku "6/10 min" (`titleMedium`).
  Po osiągnięciu celu ring zmienia kolor na `secondary` + jednorazowa animacja skali.
- **Hero CTA:** karta 96dp wysokości, tło `primaryContainer`, radius 16dp. Ikona
  `mic` w kole `primary` 48dp (`onPrimary`), tytuł "Zacznij rozmowę" `titleLarge`
  (`onPrimaryContainer`), podtytuł ze scenariuszem `bodyMedium`. Cała karta
  klikalna → bottom sheet wyboru scenariusza → `conversation/{id}`.
- **Powtórki due:** dwa `StatTile` w rzędzie (gap 12dp) — słówka (ikona `style`,
  akcent `secondary`) i ćwiczenia (ikona `quiz`, akcent `tertiary`); wartość
  `headlineSmall`, przycisk tekstowy w stopce kafla.
- **Ostatnia rozmowa:** `CoachCard` z tytułem, metadanymi `bodySmall` i chipem
  liczby błędów (`errorContainer`/`onErrorContainer`); tap → summary.

### 2.8 ConversationListScreen

Lista `LazyColumn` ze sticky headers dat (`labelLarge`, `primary`). Pozycja listy:
`CoachCard` — tytuł `titleMedium`, meta "8 min · 12 wiadomości" `bodySmall`
(`onSurfaceVariant`), `AssistChip` scenariusza (obrys `outline`) i chip błędów
(`errorContainer`) gdy > 0. FAB rozszerzony (M3 Extended FAB): tło
`primaryContainer`, ikona `mic`, label "Nowa rozmowa"; przy scrollu zwija się do
okrągłego. `SearchBar` M3 na górze (tło `surfaceContainerHigh`, radius pełny).
Pusty stan: `EmptyState` z ikoną `forum` 64dp.

### 2.9 ConversationScreen

Tryb immersyjny — bez dolnej nawigacji. `CoachTopBar`: `arrow_back`, tytuł
scenariusza, timer `labelLarge` monospaced tabular figures, menu `more_vert`
(pozycje: "Zakończ i analizuj", "Pokaż/ukryj tłumaczenia").

- **MessageBubble assistant:** wyrównanie do lewej, tło `surfaceContainerHigh`,
  tekst `bodyLarge` `onSurface`, radius 16dp z ostrym rogiem 4dp u dołu-lewej;
  ikona `volume_up` 20dp (`onSurfaceVariant`) do ponownego TTS; tłumaczenie PL
  zwijane pod bąblem (`bodySmall`, `onSurfaceVariant`).
- **MessageBubble user:** wyrównanie do prawej, tło `primary`, tekst `onPrimary`,
  radius 16dp z ostrym rogiem 4dp u dołu-prawej. Wiadomość rozpoznana z niską
  pewnością STT — podkreślenie falowane `tertiary` (kandydat do treningu wymowy).
- **Strefa dolna (104dp):** wyśrodkowany `MicButton` 80dp (stany opisane w 3.4.4),
  nad nim etykieta stanu ("Słucham…", "Myślę…", "Mówię…") `labelLarge` i
  wizualizacja fali (5 słupków, `primary`). Po lewej `IconButton` `keyboard`
  (fallback tekstowy — pole nad strefą), po prawej `IconButton` `stop_circle`
  (`error`) kończący rozmowę → dialog "Zakończyć i przeanalizować?".
- Streaming odpowiedzi AI: bąbel z animowanym kursorem; auto-scroll do dołu.

### 2.10 ConversationSummaryScreen

Karta nagłówkowa: tło `primaryContainer`, tytuł rozmowy `titleLarge`, metryki
w wierszu (`bodyMedium`), pasek płynności (`LinearProgressIndicator` `secondary`).
Feedback tutora: `CoachCard` z ikoną `emoji_objects` (`tertiary`), tekst `bodyLarge`.
Sekcja błędów: karty z chipem kategorii (GRAMMAR — `primary`, VOCABULARY —
`secondary`, PRONUNCIATION — `tertiary`, FLUENCY — `outline`); oryginał
przekreślony w `error`, poprawka w `secondary` z ikoną `check`, wyjaśnienie
`bodyMedium` (`onSurfaceVariant`). Nowe słówka: `SuggestionChip` w `FlowRow`; tap
→ bottom sheet szczegółów słówka. `CoachPrimaryButton` "Wygeneruj ćwiczenia (N)"
— po tapnięciu `LoadingIndicator` w przycisku, po sukcesie nawigacja do
`practice/exercises`. `TextButton` "Wróć do Home".

### 2.11 PracticeHubScreen

Karta due: tło `tertiaryContainer` gdy są zaległości (ikona `event`,
`onTertiaryContainer`), tło `surfaceContainerLow` gdy brak ("Wszystko zrobione ✓",
ikona `task_alt` w `secondary`). Grid 2×2 kafli nawigacyjnych (`CoachCard` 104dp,
gap 12dp): ikona 28dp w kółku `primaryContainer`, tytuł `titleMedium`, licznik
`bodySmall` z kolorem `primary` gdy > 0. Ikony: `quiz` (ćwiczenia), `style`
(słówka), `record_voice_over` (wymowa), `mic` (notatki). Sekcja "Słabe strony":
`CoachCard` z listą punktowaną (`bodyMedium`), ikona `psychology` — dane z
`ai_memories` (kind = WEAKNESS).

### 2.12 ExercisesScreen

`SegmentedButton` (M3) "Due (N)" / "Wszystkie" pod TopBarem. Karty ćwiczeń:
chip typu (FILL_GAP `primary`, MULTIPLE_CHOICE `secondary`, TRANSLATION
`tertiary`, SPEAKING `error` tonalny — wszystkie jako `SuggestionChip` z tłem
kontenerowym), pytanie `bodyLarge` (maks. 2 linie, ellipsis), źródło
"z: {tytuł rozmowy}" `bodySmall` + ikona `history` 16dp, badge due (`schedule`,
`tertiary`). Sticky bottom: `CoachPrimaryButton` "Rozpocznij serię (N)" na tle
`surface` z górnym `outlineVariant` 1dp.

### 2.13 ExercisePlayerScreen

TopBar: `close` + "Ćwiczenie X z Y" + `LinearProgressIndicator`. Treść zależna
od typu:

- **FILL_GAP / MULTIPLE_CHOICE:** opcje jako karty 56dp (radius 12dp, obrys
  `outlineVariant`); wybrana — obrys `primary` 2dp; po sprawdzeniu poprawna —
  tło `secondaryContainer` + `check_circle`, błędna wybrana — tło
  `errorContainer` + `cancel`.
- **TRANSLATION:** `OutlinedTextField` wieloliniowy, licznik znaków.
- **SPEAKING:** `MicButton` 64dp + transkrypcja na żywo `bodyLarge`.

Panel feedbacku wsuwa się od dołu (spring, 300 ms): tło `secondaryContainer`
(poprawnie) lub `errorContainer` (błędnie), wyjaśnienie `bodyMedium`.
`CoachPrimaryButton`: "Sprawdź" → po odpowiedzi "Dalej" (crossfade labelu).
Ekran końcowy serii: `ProgressRing` 120dp z wynikiem X/Y, komunikat, wpływ na
SRS ("następna powtórka: za 6 dni"), przycisk "Zakończ".

### 2.14 VocabularyScreen

`SearchBar` + rząd `FilterChip` (Wszystkie/Nowe/W nauce/Opanowane; wybrany —
`secondaryContainer`). Wiersz słówka (72dp, `ListItem` M3): słowo `titleMedium`,
tłumaczenie `bodyMedium` (`onSurfaceVariant`), status chip — NEW
(`primaryContainer`), LEARNING (`tertiaryContainer`), MASTERED
(`secondaryContainer`); trailing `IconButton` `volume_up`. Due podpisane
"powtórka: jutro" `labelSmall` (`tertiary`). Tap → `ModalBottomSheet` (radius 16dp
górne rogi): słowo `headlineSmall`, definicja, przykład (kursywa), link do rozmowy
źródłowej, akcje: TTS, zmień status, usuń. Sticky bottom CTA "Powtórz due (N)".

### 2.15 VocabularyReviewScreen

Fiszka: karta 320dp wysokości, radius 16dp, elevation 3, tło
`surfaceContainerLow`; przód — słowo `displaySmall` wyśrodkowane + `volume_up`;
tył — tłumaczenie `headlineSmall`, definicja `bodyLarge`, przykład `bodyMedium`
kursywą. Obrót Y-axis 3D (400 ms, emphasized). Rząd ocen SM-2 (wysokość 56dp,
gap 8dp): "Nie wiem" (tonal `errorContainer`), "Trudne" (`tertiaryContainer`),
"Dobre" (`secondaryContainer`), "Łatwe" (`primaryContainer`) — mapowanie na oceny
SM-2: 1 / 3 / 4 / 5. Przyciski ocen widoczne dopiero po odwróceniu (fade-in).
Podsumowanie sesji: statystyki + `CoachPrimaryButton` "Zakończ".

### 2.16 PronunciationScreen

Karta słowa: tło `surfaceContainerLow`, słowo `displaySmall`, IPA `bodyLarge`
monospaced (`onSurfaceVariant`), `IconButton` `volume_up` (wzorzec TTS).
`MicButton` 80dp pod kartą. Karta wyniku (po próbie, slide-up 300 ms):
`ProgressRing` 64dp — score < 60 `error`, 60–79 `tertiary`, ≥ 80 `secondary`;
"Usłyszano: …" `bodyLarge` z podświetleniem różnic (`error` na rozbieżnych
sylabach), feedback AI z ikoną `tips_and_updates`, `OutlinedButton` "Spróbuj
ponownie" (`refresh`). Kolejka treningu: `ListItem` ze score-badge i chevronem.

### 2.17 VoiceNotesScreen

Lista notatek: `ListItem` 88dp — tytuł `titleMedium` (lub "Notatka {data}" gdy
brak), czas trwania + data `bodySmall`, transkrypcja `bodyMedium` 2 linie;
leading `IconButton` `play_circle` 40dp (`primary`), podczas odtwarzania →
`pause_circle` + mini-pasek postępu pod wierszem. Notatka bez transkrypcji:
`LoadingIndicator` 16dp + "Transkrybuję…" `labelSmall`. Strefa nagrywania na
dole (jak w ConversationScreen): `MicButton` 72dp; podczas nagrywania — licznik
czasu `titleMedium` tabular + fala; zakończenie → dialog tytułu (opcjonalny) →
zapis + transkrypcja w tle. Swipe-to-dismiss z tłem `errorContainer` i ikoną
`delete`; `Snackbar` z akcją "Cofnij".

### 2.18 StatisticsScreen

`SegmentedButton` zakresu (Tydzień/Miesiąc/Wszystko). Grid 2×2 `StatTile`
(gap 12dp): streak (`local_fire_department`, `tertiary`), minuty (`timer`,
`primary`), słówka (`book_2`, `secondary`), skuteczność (`task_alt`,
`secondary`); wartość `headlineMedium`, delta vs poprzedni okres `labelSmall`
(`secondary` ↑ / `error` ↓). Wykres słupkowy minut: słupki `primary` (radius 4dp
u góry), dzisiejszy — `tertiary`; oś `labelSmall` `onSurfaceVariant`; wartość
słupka w tooltipie po tapnięciu. Wykres liniowy słówek: linia `secondary` 2dp,
wypełnienie gradient `secondary` 20%→0%. Karta raportu AI: ikona
`auto_awesome` (`primary`), tekst `bodyMedium`, stopka "Wygenerowano: niedziela"
`labelSmall`; stan ładowania — skeleton shimmer.

### 2.19 SettingsScreen

Grupy w `CoachCard` (radius 12dp), pozycje `ListItem` 64dp z ikoną leading
24dp (`onSurfaceVariant`), tytułem `bodyLarge`, wartością bieżącą `bodyMedium`
(`onSurfaceVariant`) i `chevron_right` lub `Switch` (M3, aktywny `primary`).
Nagłówki grup `labelLarge` (`primary`), padding 16/8. Dialog motywu:
`AlertDialog` z `RadioButton` (Jasny/Ciemny/Systemowy) + `Switch` "Kolory
dynamiczne (Material You)" — widoczny tylko na Androidzie 12+. Stopka wersji
`bodySmall` (`outline`), wyśrodkowana.

### 2.20 SettingsAiScreen

Model: `ExposedDropdownMenuBox` z listą modeli (domyślnie `gpt-4o-mini`).
Klucz API: wartość maskowana `bodyLarge` monospaced, `TextButton` "Zmień" →
dialog z polem i testem połączenia (jak w ApiSetupScreen). Akcent TTS:
`SegmentedButton` US/GB. Tempo: `Slider` 0.5×–1.5× (krok 0.1), wartość
`titleMedium` `primary`. "Posłuchaj próbki": `OutlinedButton` z `volume_up`;
podczas odtwarzania ikona `graphic_eq` animowana.

### 2.21 SettingsProfileScreen

Rząd czterech `LevelChip` (single-select — wybrany pełny `primary`, pozostałe
obrysowe). Cele: `FilterChip` `FlowRow` jak w GoalsScreen (spójność 1:1).
Cel dzienny: `Slider` jak w 2.4. `CoachPrimaryButton` "Zapisz" — aktywny tylko
gdy są zmiany; po zapisie `Snackbar` "Zapisano" i powrót.

### 2.22 SettingsDataScreen

Karta podsumowania: liczniki w 2 kolumnach `titleMedium` + `labelMedium`.
Eksport: `ListItem` z `upload_file`; tap → `LoadingIndicator` → systemowy share
sheet z plikiem JSON; data ostatniego eksportu `bodySmall`. Synchronizacja
chmurowa: `ListItem` disabled (`onSurface` 38%) + `Badge` "Wkrótce"
(`tertiaryContainer`). Strefa niebezpieczna: nagłówek `labelLarge` w `error`;
pozycja `delete_forever` w `error`; dialog potwierdzenia wymaga wpisania "USUŃ",
przycisk potwierdzający — `Button` z kontenerem `error`/`onError`.

---

## 3. Design System

### 3.1 Paleta kolorów — role Material 3 (light + dark)

Kolory bazowe z briefu: primary `#4F46E5` (indygo), secondary `#14B8A6` (teal),
tertiary `#F59E0B` (bursztyn), error `#DC2626`, background light `#FAFAFF`,
dark `#121218`/`#1C1C24`, primary dark `#A5B4FC`. Pozostałe role wyprowadzone
z palet tonalnych tych seedów.

| Rola M3 | Hex — Light | Hex — Dark |
|---|---|---|
| `primary` | `#4F46E5` | `#A5B4FC` |
| `onPrimary` | `#FFFFFF` | `#312E81` |
| `primaryContainer` | `#E0E7FF` | `#3730A3` |
| `onPrimaryContainer` | `#1E1B4B` | `#E0E7FF` |
| `secondary` | `#14B8A6` | `#5EEAD4` |
| `onSecondary` | `#FFFFFF` | `#134E4A` |
| `secondaryContainer` | `#CCFBF1` | `#0F766E` |
| `onSecondaryContainer` | `#134E4A` | `#CCFBF1` |
| `tertiary` | `#F59E0B` | `#FCD34D` |
| `onTertiary` | `#FFFFFF` | `#78350F` |
| `tertiaryContainer` | `#FEF3C7` | `#B45309` |
| `onTertiaryContainer` | `#78350F` | `#FEF3C7` |
| `error` | `#DC2626` | `#FCA5A5` |
| `onError` | `#FFFFFF` | `#7F1D1D` |
| `errorContainer` | `#FEE2E2` | `#991B1B` |
| `onErrorContainer` | `#7F1D1D` | `#FEE2E2` |
| `background` | `#FAFAFF` | `#121218` |
| `onBackground` | `#1B1B22` | `#E4E1E9` |
| `surface` | `#FAFAFF` | `#121218` |
| `onSurface` | `#1B1B22` | `#E4E1E9` |
| `surfaceVariant` | `#E4E1F0` | `#47464F` |
| `onSurfaceVariant` | `#47464F` | `#C8C5D0` |
| `surfaceContainerLowest` | `#FFFFFF` | `#0D0D12` |
| `surfaceContainerLow` | `#F4F3FA` | `#1C1C24` |
| `surfaceContainer` | `#EEEDF4` | `#20202A` |
| `surfaceContainerHigh` | `#E8E7EF` | `#2A2A35` |
| `surfaceContainerHighest` | `#E2E1E9` | `#35353F` |
| `outline` | `#78767F` | `#918F9A` |
| `outlineVariant` | `#C8C5D0` | `#47464F` |
| `inverseSurface` | `#303036` | `#E4E1E9` |
| `inverseOnSurface` | `#F2F0F7` | `#303036` |
| `inversePrimary` | `#A5B4FC` | `#4F46E5` |
| `scrim` | `#000000` | `#000000` |

Semantyka użycia akcentów:

- **primary (indygo)** — akcje główne, nawigacja, postęp, bąble użytkownika;
- **secondary (teal)** — sukces, poprawne odpowiedzi, status MASTERED, słownictwo;
- **tertiary (bursztyn)** — streak, due/przypomnienia, ostrzeżenia miękkie, status LEARNING;
- **error (czerwień)** — błędy językowe (oryginał), akcje destrukcyjne, walidacja.

Uwaga na kontrast: `tertiary #F59E0B` z białym tekstem jest poniżej AA dla małego
tekstu — na `tertiary` używamy wyłącznie ikon/dużych elementów; tekst bursztynowy
zawsze na `tertiaryContainer` jako `onTertiaryContainer`.

### 3.2 Skala typograficzna M3 — Inter

Font: **Inter** (zmienne osie zredukowane do statycznych wag 400/500/600/700).
Ładowany lokalnie (`res/font/inter_*.ttf`), bez zależności sieciowej.

| Styl M3 | Rozmiar | Line height | Weight | Letter spacing | Użycie |
|---|---|---|---|---|---|
| `displayLarge` | 57sp | 64sp | 400 | -0.25sp | — (rezerwowy) |
| `displayMedium` | 45sp | 52sp | 400 | 0 | — (rezerwowy) |
| `displaySmall` | 36sp | 44sp | 500 | 0 | słowo na fiszce, słowo w treningu wymowy, liczba streak |
| `headlineLarge` | 32sp | 40sp | 600 | 0 | nagłówki onboardingu |
| `headlineMedium` | 28sp | 36sp | 600 | 0 | tytuły ekranów onboardingu, wartości StatTile (duże) |
| `headlineSmall` | 24sp | 32sp | 600 | 0 | pytanie ćwiczenia, tył fiszki, wartości StatTile |
| `titleLarge` | 22sp | 28sp | 600 | 0 | tytuły w TopBar, tytuł hero CTA |
| `titleMedium` | 16sp | 24sp | 600 | 0.15sp | nagłówki sekcji, tytuły kart i list |
| `titleSmall` | 14sp | 20sp | 600 | 0.1sp | podtytuły, nagłówki kompaktowe |
| `bodyLarge` | 16sp | 24sp | 400 | 0.5sp | treść wiadomości (MessageBubble), opisy główne |
| `bodyMedium` | 14sp | 20sp | 400 | 0.25sp | opisy, wyjaśnienia błędów, feedback |
| `bodySmall` | 12sp | 16sp | 400 | 0.4sp | metadane, noty, transkrypcje pomocnicze |
| `labelLarge` | 14sp | 20sp | 500 | 0.1sp | teksty przycisków, etykiety stanu MicButton, nagłówki grup |
| `labelMedium` | 12sp | 16sp | 500 | 0.5sp | labele NavigationBar, podpisy kafli |
| `labelSmall` | 11sp | 16sp | 500 | 0.5sp | badge, chipy pomocnicze, daty due |

Zasady: liczby w timerach i statystykach — `FontFeature "tnum"` (tabular figures);
transkrypcja IPA i klucz API — monospaced fallback (`Roboto Mono`).

### 3.3 Ikony — Material Symbols Rounded

Styl: **Material Symbols Rounded**, waga 400, fill 0 (kontur); wariant fill 1 dla
aktywnej zakładki NavigationBar. Rozmiary: 24dp standard, 20dp w chipach/bąblach,
16dp meta, 28–64dp dekoracyjne.

| Ekran | Ikony (nazwy Material Symbols) |
|---|---|
| Globalne (nawigacja) | `home`, `forum`, `school`, `monitoring`, `arrow_back`, `more_vert`, `chevron_right`, `close`, `check`, `search` |
| SplashScreen | `mic` (logo) |
| WelcomeScreen | `mic`, `insights`, `repeat` (slajdy wartości) |
| LevelSelectionScreen | — (LevelChip tekstowy) |
| GoalsScreen | `work`, `flight`, `school`, `public`, `chat`, `movie` (chipy celów), `check` |
| PermissionsScreen | `mic`, `notifications`, `check_circle`, `lock` |
| ApiSetupScreen | `key`, `visibility`, `visibility_off`, `info`, `check_circle` |
| HomeScreen | `settings`, `local_fire_department`, `mic`, `style`, `quiz`, `history` |
| ConversationListScreen | `search`, `mic`, `forum` (empty), `error` (chip błędów) |
| ConversationScreen | `mic`, `stop_circle`, `keyboard`, `volume_up`, `translate`, `hourglass_top`, `graphic_eq` |
| ConversationSummaryScreen | `emoji_objects`, `check`, `close`, `add_circle` (słówka), `quiz` |
| PracticeHubScreen | `event`, `task_alt`, `quiz`, `style`, `record_voice_over`, `mic`, `psychology` |
| ExercisesScreen | `quiz`, `schedule`, `history`, `play_arrow` |
| ExercisePlayerScreen | `close`, `check_circle`, `cancel`, `mic`, `arrow_forward` |
| VocabularyScreen | `search`, `volume_up`, `style` (empty), `filter_list` |
| VocabularyReviewScreen | `volume_up`, `flip`, `sentiment_very_dissatisfied`…`sentiment_very_satisfied` (opcjonalnie na ocenach) |
| PronunciationScreen | `record_voice_over`, `volume_up`, `mic`, `refresh`, `tips_and_updates` |
| VoiceNotesScreen | `mic`, `play_circle`, `pause_circle`, `delete`, `edit` |
| StatisticsScreen | `local_fire_department`, `timer`, `book_2`, `task_alt`, `auto_awesome`, `trending_up`, `trending_down` |
| SettingsScreen | `person`, `auto_awesome`, `palette`, `notifications`, `database`, `chevron_right` |
| SettingsAiScreen | `smart_toy`, `key`, `record_voice_over`, `speed`, `volume_up`, `graphic_eq` |
| SettingsProfileScreen | `person`, `flag`, `timer` |
| SettingsDataScreen | `upload_file`, `cloud_sync`, `delete_forever`, `warning` |

### 3.4 Komponenty kanoniczne — specyfikacje

Wszystkie komponenty żyją w `core/designsystem` (pakiet
`com.aienglishcoach.core.designsystem.component`).

#### 3.4.1 CoachPrimaryButton

| Właściwość | Wartość |
|---|---|
| Baza | M3 `Button` (filled) |
| Wysokość | 56dp (hero/CTA pełnej szerokości), 40dp (wariant compact) |
| Radius | pełny (pill) |
| Kolory | kontener `primary`, treść `onPrimary` |
| Typografia | `labelLarge` |
| Warianty | `fullWidth` (domyślny), `compact`; opcjonalna ikona leading 18dp |
| Stany | enabled / **disabled** (kontener `onSurface` 12%, treść `onSurface` 38%) / **loading** (`LoadingIndicator` 18dp `onPrimary` zamiast labelu, przycisk nieklikalny) / pressed (state layer `onPrimary` 12% + skala 0.98) |

#### 3.4.2 CoachCard

| Właściwość | Wartość |
|---|---|
| Baza | M3 `Card` (filled/elevated/outlined) |
| Radius | **12dp** (kanon z briefu) |
| Padding wewnętrzny | 16dp |
| Warianty | `filled` (tło `surfaceContainerLow`, elevation 1) / `outlined` (obrys `outlineVariant` 1dp, tło `surface`) / `highlight` (tło `primaryContainer`/`secondaryContainer`/`tertiaryContainer` — karty akcentowe) |
| Stany | default / **clickable** (ripple `onSurface` 12%) / **selected** (obrys `primary` 2dp) / disabled (alpha 38%) |

#### 3.4.3 CoachTopBar

| Właściwość | Wartość |
|---|---|
| Baza | M3 `TopAppBar` (small) / `CenterAlignedTopAppBar` |
| Wysokość | 64dp |
| Kolory | tło `surface`; po scrollu `surfaceContainer` (scrollBehavior pinned) |
| Sloty | `navigationIcon` (`arrow_back`/`close`), `title` (`titleLarge`), `actions` (maks. 2 `IconButton` + menu) |
| Warianty | `root` (bez back, tytuł zakładki), `child` (back), `modal` (close, dla playerów/fiszek) |

#### 3.4.4 MicButton

Najważniejszy komponent aplikacji — przycisk głosowy.

| Właściwość | Wartość |
|---|---|
| Rozmiary | 80dp (Conversation), 72dp (VoiceNotes), 64dp (Pronunciation, Speaking-exercise) |
| Kształt | pełny okrąg (radius pełny — kanon briefu) |
| Ikona | `mic` 32dp (proporcjonalnie do rozmiaru) |
| Stany (kanoniczne) | `idle` / `listening` / `processing` / `speaking` |

Specyfikacja stanów:

| Stan | Kontener | Ikona | Animacja |
|---|---|---|---|
| `idle` | `primaryContainer` | `mic`, `onPrimaryContainer` | brak; delikatny puls skali 1.0→1.03 co 3 s (zachęta) |
| `listening` | `primary` | `mic`, `onPrimary` | **fala głosowa**: 2 rozchodzące się pierścienie (`primary` 30%→0%, skala 1.0→1.6, 1200 ms, staggered 600 ms, infinite) + 5 słupków ekwalizera nad przyciskiem reagujących na amplitudę RMS mikrofonu (wysokość 4–24dp, spring) |
| `processing` | `secondaryContainer` | `hourglass_top`/`more_horiz`, `onSecondaryContainer` | rotujący pierścień progresu 3dp (`secondary`, indeterminate) |
| `speaking` | `secondary` | `graphic_eq`, `onSecondary` | ikona ekwalizera animowana (3 słupki, 600 ms loop); tap = przerwij TTS |

Interakcje: tap w `idle` → start nasłuchu; tap w `listening` → koniec wypowiedzi;
tap w `speaking` → przerwanie TTS i natychmiastowy nasłuch (barge-in). Haptyka:
`CONFIRM` przy starcie, `CLICK` przy stopie. A11y: `contentDescription` per stan
("Zacznij mówić", "Słucham — dotknij, by zakończyć"…), stan ogłaszany przez TalkBack.

#### 3.4.5 MessageBubble

| Właściwość | Wartość |
|---|---|
| Warianty | `user` / `assistant` |
| Maks. szerokość | 80% szerokości ekranu |
| Radius | 16dp; róg "ogonka" 4dp (dół-lewy dla assistant, dół-prawy dla user) |
| Kolory | user: `primary`/`onPrimary`; assistant: `surfaceContainerHigh`/`onSurface` |
| Typografia | `bodyLarge`; timestamp `labelSmall` (`onSurfaceVariant`) |
| Sloty | treść, opcjonalne tłumaczenie (zwijane), akcja `volume_up` (assistant), znacznik niskiej pewności STT (podkreślenie `tertiary`, user) |
| Stany | default / streaming (kursor migający, tylko assistant) / error (obrys `error`, akcja "ponów") |

#### 3.4.6 StatTile

| Właściwość | Wartość |
|---|---|
| Baza | `CoachCard` filled |
| Wymiary | min. 96dp wysokości, elastyczna szerokość (grid 2 kolumny, gap 12dp) |
| Sloty | ikona 24dp w kółku 40dp (kontener roli akcentu), wartość `headlineSmall`–`headlineMedium`, etykieta `labelMedium` (`onSurfaceVariant`), opcjonalna delta `labelSmall` (↑ `secondary` / ↓ `error`), opcjonalna akcja `TextButton` |
| Stany | default / clickable / loading (skeleton shimmer) |

#### 3.4.7 ProgressRing

| Właściwość | Wartość |
|---|---|
| Rozmiary | 48 / 64 / 72 / 120dp |
| Grubość toru | 6dp (8dp dla 120dp), zaokrąglone końce |
| Kolory | tor `surfaceVariant`; progres `primary` (domyślnie), `secondary` (cel osiągnięty / score ≥ 80), `tertiary` (60–79), `error` (< 60) |
| Zawartość | slot centralny (wartość `titleMedium`/`headlineMedium` + podpis `labelSmall`) |
| Animacja | progres animowany 600 ms emphasized decelerate od 0 przy wejściu |

#### 3.4.8 LevelChip

| Właściwość | Wartość |
|---|---|
| Wymiary | wysokość 32dp, radius pełny, padding poziomy 12dp |
| Treść | poziom (A2/B1/B2/C1) `labelLarge` |
| Warianty | `outlined` (obrys `outline`, tekst `onSurfaceVariant`) / `selected` (tło `primary`, tekst `onPrimary`) / `display` (tło `primaryContainer`, tekst `onPrimaryContainer` — nieinteraktywny, np. w Settings) |

#### 3.4.9 EmptyState

| Właściwość | Wartość |
|---|---|
| Layout | kolumna wyśrodkowana, padding 32dp |
| Sloty | ikona 64dp (`onSurfaceVariant` 60%), tytuł `titleMedium`, opis `bodyMedium` (`onSurfaceVariant`, maks. 2 linie), opcjonalny `CoachPrimaryButton` compact |
| Użycie | listy bez danych (rozmowy, ćwiczenia, słówka, notatki) |

#### 3.4.10 LoadingIndicator

| Właściwość | Wartość |
|---|---|
| Warianty | `circular` (M3 `CircularProgressIndicator`, 4dp, `primary`; rozmiary 18/32/48dp) / `dots` (3 kropki 8dp, puls sekwencyjny 900 ms — splash, "AI myśli") / `skeleton` (shimmer `surfaceContainerHigh`→`surfaceContainerHighest`, 1200 ms) |
| Zasada | pełnoekranowy loader tylko na Splash; w treści zawsze skeleton lub inline |

#### 3.4.11 ErrorBanner

| Właściwość | Wartość |
|---|---|
| Layout | pasek pełnej szerokości, radius 12dp, padding 12/16dp, tło `errorContainer` |
| Sloty | ikona `error` 24dp (`onErrorContainer`), komunikat `bodyMedium` (`onErrorContainer`), opcjonalna akcja `TextButton` ("Ponów", "Ustawienia") |
| Warianty | `inline` (w treści ekranu) / `top` (pod TopBarem, slide-in 250 ms) |
| Użycie | brak sieci, błąd OpenAI (401/429/5xx), brak uprawnienia mikrofonu |

### 3.5 Grid i spacing

Siatka bazowa: **4dp**. Tokeny (kanon briefu):

| Token | Wartość | Użycie |
|---|---|---|
| `space-1` | 4dp | mikroprzerwy (ikona↔tekst w chipie) |
| `space-2` | 8dp | gap chipów, odstęp ikona↔label, wewnętrzne drobne |
| `space-3` | 12dp | gap kafli/kart w gridzie, padding chipów |
| `space-4` | 16dp | padding boczny ekranu, padding kart, odstęp między kartami |
| `space-6` | 24dp | odstęp między sekcjami ekranu |
| `space-8` | 32dp | duże separacje (hero, empty state) |

Corner radius (kanon briefu): **12dp** karty, **16dp** arkusze (bottom sheets,
duże karty hero, fiszki), **pełny** — FAB, przyciski, MicButton, chipy.
Layout: 1 kolumna treści; gridy 2-kolumnowe (StatTile, kafle huba) z gap 12dp.
Safe area: edge-to-edge, insets przez `WindowInsets`.

### 3.6 Elevation (poziomy M3)

| Poziom | dp | Użycie |
|---|---|---|
| Level 0 | 0dp | tło, powierzchnie płaskie, outlined cards |
| Level 1 | 1dp | `CoachCard` filled, `StatTile` |
| Level 2 | 3dp | FAB (spoczynek), fiszka, `NavigationBar` |
| Level 3 | 6dp | FAB (pressed), `ModalBottomSheet`, dialogi, menu |
| Level 4 | 8dp | — (rezerwowy) |
| Level 5 | 12dp | — (rezerwowy) |

W dark mode elevation wyrażana **kolorem powierzchni** (surfaceContainer*
zamiast cieni) — zgodnie z M3 tonal elevation; cienie tylko dla FAB i dialogów.

### 3.7 Motion

Zgodnie z briefem: M3 motion, emphasized easing, 200–400 ms.

| Token | Czas | Easing | Użycie |
|---|---|---|---|
| `motion-short` | 200 ms | standard (0.2, 0, 0, 1) | ripple, crossfade labeli, checkmarki |
| `motion-medium` | 300 ms | emphasized decelerate (0.05, 0.7, 0.1, 1) | wejścia paneli, slide-up feedbacku, bottom sheet |
| `motion-long` | 400 ms | emphasized (0.2, 0, 0, 1) | przejścia ekranów, obrót fiszki, morfing FAB |
| `motion-exit` | 200 ms | emphasized accelerate (0.3, 0, 0.8, 0.15) | wyjścia elementów |

Przejścia nawigacji: zakładki — fade-through (250 ms); push child —
shared-axis X (400 ms); modalne (player, review, rozmowa) — slide-up (300 ms).

Kluczowe animacje:

1. **Fala głosowa MicButton (listening)** — dwa pierścienie z centrum przycisku:
   skala 1.0→1.6, alpha 30%→0%, kolor `primary`, czas 1200 ms, linear-out,
   opóźnienie drugiego 600 ms, infinite; równolegle ekwalizer 5 słupków
   (szer. 4dp, gap 4dp, wysokość 4–24dp mapowana ze znormalizowanej amplitudy RMS,
   spring damping 0.6). Wymóg a11y: przy `reduce motion` pierścienie zastąpione
   statycznym obrysem `primary` 2dp.
2. **Przejście stanów MicButton** — kolor kontenera i ikona: crossfade 200 ms;
   skala "pop" 1.0→1.08→1.0 (spring) przy zmianie stanu.
3. **Obrót fiszki** — rotationY 0→180°, 400 ms emphasized, cameraDistance 12×density.
4. **ProgressRing** — sweep 0→wartość, 600 ms emphasized decelerate.
5. **Streak celebration** — po osiągnięciu celu dziennego: skala ringu 1.0→1.15→1.0
   + zmiana koloru na `secondary` (500 ms).
6. **Streaming tekstu AI** — pojawianie się tekstu + migający kursor (530 ms loop).
7. **Skeleton shimmer** — gradient przesuwny 1200 ms linear infinite.

### 3.8 Dark Mode i Light Mode — zasady

1. Motywy: **Light / Dark / System** (wybór w SettingsScreen, zapis w DataStore
   `theme`); domyślnie System.
2. Mapowanie wyłącznie przez **role M3** (tabela 3.1) — komponenty nigdy nie
   używają hexów bezpośrednio; brak hardkodowanych `Color(0xFF…)` poza
   `core/designsystem/theme/Color.kt`.
3. Dark nie jest inwersją: tła `#121218`/`#1C1C24` (kanon), akcenty rozjaśnione
   (primary `#A5B4FC`), kontenery przyciemnione; kontrast tekstów AA na wszystkich
   parach rola/on-rola.
4. Elevation w dark = tonal (jaśniejsze surfaceContainer*), cienie zminimalizowane.
5. Ilustracje/wykresy: warianty kolorów per motyw (gradienty wykresów z alpha,
   nie stałe hexy).
6. Status bar / navigation bar: transparentne, ikony systemowe dopasowane do
   luminancji tła (`isAppearanceLightStatusBars`).
7. Testy: każdy ekran w preview Compose w obu motywach
   (`@PreviewLightDark` w `core/designsystem` i feature'ach).

### 3.9 Dynamic color

- Android 12+ (API 31+): opcjonalny przełącznik "Kolory dynamiczne
  (Material You)" w dialogu motywu (SettingsScreen); **domyślnie wyłączony** —
  paleta marki (indygo/teal/bursztyn) jest domyślna.
- Implementacja: `dynamicLightColorScheme(context)` / `dynamicDarkColorScheme(context)`
  w `CoachTheme`, fallback do palety z tabeli 3.1 na API < 31 lub przy wyłączonym
  przełączniku.
- Kolory semantyczne poza schematem (kategorie błędów, statusy słówek, score
  wymowy) zdefiniowane jako `LocalCoachColors` (CompositionLocal) i **nie**
  podlegają dynamic color — utrzymują spójność znaczeniową (sukces = teal,
  due = bursztyn, błąd = czerwień).
- QA: weryfikacja kontrastu ról dynamicznych nie jest wymagana (gwarantuje ją
  system), ale ekrany z akcentami stałymi testujemy na 2–3 tapetach skrajnych.
