# AI English Coach — ETAP 1: Analiza produktu

> Dokument zgodny z briefem decyzji projektowych (`00-brief-decyzje-projektowe.md`).
> Nazwy modułów, ekranów i tabel odpowiadają kanonicznym nazwom z briefu.

## 1. Cel aplikacji

**AI English Coach** to natywna aplikacja Android (minSdk 26, targetSdk 35) do nauki
**mówionego** angielskiego przez codzienne, realistyczne rozmowy głosowe z tutorem AI —
dla polskich dorosłych na poziomach **A2–C1**, którzy nie mają czasu lub odwagi na
konwersacje z lektorem.

Cel produktowy: przekuć krótkie, regularne sesje mówienia w mierzalny postęp dzięki pętli:

**rozmowa głosowa → analiza AI (błędy, słownictwo, feedback) → ćwiczenia i powtórki (SRS)
→ statystyki i pamięć długoterminowa → kolejna, lepiej spersonalizowana rozmowa.**

Wyróżniki:

- Tutor AI **pamięta ucznia** (cele, błędy, słabości, kontekst życiowy) — pamięć
  długoterminowa z embeddings + RAG (moduł *memory*, tabela `ai_memories`).
- Każda rozmowa jest analizowana: błędy trafiają do `user_errors`, nowe słówka do
  `vocabulary_items`, a z błędów generowane są spersonalizowane ćwiczenia (`exercises`).
- **Offline-first**: historia rozmów, słownictwo, ćwiczenia, statystyki i notatki
  dostępne bez sieci (Room = źródło prawdy); internet potrzebny tylko do wywołań AI.

## 2. Grupa użytkowników — persony

### Persona 1: Michał, 32 — zapracowany specjalista IT (B1)

| Atrybut | Opis |
|---|---|
| Kontekst | Backend developer w polskiej firmie wchodzącej na rynki zagraniczne; daily stand-upy i code review po angielsku od przyszłego kwartału |
| Poziom | B1 — czyta dokumentację płynnie, ale „blokuje się" mówiąc; silny polski akcent, kalki językowe |
| Motywacja | Nie chcieć wypadać słabo na spotkaniach; awans wymaga swobodnej komunikacji |
| Bariery | Brak czasu na kurs (praca + rodzina); wstyd przed mówieniem przy lektorze |
| Wzorzec użycia | 10–15 min dziennie: rano w aucie/pociągu (scenariusz „small talk", „stand-up meeting"), wieczorem szybkie powtórki SRS |
| Kluczowe funkcje | scenariusze zawodowe, analiza błędów gramatycznych, streak i cel dzienny (motywacja), ćwiczenia z własnych błędów |

### Persona 2: Anna, 45 — osoba przed emigracją (A2)

