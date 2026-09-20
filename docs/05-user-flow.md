# ETAP 5 — User Flow

> Dokument bazuje na [00-brief-decyzje-projektowe.md](./00-brief-decyzje-projektowe.md)
> oraz [04-architektura-informacji.md](./04-architektura-informacji.md). Nazwy ekranów
> i routes są kanoniczne.

Konwencja: każdy przepływ ma diagram mermaid + opis kroków z wariantami
**happy path** / **edge case**. Stany MicButton (idle/listening/processing/speaking)
opisane szczegółowo w [06-ux.md](./06-ux.md).

---

## F1. Przepływ główny — pierwsze uruchomienie → pełna pętla nauki

Najważniejszy przepływ produktu: od instalacji do zamknięcia pełnej pętli
rozmowa → analiza → ćwiczenia → powtórki → statystyki → kolejna sesja.

```mermaid
flowchart TB
    A([Pierwsze uruchomienie]) --> B[SplashScreen<br/>splash]
    B -->|onboardingCompleted = false| C[WelcomeScreen<br/>onboarding/welcome]
    C -->|Zaczynamy| D[LevelSelectionScreen<br/>onboarding/level]
    D -->|wybór A2–C1| E[GoalsScreen<br/>onboarding/goals]
    E -->|cele + minuty dziennie| F[PermissionsScreen<br/>onboarding/permissions]
    F -->|mikrofon + powiadomienia| G[ApiSetupScreen<br/>onboarding/api]
    G -->|klucz zweryfikowany| H[HomeScreen<br/>home]
    H -->|Rozpocznij rozmowę| I[ConversationScreen<br/>conversation/id]
    I -->|pętla: user mówi → AI odpowiada| I
    I -->|Zakończ rozmowę| J[ConversationSummaryScreen<br/>conversation/id/summary]
    J -->|Ćwicz błędy| K[ExercisesScreen<br/>practice/exercises]
    K -->|Rozpocznij sesję| L[ExercisePlayerScreen<br/>practice/exercises/session]
    L -->|seria ukończona| M[Wynik sesji]
    M -->|następnego dnia: powiadomienie| N[VocabularyReviewScreen<br/>practice/vocabulary/review]
    N -->|fiszki ocenione| O[StatisticsScreen<br/>statistics]
    O -->|motywacja: streak +1| P[HomeScreen]
    P -->|kolejna sesja — AI pamięta ucznia| I
```

Kroki:

1. **SplashScreen** — odczyt DataStore (< 500 ms). `onboardingCompleted = false` →
   onboarding; splash usuwany ze stacku.
2. **WelcomeScreen** — 2–3 plansze wartości produktu (rozmowy głosowe, AI pamięta,
   spersonalizowane powtórki). CTA „Zaczynamy".
3. **LevelSelectionScreen** — samoocena poziomu: 4 karty `LevelChip` (A2/B1/B2/C1)
   z opisem „co potrafisz" po polsku. Wybór zapisywany od razu do DataStore.
4. **GoalsScreen** — multi-select celów (praca, podróże, egzaminy, small talk,
   emigracja) + slider celu dziennego (5/10/15/20 min, domyślnie 10).
5. **PermissionsScreen** — wyjaśnienie *zanim* pojawi się systemowy dialog:
   mikrofon (wymagany do rozmów), powiadomienia (opcjonalne — przypomnienia o powtórkach).
   Edge case → **F3** (odmowa mikrofonu). Odmowa powiadomień nie blokuje przejścia dalej.
6. **ApiSetupScreen** — pole klucza `sk-...`, link „Skąd wziąć klucz?", przycisk
   [Zweryfikuj i zapisz] wykonuje testowy request; sukces → zapis w szyfrowanym
   storage, `onboardingCompleted = true`, nawigacja do `home` z czyszczeniem stacku.
   Edge case: klucz niepoprawny (401) → inline error „Klucz wygląda na nieprawidłowy";
   brak sieci → „Sprawdź połączenie i spróbuj ponownie".
7. **HomeScreen** (stan zerowy) — streak 0, pierścień celu dziennego 0/10 min,
   duży CTA „Rozpocznij pierwszą rozmowę", sekcja powtórek pusta („Pojawią się po
   pierwszej rozmowie").
8. **ConversationScreen** — AI otwiera rozmowę głosowo (scenariusz dobrany do poziomu
   i celów z onboardingu). Pętla: MicButton `listening` → transkrypcja na żywo →
   `processing` → odpowiedź AI `speaking` → auto-powrót do `listening`.
   Wszystkie wiadomości zapisywane do Room na bieżąco.
9. **Zakończenie** — użytkownik: ✕ / back → dialog → [Zakończ i analizuj].
   Analiza AI (błędy, słówka, feedback) → **ConversationSummaryScreen**: ocena rozmowy,
   lista błędów (kategoria, oryginał, poprawka, wyjaśnienie), nowe słówka, CTA.
10. **ExercisesScreen → ExercisePlayerScreen** — ćwiczenia wygenerowane z błędów
    (FILL_GAP / MULTIPLE_CHOICE / TRANSLATION / SPEAKING); każda odpowiedź oceniana,
    SM-2 wyznacza `dueAt`. Wynik sesji: X/Y poprawnych, następna powtórka „jutro".
11. **Następny dzień** — `RevisionSchedulerWorker` wysyła powiadomienie → deep link
    do **VocabularyReviewScreen** (przepływ **F6**). Fiszki: swipe/samoocena 0–5.
12. **StatisticsScreen** — streak 2, minuty rozmów, nowe słówka, skuteczność ćwiczeń.
13. **Kolejna sesja** — `MemoryManager` wstrzykuje do promptu pamięć (cele, słabości,
    fakty) → rozmowa jest lepiej spersonalizowana. Pętla się zamyka.

---

## F2. Powracający użytkownik (codzienna sesja)

```mermaid
flowchart TB
    A([Uruchomienie aplikacji]) --> B[SplashScreen]
    B -->|onboardingCompleted = true| C[HomeScreen]
    C --> D{Co widzi?}
    D -->|streak zagrożony| E[Karta: Podtrzymaj streak — 10 min rozmowy]
    D -->|powtórki due > 0| F[Karta: Masz 12 powtórek]
    D -->|cel dzienny niezrealizowany| G[ProgressRing 0/10 min]
    E -->|Rozpocznij rozmowę| H[ConversationScreen]
    F -->|Powtórz teraz| I[VocabularyReviewScreen / ExercisesScreen]
    H --> J[ConversationSummaryScreen]
    J --> C2[HomeScreen — cel zaliczony, streak +1]
    I --> C2
```

- **Happy path:** splash → home w < 2 s (cold start, wymaganie niefunkcjonalne);
  Home priorytetyzuje **jedną** najważniejszą akcję dnia (rozmowa, jeśli cel
  niezrealizowany; powtórki, jeśli rozmowa odbyta).
- **Edge case — długa nieobecność (7+ dni):** karta „Witaj z powrotem" z łagodnym
  komunikatem (bez karania), skumulowane powtórki ograniczone do sensownej porcji
  (max 20 na sesję), reszta przesunięta.
- **Edge case — klucz API przestał działać:** pierwsza akcja AI kończy się 401 →
  banner na Home „Problem z kluczem API — [Napraw]" → `settings/ai`.

---

## F3. Odmowa uprawnienia mikrofonu

```mermaid
flowchart TB
    A[PermissionsScreen<br/>onboarding/permissions] --> B[Systemowy dialog RECORD_AUDIO]
    B -->|Zezwól| C[Prośba o powiadomienia] --> D[ApiSetupScreen]
    B -->|Odmów| E[Ekran wyjaśnienia:<br/>Bez mikrofonu nie porozmawiasz z AI]
    E -->|Spróbuj ponownie| B2{shouldShowRationale?}
    B2 -->|tak| B
    B2 -->|nie — trwała odmowa| F[Karta: Włącz mikrofon w ustawieniach systemu<br/>przycisk → App Settings]
    F -->|powrót z ustawień, brak zgody| G[Kontynuuj mimo to]
    E -->|Kontynuuj mimo to| G
    G --> D
    D -.-> H[HomeScreen — CTA rozmowy z ikoną 🎙 off]
    H -->|Rozpocznij rozmowę| I[Ponowna prośba lub kierowanie do ustawień]
    I -->|zgoda| J[ConversationScreen]
    I -->|dalej brak| K[Propozycja trybu pisania<br/>rozmowa tekstowa]
```