| Atrybut | Opis |
|---|---|
| Kontekst | Za 6 miesięcy wyjeżdża do męża do Irlandii; musi ogarnąć urząd, lekarza, sklep, pracę dorywczą |
| Poziom | A2 — zna podstawy ze szkoły, panicznie boi się mówić; potrzebuje tłumaczeń na polski |
| Motywacja | Konkretny, terminowy cel życiowy — samodzielność za granicą |
| Bariery | Niska pewność siebie, niewielkie doświadczenie z aplikacjami, ograniczony budżet |
| Wzorzec użycia | 20–30 min dziennie w domu: scenariusze sytuacyjne („u lekarza", „w urzędzie", „rozmowa o pracę"), wolniejsze tempo TTS, tłumaczenia wiadomości, fiszki słownictwa |
| Kluczowe funkcje | scenariusze codzienne, tłumaczenie na polski (`messages.translation`), regulowane tempo głosu TTS, cierpliwy tutor dopasowany do poziomu A2, trening wymowy |

### Persona 3: Karolina, 38 — menedżerka szlifująca fluency (C1)

| Atrybut | Opis |
|---|---|
| Kontekst | Head of Marketing w korporacji; prezentuje zarządowi po angielsku, negocjuje z partnerami |
| Poziom | C1 — komunikuje się swobodnie, ale chce brzmieć naturalnie: idiomy, precyzja, rejestr formalny, eliminacja drobnych błędów |
| Motywacja | Profesjonalny wizerunek; „ostatnie 10%" płynności, którego kursy grupowe nie dają |
| Bariery | Zero tolerancji dla banalnych treści; oczekuje wymagającego rozmówcy i konkretnego feedbacku |
| Wzorzec użycia | 3–4 razy w tygodniu po 15–20 min: zaawansowane scenariusze (negocjacje, prezentacja, debata), przegląd analizy po rozmowie, trening wymowy trudnych słów, tygodniowy raport postępów |
| Kluczowe funkcje | trudne scenariusze na poziomie C1, analiza FLUENCY/VOCABULARY, pamięć AI (kontekst zawodowy), statystyki i raporty tygodniowe |

## 3. Sposób korzystania

| Wymiar | Opis |
|---|---|
| Częstotliwość | Docelowo codziennie (mechanika streak + cel dzienny w minutach z onboardingu `GoalsScreen`); realnie 3–7 sesji/tydzień |
| Długość sesji | Rozmowa: 5–20 min; powtórki/ćwiczenia: 3–10 min; łączny cel dzienny konfigurowalny |
| Pora i miejsce | Dojazdy (słuchawki), przerwa w pracy, wieczór w domu; sesje głosowe wymagają możliwości mówienia na głos, ćwiczenia i fiszki działają wszędzie |
| Główny rytuał | `HomeScreen`: streak → szybki start rozmowy → po rozmowie `ConversationSummaryScreen` (analiza) → w wolnej chwili powtórki due z `ExercisesScreen`/`VocabularyReviewScreen` |
| Konteksty użycia | (1) rozmowa scenariuszowa lub swobodna, (2) powtórki SRS (słówka + ćwiczenia), (3) trening wymowy (`PronunciationScreen`), (4) notatki głosowe — „chcę umieć to powiedzieć" (`VoiceNotesScreen`), (5) przegląd postępów (`StatisticsScreen`) |
| Offline | Bez sieci: historia, transkrypcje, fiszki, ćwiczenia już wygenerowane, statystyki, notatki; rozmowa z AI i generowanie treści wymagają sieci |

## 4. Wymagania funkcjonalne

| ID | Wymaganie | Moduł | Priorytet |
|---|---|---|---|
| FR-01 | Onboarding: wybór poziomu A2–C1 (samoocena), cele nauki, cel dzienny w minutach, uprawnienia (mikrofon, powiadomienia), konfiguracja klucza OpenAI | onboarding/settings | MVP |
| FR-02 | Rozmowa głosowa z tutorem AI w pętli STT → AI → TTS, z widoczną transkrypcją na żywo (`ConversationScreen`, `MicButton` ze stanami idle/listening/processing/speaking) | conversation | MVP |
| FR-03 | Wybór scenariusza rozmowy (sytuacje codzienne, zawodowe, swobodna rozmowa) dopasowanego do poziomu użytkownika | conversation | MVP |
| FR-04 | Zapis pełnej historii rozmów z transkrypcjami do Room (`conversations`, `messages`); przegląd offline (`ConversationListScreen`) | conversation | MVP |
| FR-05 | Tłumaczenie wiadomości tutora na polski na żądanie (`messages.translation`) | conversation | MVP |
| FR-06 | Analiza rozmowy po jej zakończeniu: lista błędów (kategorie GRAMMAR/VOCABULARY/PRONUNCIATION/FLUENCY) z poprawką i wyjaśnieniem, nowe słownictwo, podsumowanie (`ConversationSummaryScreen`, tabele `user_errors`, `vocabulary_items`) | ai/conversation | MVP |
| FR-07 | Pamięć długoterminowa AI: konsolidacja faktów/celów/słabości po rozmowie (`MemoryConsolidationWorker`, `ai_memories` z embeddingami), wstrzykiwanie top-K wspomnień do system promptu przed rozmową (RAG) | memory | MVP |
| FR-08 | Generowanie ćwiczeń z błędów użytkownika (typy: FILL_GAP, MULTIPLE_CHOICE, TRANSLATION, SPEAKING) i ich wykonywanie w sesjach (`ExercisePlayerScreen`) | exercises | MVP |
| FR-09 | Harmonogram powtórek SRS (algorytm SM-2) dla słówek i ćwiczeń; codzienne wyznaczanie powtórek due + powiadomienie (`RevisionSchedulerWorker`) | revision | MVP |
| FR-10 | Słownictwo: lista wykrytych/nowych słówek z tłumaczeniem, definicją i przykładem; fiszki SRS; statusy NEW/LEARNING/MASTERED (`VocabularyScreen`, `VocabularyReviewScreen`) | vocabulary | MVP |
| FR-11 | Trening wymowy: użytkownik wymawia słowo, ocena 0–100 (confidence STT + feedback AI), historia wyników (`PronunciationScreen`, `pronunciation_results`) | pronunciation | MVP |
| FR-12 | Notatki głosowe: nagrywanie, automatyczna transkrypcja, lista notatek (`VoiceNotesScreen`, `voice_notes`) | voicenotes | MVP |
| FR-13 | Statystyki: streak, minuty rozmów, wysłane wiadomości, nauczone słówka, skuteczność ćwiczeń, średnia wymowy; wykresy (`StatisticsScreen`, `daily_stats`) | statistics | MVP |
| FR-14 | Tygodniowy raport postępów generowany przez LLM ze statystyk | statistics/ai | Later |
| FR-15 | Ustawienia: profil (poziom, cele), głos i tempo TTS (US/GB), model AI, klucz API (szyfrowany), motyw, zarządzanie danymi (`SettingsScreen` + podekrany) | settings | MVP |
| FR-16 | Eksport danych do lokalnego pliku JSON i kasowanie danych (`SettingsDataScreen`) | sync | MVP |
| FR-17 | Synchronizacja chmurowa danych między urządzeniami | sync | Later (S6) |
| FR-18 | Opcjonalne STT Whisper API i TTS OpenAI jako alternatywa dla systemowych | ai | Later (S6) |
| FR-19 | Ekran startowy `HomeScreen`: streak, postęp celu dziennego, szybki start rozmowy, powtórki due | home | MVP |
| FR-20 | Powiadomienia: przypomnienie o powtórkach due i celu dziennym | revision | MVP |

## 5. Wymagania niefunkcjonalne

| ID | Kategoria | Wymaganie |
|---|---|---|
| NFR-01 | Wydajność | Cold start aplikacji < 2 s; UI 60 fps (Compose, brak pracy na wątku głównym) |
| NFR-02 | Wydajność | Latencja odpowiedzi głosowej (koniec wypowiedzi użytkownika → start TTS) < 4 s (cel); wskaźnik stanu `processing` widoczny natychmiast |
| NFR-03 | Bezpieczeństwo | Klucz OpenAI przechowywany wyłącznie w szyfrowanym storage (security-crypto + DataStore); nigdy w logach ani plikach eksportu |
| NFR-04 | Bezpieczeństwo | Cała komunikacja sieciowa przez HTTPS; brak PII i treści rozmów w logach release |
| NFR-05 | Prywatność | Dane użytkownika przechowywane lokalnie (Room/DataStore); nagrania audio kasowane po transkrypcji; wysyłka treści do AI za wyraźnym opt-in w onboardingu |
| NFR-06 | Prywatność | Zgodność z RODO: eksport danych (przenoszalność), pełne kasowanie danych na żądanie, przejrzysta informacja o przetwarzaniu głosu i wysyłce transkrypcji do OpenAI |
| NFR-07 | Dostępność | Pełne wsparcie TalkBack, kontrast min. WCAG AA, touch targets min. 48dp, wsparcie dużych czcionek |
| NFR-08 | Offline | Historia rozmów, słownictwo, ćwiczenia, statystyki i notatki w pełni dostępne offline; Room źródłem prawdy; wyniki AI zapisywane lokalnie przed pokazaniem |
| NFR-09 | Niezawodność | Zerwanie sieci/błąd API w trakcie rozmowy nie może utracić dotychczasowej transkrypcji; degradacja z komunikatem (`ErrorBanner`) i możliwością ponowienia |
| NFR-10 | Testowalność | Testy jednostkowe warstw domain/data/ai (JUnit4, MockK, Turbine); testy UI Compose dla kluczowych przepływów |
| NFR-11 | Kompatybilność | Android 8.0+ (minSdk 26), poprawne działanie na urządzeniach bez zainstalowanego głosu TTS lub bez usługi rozpoznawania mowy (czytelny komunikat + fallback tekstowy) |
| NFR-12 | Koszty | Kontrola zużycia API: domyślny model `gpt-4o-mini`, przycinanie okna kontekstu (ostatnie N wiadomości), batching embeddingów, czytelny komunikat przy wyczerpaniu limitu klucza |

## 6. Możliwe problemy i ryzyka

### 6.1 Ryzyka techniczne

| Ryzyko | Wpływ | Prawdop. | Mitygacja |
|---|---|---|---|
| Sumaryczna latencja STT→AI→TTS > 4 s psuje wrażenie „rozmowy" | Wysoki | Średnie | On-device `SpeechRecognizer` (niska latencja), krótkie odpowiedzi tutora wymuszone promptem, `gpt-4o-mini`, przycinanie kontekstu, natychmiastowy feedback wizualny (stan `processing`, animacja fali) |
| Jakość rozpoznawania polskiego akcentu przez STT (błędne transkrypcje frustrują, zaniżają ocenę wymowy) | Wysoki | Wysokie | Tutor AI instruowany, by tolerować szum STT i dopytywać zamiast karać; edycja transkrypcji przed wysłaniem (opcja); wynik wymowy łączy confidence STT z oceną AI; roadmapa: Whisper API (lepszy dla akcentów) |
| Koszty OpenAI API rosną z użyciem (rozmowy + analizy + embeddingi) | Średni | Wysokie | MVP: klucz użytkownika (koszt po jego stronie, jasno zakomunikowany); tani model domyślny; structured output ogranicza tokeny; docelowo backend proxy z limitami |
| Brak/rozbieżność usług systemowych: `SpeechRecognizer` lub `TextToSpeech` niedostępne albo słabej jakości na części urządzeń | Średni | Średnie | Detekcja dostępności przy starcie, prowadzenie do instalacji głosu/usługi Google, fallback: rozmowa tekstowa bez TTS; roadmapa: Whisper/OpenAI TTS |
| Awaria/zmiany OpenAI API, rate limiting | Średni | Średnie | Retry z backoff, czytelne komunikaty błędów, konfigurowalny model, zapis lokalny przed pokazaniem (nic nie ginie) |
| Rozmiar embeddingów i skalowanie wyszukiwania kosinusowego w Room | Niski | Niskie | Limit liczby wspomnień, `importance` do przycinania, top-K liczone w pamięci (skala setek rekordów jest bezpieczna) |

### 6.2 Ryzyka produktowe

| Ryzyko | Wpływ | Prawdop. | Mitygacja |
|---|---|---|---|
| Niska retencja — użytkownik porzuca po kilku dniach | Wysoki | Wysokie | Pętla nawyku: streak, cel dzienny, powiadomienie o powtórkach due (`RevisionSchedulerWorker`), krótkie sesje 5–10 min, widoczny postęp na `HomeScreen` i `StatisticsScreen` |
| Bariera wstydu/mówienia na głos — użytkownik nie zaczyna rozmów | Wysoki | Średnie | Pozycjonowanie „bez oceniania", scenariusze o niskim progu wejścia, tutor dopasowany do poziomu, tryb wolniejszego TTS, prywatność (dane lokalnie) |
| Tutor AI generyczny/nudny → brak poczucia personalizacji | Średni | Średnie | Pamięć długoterminowa (RAG) jako rdzeń produktu, ćwiczenia z własnych błędów, scenariusze powiązane z celami z onboardingu |
| Bariera wejścia: konieczność podania własnego klucza OpenAI w MVP | Wysoki | Wysokie | Prosty kreator w `ApiSetupScreen` (instrukcja krok po kroku, walidacja klucza testowym wywołaniem); priorytetowa roadmapa backend proxy |
| Zła samoocena poziomu w onboardingu → zbyt trudne/łatwe rozmowy | Średni | Średnie | Poziom edytowalny w `SettingsProfileScreen`; tutor adaptuje trudność na podstawie analizy błędów i pamięci |

### 6.3 Ryzyka prawne i prywatności

| Ryzyko | Wpływ | Prawdop. | Mitygacja |
|---|---|---|---|
| RODO: głos to dane osobowe; transkrypcje mogą zawierać dane wrażliwe wysyłane do OpenAI (transfer poza EOG) | Wysoki | Średnie | Wyraźny opt-in na wysyłkę do AI przed pierwszą rozmową, polityka prywatności wprost opisująca OpenAI jako odbiorcę, minimalizacja: audio kasowane po transkrypcji, dane trzymane lokalnie |
| Prawo do usunięcia i przenoszalności danych | Średni | Niskie | `SettingsDataScreen`: pełny eksport JSON + kasowanie wszystkich danych lokalnych; brak kont serwerowych w MVP upraszcza zgodność |
| Odpowiedzialność za treści AI (błędne korekty, niestosowne treści) | Niski | Niskie | Prompty z guardrails (rola tutora, tematyka nauki języka), disclaimer w aplikacji, structured output ogranicza swobodę generacji analitycznych |
| Klucz API użytkownika: wyciek = koszt finansowy użytkownika | Średni | Niskie | Szyfrowanie kluczem sprzętowym (security-crypto), klucz nigdy nie opuszcza urządzenia poza nagłówkiem HTTPS do OpenAI, wykluczony z eksportu i backupu |

## 7. Podsumowanie produktu

AI English Coach to offline-first'owa, natywna aplikacja Android, która zamienia
największą słabość polskich dorosłych uczących się angielskiego — brak regularnej,
bezpiecznej okazji do mówienia — w codzienny nawyk. Rdzeniem jest pętla nauki: głosowa
rozmowa z tutorem AI, który pamięta ucznia (RAG na `ai_memories`), automatyczna analiza
błędów i słownictwa, spersonalizowane ćwiczenia oraz powtórki SM-2, a wszystko spięte
statystykami i mechaniką streak/celu dziennego. MVP (sprinty S0–S5) opiera się na
systemowym STT/TTS i kluczu OpenAI użytkownika; roadmapa (S6) to backend proxy,
synchronizacja chmurowa oraz Whisper/OpenAI TTS. Kluczowe ryzyka — latencja pętli
głosowej, rozpoznawanie polskiego akcentu, retencja i zgodność z RODO — mają
zdefiniowane mitygacje wbudowane w architekturę i UX od pierwszego sprintu.