- **Happy path:** zgoda za pierwszym razem, przejście płynne.
- **Edge case — pierwsza odmowa:** ekran nieblokujący z uzasadnieniem; ponowna prośba
  możliwa (system pokaże dialog dopóki nie wybrano „nie pytaj ponownie" / drugiej odmowy).
- **Edge case — trwała odmowa:** przycisk otwiera systemowe ustawienia aplikacji;
  onboarding **nie jest zablokowany** — „Kontynuuj mimo to" pozwala dokończyć konfigurację.
- **Zabezpieczenie w produkcie:** każdy punkt wejścia do rozmowy i nagrywania
  (ConversationScreen, PronunciationScreen, VoiceNotesScreen) sprawdza uprawnienie
  tuż przed startem; fallbackiem rozmowy jest **tryb pisania** (patrz 06-ux.md §7).

---

## F4. Brak internetu w trakcie rozmowy

```mermaid
flowchart TB
    A[ConversationScreen — rozmowa trwa] --> B[Użytkownik kończy wypowiedź<br/>STT zwraca transkrypcję]
    B --> C[Wywołanie OpenAI]
    C -->|timeout / IOException| D[MicButton wraca do idle<br/>ErrorBanner: Brak połączenia]
    D --> E{Auto-retry z backoff<br/>1 s → 2 s → 4 s}
    E -->|sieć wróciła| F[Odpowiedź AI — rozmowa kontynuowana]
    E -->|3 nieudane próby| G[Karta w wątku: Nie mogę się połączyć<br/>przyciski: Ponów / Zakończ rozmowę]
    G -->|Ponów| C
    G -->|Zakończ rozmowę| H{Analiza możliwa?}
    H -->|offline| I[ConversationSummaryScreen — stan:<br/>Analiza czeka na połączenie<br/>transkrypcja dostępna]
    I -.->|sieć wróciła / WorkManager| J[Analiza uzupełniona<br/>opcjonalne powiadomienie: Analiza gotowa]
```

- **Kluczowa zasada (offline-first):** wypowiedź użytkownika **nigdy nie ginie** —
  transkrypcja jest w Room zanim ruszy request; retry wysyła tę samą wiadomość.
- STT (`SpeechRecognizer` on-device) i TTS działają offline — zawodzi tylko warstwa AI.
- Analiza po rozmowie kolejkowana przez WorkManager (constraint `NetworkType.CONNECTED`);
  podsumowanie pokazuje transkrypcję od razu, sekcje AI (błędy, feedback) w stanie
  „oczekuje na połączenie".
- **Edge case — brak sieci przy próbie startu rozmowy:** CTA na Home/Rozmowach pokazuje
  od razu komunikat „Rozmowa wymaga internetu" (bez wchodzenia na ekran); dostępne
  offline pozostają: historia, ćwiczenia, fiszki, notatki, statystyki.

---

## F5. Przerwana rozmowa (telefon dzwoni / aplikacja w tle)

```mermaid
flowchart TB
    A[ConversationScreen — MicButton listening/speaking] --> B([Przychodzące połączenie<br/>lub przejście do tła — onStop])
    B --> C[Auto-pauza:<br/>stop STT, stop TTS,<br/>stan rozmowy PAUSED zapisany w Room]
    C --> D([Powrót do aplikacji])
    D --> E[ConversationScreen — stan wstrzymania:<br/>Rozmowa wstrzymana + ostatnie wiadomości]
    E -->|Wznów| F[AI: krótkie nawiązanie<br/>where were we... → listening]
    E -->|Zakończ| G[Dialog → analiza → Summary]
    D -.->|powrót po > 30 min| H[Dialog: Wznowić wczorajszą rozmowę<br/>czy zakończyć i przeanalizować?]
    B -.->|proces ubity przez system| I[Ponowne uruchomienie → Home<br/>karta: Masz niedokończoną rozmowę → Wznów/Zakończ]
```

- **Happy path:** pauza automatyczna i bezstratna — audio focus oddany (`AudioFocusRequest`),
  częściowa transkrypcja z aktywnego nasłuchu odrzucana (użytkownik powtórzy myśl),
  wszystkie zakończone wiadomości są już w Room.
- **Edge case — długa przerwa (> 30 min):** przy powrocie dialog wyboru: wznowienie
  (AI dostaje kontekst z historii) albo zakończenie z analizą.
- **Edge case — process death:** rozmowa ze statusem innym niż zakończona wykrywana
  przy starcie; Home pokazuje kartę wznowienia. Rozmowy porzucone > 24 h są
  automatycznie zamykane i analizowane w tle.

---

## F6. Powtórka z powiadomienia (deep link)

```mermaid
flowchart TB
    A([RevisionSchedulerWorker — codziennie rano]) --> B{Są powtórki due?}
    B -->|nie| Z([brak powiadomienia])
    B -->|tak| C[Powiadomienie: Masz 8 słówek i 4 ćwiczenia do powtórki]
    C -->|tap: fiszki| D[Deep link aienglishcoach://practice/vocabulary/review]
    C -->|tap: ćwiczenia| E[Deep link aienglishcoach://practice/exercises?filter=due]
    D --> F{App odblokowana?}
    F -->|zimny start| G[Splash → syntetyczny back stack<br/>home → practice → vocabulary → review]
    F -->|app w tle| H[Nawigacja bezpośrednia na review]
    G --> I[VocabularyReviewScreen — sesja fiszek]
    H --> I
    I -->|wszystkie ocenione| J[Wynik sesji: 8/8, następne powtórki: pojutrze]
    J -->|Gotowe / back| K[VocabularyScreen → practice → home]
```

- **Happy path:** tap w powiadomienie → sesja fiszek w < 2 s; po sesji back prowadzi
  w głąb aplikacji (syntetyczny stack), nie na launcher — naturalna okazja do
  kontynuowania nauki.
- **Edge case — powtórki zrobione wcześniej w aplikacji:** deep link trafia na
  VocabularyReviewScreen w stanie pustym „Wszystko powtórzone ✓" z CTA „Wróć do nauki".
- **Edge case — onboarding nieukończony:** deep link ignorowany, start od splash → onboarding.
- **Edge case — brak zgody na powiadomienia:** worker działa (wyznacza due), ale nie
  powiadamia; powtórki widoczne na Home i w badge zakładki Nauka.

---

## F7. Dodanie słówka ręcznie

```mermaid
flowchart TB
    A[VocabularyScreen<br/>practice/vocabulary] -->|FAB +| B[Bottom sheet: Nowe słówko]
    B --> C[Pole: słowo/fraza EN]
    C -->|wpisane, jest sieć| D[AI podpowiada: tłumaczenie,<br/>definicję, przykład — do edycji]
    C -->|offline| E[Pola tłumaczenie/przykład<br/>do ręcznego wypełnienia]
    D --> F[Zapisz]
    E --> F
    F --> G{Duplikat?}
    G -->|tak| H[Info: Masz już to słówko<br/>Zobacz / Zapisz mimo to]
    G -->|nie| I[Zapis do Room: status NEW,<br/>dueAt = jutro, snackbar Dodano]
    I --> J[Słówko widoczne na liście<br/>i w kolejnej sesji fiszek]
```

- **Happy path:** wpisanie słowa → autouzupełnienie AI (debounce ~600 ms) → korekta →
  zapis; słówko wchodzi w cykl SRS od następnego dnia.
- **Edge case — offline:** formularz w pełni ręczny (zapis lokalny działa zawsze);
  brak kolejkowania wzbogacania AI w MVP — użytkownik może później edytować wpis.
- **Edge case — duplikat:** łagodne ostrzeżenie z podglądem istniejącego wpisu.
- **Drugi punkt wejścia:** long-press słowa w `MessageBubble` podczas rozmowy /
  w podsumowaniu → „Zapisz słówko" → ten sam bottom sheet z prewypełnionym słowem
  i `sourceConversationId`.

---

## F8. Trening wymowy

```mermaid
flowchart TB
    A[PracticeHubScreen] --> B[PronunciationScreen<br/>practice/pronunciation]
    B --> C[Lista słów do treningu:<br/>z błędów PRONUNCIATION + trudne słówka]
    C -->|wybór słowa| D[Karta słowa: transkrypcja IPA<br/>przycisk Posłuchaj TTS]
    D -->|Posłuchaj| E[TTS wymawia wzorzec]
    E --> F[Nagraj — MicButton listening]
    F --> G[STT rozpoznaje wypowiedź]
    G --> H{Analiza: recognizedText vs expectedText<br/>+ confidence STT + feedback AI}
    H -->|score ≥ 80| I[Wynik zielony: Świetnie! 92/100<br/>zapis PronunciationResult]
    H -->|score < 80| J[Wynik + feedback AI:<br/>co poprawić, np. akcent na 2. sylabę]
    J -->|Spróbuj ponownie| F
    I --> K{Kolejne słowo?}
    J -->|Następne słowo| K
    K -->|tak| D
    K -->|nie| L[Podsumowanie treningu: średni wynik,<br/>najlepsze/najtrudniejsze słowa]
```

- **Happy path:** pętla posłuchaj → nagraj → wynik; wyniki zapisywane do
  `pronunciation_results`, średnia zasila `daily_stats.pronunciationAvg`.
- **Edge case — STT nic nie rozpoznał (cisza/szum):** komunikat „Nie usłyszałem —
  spróbuj bliżej mikrofonu", bez zapisu wyniku, bez naliczenia próby.
- **Edge case — offline:** odsłuch TTS i rozpoznanie STT działają (on-device),
  wynik liczony lokalnie z porównania tekstów i confidence; tekstowy feedback AI
  oznaczony „dostępny online" i pomijany.
- **Edge case — brak uprawnienia do mikrofonu:** przycisk Nagraj uruchamia przepływ F3.

---

## F9. Nagranie notatki głosowej

```mermaid
flowchart TB
    A[PracticeHubScreen] --> B[VoiceNotesScreen<br/>practice/notes]
    B -->|przycisk Nagraj| C{Uprawnienie mikrofonu?}
    C -->|brak| P[Przepływ F3]
    C -->|jest| D[Nagrywanie: timer + fala głosowa<br/>przyciski: Pauza / Stop]
    D -->|Stop| E[Zapis pliku audio do storage aplikacji]
    E --> F[Transkrypcja SpeechRecognizer]
    F -->|sukces| G[Notatka na liście: tytuł auto<br/>z pierwszych słów + transkrypcja]
    F -->|niepowodzenie| H[Notatka bez transkrypcji<br/>akcja: Transkrybuj ponownie]
    G --> I[Tap → bottom sheet: odtwarzanie,<br/>pełna transkrypcja, edycja tytułu]
    I -->|long-press słowa w transkrypcji| J[Zapisz słówko — przepływ F7]
```

- **Happy path:** nagranie → automatyczna transkrypcja → notatka z tytułem i tekstem;
  wszystko działa w pełni offline.
- **Edge case — bardzo długie nagranie:** miękki limit 5 min z ostrzeżeniem przy 4:30;
  po limicie auto-stop i zapis.
- **Edge case — przerwanie (telefon):** nagrywanie zatrzymane, dotychczasowy materiał
  zapisany jako notatka (nic nie ginie).
- **Prywatność (zgodnie z briefem):** w rozmowach audio jest kasowane po transkrypcji;
  notatki głosowe to świadomy wyjątek — plik audio pozostaje, bo odtwarzanie jest
  funkcją notatki. Kasowanie notatki usuwa plik.

---

## F10. Zmiana poziomu w ustawieniach

```mermaid
flowchart TB
    A[HomeScreen — ikona ⚙] --> B[SettingsScreen<br/>settings]
    B --> C[SettingsProfileScreen<br/>settings/profile]
    C --> D[Sekcja Poziom: LevelChip A2–C1<br/>zaznaczony obecny]
    D -->|wybór innego poziomu| E{Zmiana o więcej niż 1 stopień?}
    E -->|nie| F[Zapis do DataStore natychmiast<br/>snackbar: Poziom zmieniony na B2]
    E -->|tak, np. A2 → C1| G[Dialog: Duża zmiana poziomu —<br/>rozmowy będą znacznie trudniejsze. Kontynuować?]
    G -->|Tak| F
    G -->|Anuluj| D
    F --> H[Efekty od kolejnej rozmowy:<br/>prompt systemowy AI, dobór scenariuszy,<br/>trudność generowanych ćwiczeń]
    F --> I[AiMemory: wpis PROGRESS<br/>o zmianie poziomu]
```

- **Happy path:** zmiana o jeden stopień bez tarcia, zapis natychmiastowy (brak
  przycisku „Zapisz" — wzorzec ustawień Android).
- **Edge case — skrajna zmiana:** dialog ostrzegawczy zapobiega przypadkowej frustracji.
- Zmiana **nie modyfikuje wstecz** danych historycznych (statystyki, `dueAt` powtórek
  pozostają); wpływa tylko na przyszłe treści AI.
- Na tym samym ekranie analogicznie edytowalne są cele nauki i cel dzienny.

---

## F11. Wyczerpanie limitu API (429 / brak środków)

```mermaid
flowchart TB
    A[Dowolne wywołanie OpenAI<br/>rozmowa / analiza / generator] --> B{Odpowiedź API}
    B -->|429 rate limit| C[Retry z backoff 2 s → 4 s → 8 s<br/>UI: processing z komunikatem Chwileczkę...]
    C -->|sukces| D[Kontynuacja normalna]
    C -->|dalej 429| E[ErrorBanner: Limit zapytań OpenAI<br/>Spróbuj za kilka minut]
    B -->|429 insufficient_quota / 402| F[Stan blokujący AI:<br/>Wyczerpany limit konta OpenAI]
    F --> G[Bottom sheet wyjaśnienia:<br/>co się stało, jak doładować konto,<br/>link do platform.openai.com]
    G -->|Sprawdź klucz / konto| H[SettingsAiScreen<br/>settings/ai]
    G -->|Zamknij| I{Kontekst}
    I -->|w trakcie rozmowy| J[Rozmowa wstrzymana — transkrypcja<br/>zapisana; Zakończ: analiza do kolejki]
    I -->|analiza po rozmowie| K[Summary: transkrypcja OK,<br/>sekcje AI oczekują — retry z WorkManagerem]
    I -->|poza rozmową| L[Tryb offline-first:<br/>ćwiczenia, fiszki, statystyki działają]
    H -->|nowy klucz zweryfikowany| M[Odblokowanie AI<br/>kolejka analiz rusza]
```

- **Kluczowa zasada:** wyczerpanie limitu **degraduje, nie zabija** aplikację —
  wszystko poza wywołaniami AI działa (Room jest źródłem prawdy).
- Rozróżnienie błędów: chwilowy rate limit (auto-retry, banner informacyjny) vs
  wyczerpana kwota (stan trwały do interwencji użytkownika, bottom sheet z wyjaśnieniem).
- Zaległe analizy rozmów czekają w kolejce WorkManagera i wykonują się po naprawie.
- Na Home utrzymuje się banner „Funkcje AI wstrzymane — [Sprawdź ustawienia]"
  dopóki pierwszy request po zmianie klucza się nie powiedzie.

---

## F12. Mapa pokrycia przepływów

| Przepływ | Ekrany kluczowe | Moduły (brief §2) |
|---|---|---|
| F1 główny | splash → onboarding/* → home → conversation → summary → exercises → review → statistics | wszystkie |
| F2 powracający | splash, home, conversation, review | conversation, revision, statistics |
| F3 mikrofon | onboarding/permissions, conversation | conversation, pronunciation, voicenotes |
| F4 offline w rozmowie | conversation, summary | conversation, ai, sync |
| F5 przerwana rozmowa | conversation, home | conversation, audio |
| F6 powtórka z powiadomienia | review, exercises (deep link) | revision, vocabulary, exercises |
| F7 słówko ręcznie | vocabulary (+ MessageBubble) | vocabulary, ai |
| F8 wymowa | pronunciation | pronunciation, audio, ai |
| F9 notatka głosowa | notes | voicenotes, audio |
| F10 zmiana poziomu | settings/profile | settings, memory, ai |
| F11 limit API | conversation, summary, settings/ai | ai, settings |
