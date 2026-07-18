# AI English Coach — ETAP 3: User Stories

> Dokument zgodny z briefem decyzji projektowych (`00-brief-decyzje-projektowe.md`).
> Stories po angielsku, opisy i kryteria po polsku. Numeracja US-01…; odwołania do
> funkcji z etapu 2 (F-CONV-01 itd.). Kryteria akceptacji w formacie Given/When/Then.

## Konwencje obsługi błędów (wspólne)

Poniższe zachowania obowiązują we wszystkich stories korzystających z sieci/AI/mowy
(nie powtarzamy ich w każdym US, chyba że story dotyczy ich wprost):

| Sytuacja | Zachowanie aplikacji |
|---|---|
| Brak internetu | `ErrorBanner` „Brak połączenia — funkcje AI niedostępne"; dane lokalne (historia, fiszki, ćwiczenia, statystyki, notatki) działają normalnie; akcja do ponowienia |
| Błąd API (5xx, timeout) | Retry z backoff (max 2); potem `ErrorBanner` z przyciskiem „Spróbuj ponownie"; żadna zapisana lokalnie treść nie ginie |
| Wyczerpany limit / nieważny klucz (429/401) | Czytelny komunikat rozróżniający limit od złego klucza + skrót do `SettingsAiScreen` |
| Brak uprawnienia do mikrofonu | Ekran/dialog wyjaśniający + przycisk do ustawień systemowych; funkcje niegłosowe pozostają dostępne |
| STT nic nie rozpoznał | Komunikat „Nie usłyszałem — spróbuj jeszcze raz", stan `MicButton` wraca do idle; brak pustej wiadomości w `messages` |

---

## Moduł: Onboarding i konfiguracja (settings)

### US-01 — Wybór poziomu i celów
**As a new user I want to set my English level (A2–C1), learning goals and a daily minutes goal during onboarding so that conversations and exercises match my needs.**

Opis: przepływ `WelcomeScreen` → `LevelSelectionScreen` → `GoalsScreen`. Wartości zapisywane w DataStore (`englishLevel`, `learningGoals`, `dailyGoalMinutes`).

Kryteria akceptacji:
- Given nowa instalacja, When otwieram aplikację, Then `SplashScreen` kieruje mnie do onboardingu (`onboarding/welcome`).
- Given jestem na `LevelSelectionScreen`, When wybieram poziom B1 i przechodzę dalej, Then poziom jest zapisany i używany w promptach AI.
- Given jestem na `GoalsScreen`, When wybieram cele i cel dzienny 10 minut, Then `HomeScreen` pokazuje postęp względem 10 minut.
- Given ukończyłem onboarding, When ponownie otwieram aplikację, Then trafiam bezpośrednio na `home` (`onboardingCompleted = true`).

### US-02 — Uprawnienia
**As a new user I want the app to clearly ask for microphone and notification permissions so that I understand why they are needed before granting them.**

Opis: `PermissionsScreen` z wyjaśnieniem celu każdego uprawnienia; odmowa nie blokuje ukończenia onboardingu.

Kryteria akceptacji:
- Given jestem na `PermissionsScreen`, When czytam ekran, Then widzę po polsku, do czego służy mikrofon (rozmowy, wymowa, notatki) i powiadomienia (powtórki).
- Given odmówiłem mikrofonu, When kończę onboarding, Then mogę używać aplikacji, a przy próbie rozmowy dostaję dialog z przyciskiem do ustawień systemowych.
- Given odmówiłem powiadomień, When nadchodzą powtórki due, Then nie dostaję powiadomienia, ale `HomeScreen` nadal pokazuje sekcję powtórek.

Wyjątki: trwała odmowa („nie pytaj ponownie") → aplikacja prowadzi do ustawień systemowych zamiast ponawiać systemowy dialog.

### US-03 — Konfiguracja klucza OpenAI
**As a new user I want to enter and validate my OpenAI API key so that AI features work and I trust the key is stored securely.**

Opis: `ApiSetupScreen` (onboarding) i `SettingsAiScreen` (później). Klucz walidowany testowym wywołaniem, zapis szyfrowany (security-crypto).

Kryteria akceptacji:
- Given wpisałem klucz, When klikam „Zweryfikuj", Then aplikacja wykonuje testowe wywołanie i pokazuje sukces lub konkretny błąd.
- Given klucz jest nieprawidłowy (401), When weryfikuję, Then widzę „Klucz nieprawidłowy" (nie ogólny błąd sieci).
- Given klucz zapisany, When eksportuję dane (US-31), Then klucz NIE znajduje się w pliku eksportu.

Wyjątki/błędy: brak internetu podczas weryfikacji → komunikat o sieci z opcją ponowienia (klucz nie jest odrzucany); limit wyczerpany (429) → „Klucz poprawny, ale limit wyczerpany — sprawdź billing OpenAI".

### US-04 — Zmiana profilu nauki
**As a user I want to change my level, goals and daily goal in settings so that the app adapts when my needs change.**

Opis: `SettingsProfileScreen`; zmiany działają od następnej rozmowy/generacji.

Kryteria akceptacji:
- Given mam poziom A2, When zmieniam na B1 w `SettingsProfileScreen`, Then kolejna rozmowa używa promptu dla B1.
- Given zmieniam cel dzienny z 10 na 20 minut, When wracam na `HomeScreen`, Then `ProgressRing` liczy postęp względem 20 minut (dzisiejsze minuty zachowane).

---

## Moduł: Rozmowy (conversation)

### US-05 — Start rozmowy ze scenariuszem
**As a user I want to start a voice conversation from a chosen scenario so that I practice situations relevant to my life.**

Opis: F-CONV-01/02; szybki start z `HomeScreen` lub wybór scenariusza; rekord w `conversations` ze `scenario` i `startedAt`.

Kryteria akceptacji:
- Given jestem na `HomeScreen`, When tapnę „szybki start", Then otwiera się `ConversationScreen` z rozmową swobodną dopasowaną do mojego poziomu.
- Given wybieram scenariusz „Rozmowa o pracę", When rozmowa startuje, Then tutor otwiera rozmowę w tym kontekście i rekord ma `scenario` ustawione.
- Given brak internetu, When próbuję rozpocząć rozmowę, Then widzę `ErrorBanner` i rozmowa nie zostaje utworzona jako pusta.

### US-06 — Pętla głosowa
**As a user I want to speak to the tutor and hear a spoken reply so that the experience feels like a real conversation.**

Opis: F-CONV-03; `MicButton` idle→listening→processing→speaking, animacja fali podczas słuchania; cel latencji < 4 s (NFR-02).

Kryteria akceptacji:
- Given rozmowa aktywna, When przytrzymuję/tapnę `MicButton` i mówię, Then widzę stan listening z animacją fali, a po zakończeniu mój tekst pojawia się jako `MessageBubble` (rola USER).
- Given moja wypowiedź została rozpoznana, When AI odpowiada, Then odpowiedź pojawia się jako `MessageBubble` (ASSISTANT) i jest czytana przez TTS (stan speaking).
- Given AI mówi, When tapnę `MicButton`, Then TTS się zatrzymuje i mogę mówić (barge-in).
- Given każda tura, When wiadomość powstaje, Then jest natychmiast zapisana do `messages` (Room przed pokazaniem — offline-first).

Wyjątki/błędy:
- STT nic nie rozpoznał → komunikat „Nie usłyszałem", powrót do idle, brak pustej wiadomości.
- Błąd API w trakcie → dotychczasowa transkrypcja zostaje (NFR-09), przycisk „Ponów odpowiedź".
- Cofnięcie uprawnienia do mikrofonu w trakcie → dialog z przejściem do ustawień; rozmowa nie znika.

Przypadki brzegowe:
- Zmiana języka w trakcie (użytkownik mówi po polsku): STT (locale EN) zwróci zniekształcony tekst — tutor jest promptowany, by grzecznie zachęcić do angielskiego lub dopytać; wypowiedź nie jest liczona jako błąd gramatyczny.
- Bardzo długa rozmowa: okno kontekstu przycinane do ostatnich N wiadomości (pamięć krótkoterminowa); UI listy wiadomości pozostaje płynne (lazy list).

### US-07 — Tłumaczenie wiadomości
**As an A2 user I want to translate the tutor's message into Polish on demand so that I never get stuck not understanding.**

Opis: F-CONV-05; tłumaczenie zapisywane w `messages.translation`, potem dostępne offline.

Kryteria akceptacji:
- Given widzę wiadomość tutora, When tapnę „Przetłumacz", Then pod treścią pojawia się polskie tłumaczenie.
- Given wiadomość ma już tłumaczenie, When otwieram rozmowę offline, Then tłumaczenie jest widoczne bez sieci.
- Given brak internetu i brak zapisanego tłumaczenia, When tapnę „Przetłumacz", Then widzę komunikat o braku sieci (bez crasha).

### US-08 — Ponowny odsłuch wiadomości
**As a user I want to replay any tutor message with TTS so that I can catch what I missed.**

Opis: F-CONV-06; odsłuch z tempem z ustawień; działa offline (TTS lokalny).

Kryteria akceptacji:
- Given rozmowa (aktywna lub z historii), When tapnę ikonę odtwarzania przy wiadomości ASSISTANT, Then TTS czyta tę wiadomość ustawionym głosem i tempem.
- Given TTS czyta wiadomość, When tapnę ponownie, Then odtwarzanie się zatrzymuje.

Przypadek brzegowy: brak zainstalowanego głosu TTS na urządzeniu → komunikat z linkiem do instalacji danych głosu w ustawieniach systemowych; transkrypcja pozostaje czytelna (rozmowa możliwa w trybie „czytam zamiast słuchać").

### US-09 — Zakończenie rozmowy i podsumowanie
**As a user I want to end a conversation and see an analysis of my errors, new vocabulary and feedback so that I know what to improve.**

Opis: F-CONV-07/08; `endedAt`/`durationSec`/`status`/`summary`; analiza AI (structured JSON) → `user_errors`, `vocabulary_items`; start `MemoryConsolidationWorker`; nawigacja do `ConversationSummaryScreen`.

Kryteria akceptacji:
- Given rozmowa z min. 1 moją wypowiedzią, When kończę rozmowę, Then widzę `ConversationSummaryScreen` z podsumowaniem, listą błędów (kategoria, oryginał, poprawka, wyjaśnienie) i nowymi słówkami.
- Given analiza zakończona, When otwieram `practice/vocabulary` i `practice/exercises`, Then nowe słówka i ćwiczenia wygenerowane z błędów są widoczne.
- Given analiza trwa, When czekam, Then widzę `LoadingIndicator` z informacją, że analiza jest w toku.

Wyjątki/błędy: błąd API podczas analizy → rozmowa zapisana normalnie, podsumowanie oznaczone „analiza nieukończona" z przyciskiem „Analizuj ponownie"; wyczerpany limit klucza → komunikat + ta sama możliwość ponowienia później.

Przypadek brzegowy — pusta rozmowa: Given rozmowa bez żadnej mojej wypowiedzi, When ją kończę, Then analiza NIE jest wywoływana (brak kosztu API), a rozmowa jest usuwana lub oznaczana jako pusta — nie zaśmieca historii i nie nabija streaka.

### US-10 — Historia rozmów offline
**As a user I want to browse past conversations with full transcripts offline so that I can review what I said anywhere.**

Opis: F-CONV-09; `ConversationListScreen` → transkrypcja + podsumowanie.

Kryteria akceptacji:
- Given mam zakończone rozmowy, When otwieram zakładkę Rozmowy, Then widzę listę (tytuł, scenariusz, data, czas trwania) posortowaną od najnowszej.
- Given tryb samolotowy, When otwieram rozmowę z listy, Then pełna transkrypcja i zapisane podsumowanie są dostępne.
- Given brak rozmów, When otwieram listę, Then widzę `EmptyState` z zachętą do pierwszej rozmowy.

### US-11 — Usuwanie rozmowy
**As a user I want to delete a conversation so that I control my stored data.**

Opis: F-CONV-10; kaskadowe usunięcie `messages`; słówka/błędy pozostają (tracą tylko referencję źródła).

Kryteria akceptacji:
- Given lista rozmów, When usuwam rozmowę i potwierdzam, Then rozmowa i jej wiadomości znikają z bazy.
- Given usunąłem rozmowę, When otwieram słownictwo, Then słówka z niej pozostają (z `sourceConversationId` osieroconym/wyczyszczonym).

---

## Moduł: Pamięć (memory)

### US-12 — Tutor, który mnie pamięta
**As a returning user I want the tutor to remember my goals, weaknesses and life context so that each conversation feels personal and builds on previous ones.**

Opis: F-MEM-01…04; `MemoryConsolidationWorker` po rozmowie → `ai_memories` (kind, importance, embedding); przed rozmową `MemoryManager` wstrzykuje top-K wspomnień (podobieństwo kosinusowe) do system promptu.

Kryteria akceptacji:
- Given w poprzedniej rozmowie powiedziałem, że przygotowuję się do rozmowy o pracę, When zaczynam nową rozmowę, Then tutor potrafi nawiązać do tego faktu (wspomnienie w promptcie).
- Given rozmowa zakończona, When `MemoryConsolidationWorker` się wykona, Then nowe wspomnienia (FACT/PREFERENCE/WEAKNESS/GOAL/PROGRESS) są w `ai_memories` z embeddingiem.
- Given fakt już istnieje w pamięci, When konsolidacja znajdzie jego nowszą wersję, Then wpis jest zaktualizowany (`updatedAt`), nie zduplikowany.

Wyjątki: brak sieci po rozmowie → WorkManager ponawia konsolidację przy dostępnej sieci (constraint NETWORK); błąd embeddingów → wspomnienie zapisane bez embeddingu i uzupełnione przy kolejnym przebiegu.

Przypadek brzegowy: pierwsza rozmowa (pusta pamięć) → prompt działa bez sekcji wspomnień; brak błędu.

---

## Moduł: Ćwiczenia (exercises)

### US-13 — Ćwiczenia z moich błędów
**As a user I want exercises generated from my own mistakes so that I practice exactly what I got wrong.**

Opis: F-EX-01; `ExerciseGenerator` tworzy z `user_errors` ćwiczenia FILL_GAP/MULTIPLE_CHOICE/TRANSLATION/SPEAKING z `sourceErrorId`.

Kryteria akceptacji:
- Given analiza rozmowy wykryła błąd GRAMMAR, When generacja się zakończy, Then w `ExercisesScreen` widzę ćwiczenie powiązane z tym błędem (z wyjaśnieniem).
- Given ćwiczenie typu MULTIPLE_CHOICE, When je otwieram, Then ma pytanie, opcje (json), poprawną odpowiedź i wyjaśnienie.

Wyjątki: błąd API generacji → błędy pozostają w `user_errors`, generacja ponawiana; brak sieci → ćwiczenia już wygenerowane działają offline.

### US-14 — Sesja ćwiczeń
**As a user I want to complete a series of due exercises in one flow so that reviewing feels quick and focused.**

Opis: F-EX-02…05; `ExercisePlayerScreen`; ocena → `exercise_attempts` → aktualizacja SM-2 (`easeFactor`, `intervalDays`, `dueAt`).

Kryteria akceptacji:
- Given mam 5 ćwiczeń due, When startuję sesję z `ExercisesScreen`, Then ćwiczenia lecą jedno po drugim z paskiem postępu.
- Given odpowiadam poprawnie, When przechodzę dalej, Then próba jest w `exercise_attempts` (isCorrect=true), a `dueAt` przesunięty wg SM-2.
- Given odpowiadam błędnie, When widzę wynik, Then dostaję poprawną odpowiedź z wyjaśnieniem, a interwał SRS jest zresetowany.
- Given przerywam sesję w połowie, When wracam później, Then ukończone ćwiczenia mają zapisane próby, reszta pozostaje due.

Przypadek brzegowy: 0 ćwiczeń due → `EmptyState` „Wszystko powtórzone" z opcją ćwiczenia wszystkich.

### US-15 — Ćwiczenie mówione (SPEAKING)
**As a user I want speaking exercises checked by voice so that I train production, not only recognition.**

Opis: F-EX-03/04; odpowiedź przez STT, ocena AI.

Kryteria akceptacji:
- Given ćwiczenie SPEAKING, When wymawiam odpowiedź, Then transkrypcja jest pokazywana i oceniana (AI), z feedbackiem.
- Given STT nic nie rozpoznał, When kończę mówić, Then mogę spróbować ponownie lub pominąć ćwiczenie (bez oceny negatywnej).
- Given brak uprawnienia do mikrofonu, When trafiam na SPEAKING, Then ćwiczenie oferuje wariant pisemny lub pominięcie + informację o uprawnieniu.
- Given brak internetu, When trafiam na SPEAKING/TRANSLATION wymagające oceny AI, Then ćwiczenie jest odkładane, a sesja kontynuuje typami ocenianymi lokalnie.

### US-16 — Domknięcie błędu
**As a user I want my source errors marked as resolved after I answer their exercises correctly so that I can see my weaknesses shrinking.**

Opis: F-EX-06; `user_errors.resolvedAt` po serii poprawnych prób.

Kryteria akceptacji:
- Given błąd z 2 ćwiczeniami, When odpowiem poprawnie w wymaganej serii, Then błąd dostaje `resolvedAt` i w statystykach rośnie licznik rozwiązanych błędów.

---

## Moduł: Powtórki (revision)

### US-17 — Codzienne powtórki i powiadomienie
**As a user I want a daily reminder with the number of due reviews so that I keep my routine without opening the app first.**

Opis: F-REV-02/03; `RevisionSchedulerWorker` (codzienny) wyznacza due (`dueAt <= dziś`) dla `vocabulary_items` i `exercises`, wysyła powiadomienie.

Kryteria akceptacji:
- Given mam elementy z `dueAt` na dziś, When worker się wykona, Then dostaję powiadomienie „Masz X słówek i Y ćwiczeń do powtórki".
- Given tapnę powiadomienie, When aplikacja się otwiera, Then trafiam do odpowiedniej sekcji powtórek.
- Given 0 elementów due, When worker się wykona, Then powiadomienie NIE jest wysyłane.
- Given brak zgody na powiadomienia, When worker się wykona, Then powiadomienia nie ma, ale sekcja due na `HomeScreen` jest zaktualizowana.

Przypadek brzegowy: urządzenie wyłączone o porze workera → WorkManager wykonuje pracę po włączeniu (powtórki nie przepadają).

### US-18 — Powtórki due na Home
**As a user I want to see due reviews on the Home screen so that starting a review takes one tap.**

Opis: F-REV-04; `HomeScreen` z liczbami due i wejściem w `VocabularyReviewScreen` / sesję ćwiczeń.

Kryteria akceptacji:
- Given mam 12 słówek i 3 ćwiczenia due, When otwieram `HomeScreen`, Then widzę obie liczby i przyciski startu sesji.
- Given ukończę powtórki, When wracam na Home, Then sekcja pokazuje stan „zrobione" (odświeżenie reactive przez Flow).

---

## Moduł: Słownictwo (vocabulary)

### US-19 — Słówka wykryte z rozmów
**As a user I want new words from my conversations collected automatically with translation, definition and example so that I build vocabulary without manual work.**

Opis: F-VOC-01; analiza po rozmowie → `vocabulary_items` (status NEW, `sourceConversationId`).

Kryteria akceptacji:
- Given w rozmowie pojawiło się nowe dla mnie słowo, When analiza się zakończy, Then słowo jest w `VocabularyScreen` z tłumaczeniem, definicją i przykładem z kontekstu.
- Given słowo już istnieje w mojej bazie, When analiza je znajdzie ponownie, Then duplikat NIE powstaje.

### US-20 — Fiszki SRS
**As a user I want to review vocabulary as flashcards with spaced repetition so that words move to long-term memory.**

Opis: F-VOC-04/05; `VocabularyReviewScreen`; samoocena 0–5 → SM-2; statusy NEW→LEARNING→MASTERED.

Kryteria akceptacji:
- Given słówka due, When startuję sesję fiszek, Then widzę słowo, po odsłonięciu tłumaczenie+przykład i oceniam znajomość (0–5).
- Given ocena ≥ 4 powtarzalnie, When historia spełnia próg, Then status przechodzi LEARNING→MASTERED.
- Given ocena ≤ 2, When zapisuję, Then interwał resetuje się (słowo wróci szybko).
- Given tryb samolotowy, When robię fiszki, Then wszystko działa offline (SM-2 lokalny).

Przypadek brzegowy: pusta lista słówek (nowy użytkownik) → `EmptyState` z zachętą do pierwszej rozmowy lub ręcznego dodania słowa.

### US-21 — Ręczne dodanie słówka i odsłuch
**As a user I want to add my own word and hear its pronunciation so that I capture words I meet outside the app.**

Opis: F-VOC-02/06; AI uzupełnia tłumaczenie/definicję/przykład; TTS czyta słowo.

Kryteria akceptacji:
- Given wpisuję słowo „warehouse", When zapisuję, Then AI uzupełnia tłumaczenie, definicję i przykład, a słowo wchodzi do SRS ze statusem NEW.
- Given brak internetu, When dodaję słowo, Then zapisuje się z samą formą, a wzbogacenie AI następuje po odzyskaniu sieci.
- Given lista lub fiszka, When tapnę ikonę głośnika, Then TTS czyta słowo (offline).

### US-22 — Filtrowanie listy słówek
**As a user I want to filter and search my vocabulary by status and source so that I quickly find what to work on.**

Opis: F-VOC-03; filtry NEW/LEARNING/MASTERED, wyszukiwanie.

Kryteria akceptacji:
- Given mam 200 słówek, When filtruję po MASTERED, Then widzę tylko opanowane.
- Given wpisuję frazę w wyszukiwarkę, When lista się filtruje, Then wyniki obejmują dopasowania w słowie i tłumaczeniu.

---

## Moduł: Wymowa (pronunciation)

### US-23 — Trening wymowy słowa
**As a user I want to practice pronouncing a word and get a score with a concrete tip so that I know exactly what to fix.**

Opis: F-PRON-01/02/05; wzorzec TTS → wypowiedź → STT → wynik 0–100 (confidence + zgodność) + feedback AI; zapis do `pronunciation_results`.

Kryteria akceptacji:
- Given słowo na `PronunciationScreen`, When tapnę „Posłuchaj", Then TTS czyta wzorzec.
- Given wymawiam słowo, When STT je rozpozna, Then widzę wynik 0–100, `recognizedText` obok `expectedText` i wskazówkę (np. dźwięk „th").
- Given wynik zapisany, When próbuję ponownie, Then widzę porównanie z poprzednią próbą.

Wyjątki/błędy: STT nic nie rozpoznał → „Nie usłyszałem" i ponowna próba bez zapisu wyniku 0; brak internetu → wynik na bazie samego STT (bez feedbacku AI) z adnotacją; brak mikrofonu → dialog uprawnień.

Przypadek brzegowy: STT „poprawia" słowo do innego istniejącego (np. „ship"/„sheep") → niska zgodność obniża wynik, a feedback wskazuje parę minimalną.

### US-24 — Kolejka i historia wymowy
**As a user I want the app to queue words I struggle with and show my score history so that pronunciation training targets my weaknesses.**

Opis: F-PRON-03/04; kolejka z błędów PRONUNCIATION, słabych wyników, słownictwa; trend per słowo.

Kryteria akceptacji:
- Given analiza rozmowy wykryła błąd PRONUNCIATION dla słowa, When otwieram `PronunciationScreen`, Then to słowo jest w kolejce treningu.
- Given słowo ćwiczone kilkukrotnie, When otwieram jego historię, Then widzę listę prób z wynikami i trendem.

---

## Moduł: Notatki głosowe (voicenotes)

### US-25 — Nagranie notatki z transkrypcją
**As a user I want to record a quick voice note that gets transcribed automatically so that I capture thoughts and phrases to learn later.**

Opis: F-NOTE-01/02; `AudioRecorder` → plik (`audioPath`) → transkrypcja STT → `voice_notes` z proponowanym tytułem.

Kryteria akceptacji:
- Given `VoiceNotesScreen`, When nagrywam i zatrzymuję, Then notatka pojawia się na liście z czasem trwania, a transkrypcja uzupełnia się po chwili.
- Given transkrypcja gotowa, When otwieram notatkę, Then widzę tekst i mogę odtworzyć audio.

Wyjątki: STT nic nie rozpoznał → notatka zostaje z audio i statusem „brak transkrypcji" + przycisk „Transkrybuj ponownie"; brak mikrofonu → dialog uprawnień; przerwane nagranie (telefon) → dotychczasowe audio zapisane.

Przypadek brzegowy: bardzo długie nagranie → limit długości z ostrzeżeniem przed jego osiągnięciem; zapis nie przekracza limitu pliku.

### US-26 — Lista, odtwarzanie i usuwanie notatek
**As a user I want to browse, play and delete my voice notes offline so that I manage my recordings freely.**

Opis: F-NOTE-03/04.

Kryteria akceptacji:
- Given mam notatki, When otwieram listę offline, Then widzę tytuły, daty, czasy trwania; odtwarzanie działa bez sieci.
- Given usuwam notatkę i potwierdzam, When operacja się kończy, Then rekord i plik audio są usunięte z urządzenia.

---

## Moduł: Statystyki (statistics)

### US-27 — Streak i cel dzienny
**As a user I want to see my streak and daily goal progress so that I stay motivated to practice every day.**

Opis: F-STAT-01/02; `daily_stats` (date PK); `HomeScreen`: streak + `ProgressRing`.

Kryteria akceptacji:
- Given dziś rozmawiałem 6 z 10 minut celu, When otwieram `HomeScreen`, Then `ProgressRing` pokazuje 60%, a licznik streak aktualną serię dni.
- Given wczoraj nie ćwiczyłem, When otwieram aplikację dziś, Then streak jest wyzerowany (liczony od dziś).
- Given kończę rozmowę/ćwiczenia, When wracam na Home, Then statystyki dnia są zaktualizowane bez restartu (Flow z Room).

Przypadek brzegowy: zmiana strefy czasowej / rozmowa przez północ → aktywność liczona do dnia rozpoczęcia sesji; streak nie pęka przez samą zmianę strefy.

### US-28 — Wykresy postępów
**As a user I want charts of my talking time, vocabulary growth, exercise accuracy and pronunciation over time so that I can see real progress.**

Opis: F-STAT-03/04; `StatisticsScreen` z wykresami tygodniowymi/miesięcznymi i `StatTile`.

Kryteria akceptacji:
- Given mam dane z 3 tygodni, When otwieram `StatisticsScreen`, Then widzę wykresy: minuty rozmów, nowe słówka, skuteczność ćwiczeń (%), średnia wymowy.
- Given tryb samolotowy, When otwieram statystyki, Then wszystko działa offline (dane z `daily_stats`).
- Given brak danych (nowy użytkownik), When otwieram statystyki, Then widzę `EmptyState` zamiast pustych wykresów.

---

## Moduł: Ustawienia (settings)

### US-29 — Głos i tempo TTS
**As a user I want to choose the tutor's voice (US/GB) and speech rate so that listening matches my comfort level.**

Opis: F-SET-04; zapis w DataStore (`ttsVoice`, `ttsSpeed`); podgląd próbki.

Kryteria akceptacji:
- Given `SettingsAiScreen`, When zmieniam głos na GB i tempo na 0.8x, Then przycisk podglądu odtwarza próbkę, a kolejne wypowiedzi tutora używają nowych ustawień.
- Given urządzenie nie ma głosu GB, When wybieram GB, Then aplikacja informuje o braku i prowadzi do instalacji danych głosu (fallback: dostępny głos EN).

### US-30 — Motyw i zgody prywatności
**As a user I want to control the app theme and my AI data-sharing consent so that the app respects my preferences and privacy.**

Opis: F-SET-05/07; light/dark/systemowy + dynamic color; opt-in wysyłki treści do AI (NFR-05/06).

Kryteria akceptacji:
- Given zmieniam motyw na Dark, When wracam do aplikacji, Then cały UI jest w trybie ciemnym (persystentnie).
- Given wycofuję zgodę na wysyłkę do AI, When próbuję rozpocząć rozmowę, Then aplikacja informuje, że funkcje AI wymagają zgody, i pozwala ją przywrócić; dane lokalne działają dalej.

---

## Moduł: Synchronizacja (sync)

### US-31 — Eksport danych do JSON
**As a user I want to export all my data to a local JSON file so that I own my learning history (GDPR portability).**

Opis: F-SYNC-01; `SettingsDataScreen`; eksport bez klucza API.

Kryteria akceptacji:
- Given tapnę „Eksportuj dane", When wybieram lokalizację (SAF), Then powstaje plik JSON z rozmowami, wiadomościami, słówkami, błędami, ćwiczeniami, statystykami i metadanymi notatek.
- Given eksport gotowy, When przeszukuję plik, Then nie zawiera klucza API ani danych szyfrowanych.
- Given mało miejsca na dysku, When eksport się nie powiedzie, Then widzę czytelny błąd, a częściowy plik jest usuwany.

### US-32 — Kasowanie wszystkich danych
**As a user I want to permanently delete all my data so that I can exercise my right to erasure.**

Opis: F-SYNC-02; czyszczenie Room, DataStore i plików audio z podwójnym potwierdzeniem.

Kryteria akceptacji:
- Given `SettingsDataScreen`, When wybieram „Usuń wszystkie dane" i potwierdzam dwustopniowo (w tym przepisanie słowa „USUŃ"), Then baza, preferencje, klucz API i pliki audio są skasowane, a aplikacja wraca do onboardingu.
- Given kasowanie przerwane (crash), When otwieram aplikację ponownie, Then operacja jest dokończona lub stan pozostaje spójny (transakcyjność).

---

## Pokrycie wymaganych wyjątków i przypadków brzegowych

| Sytuacja | Pokryta w |
|---|---|
| Brak internetu | Konwencje wspólne; US-03, US-05, US-06, US-07, US-12, US-13, US-15, US-20, US-21, US-23, US-28 |
| Brak uprawnienia do mikrofonu | US-02, US-06, US-15, US-23, US-25 |
| Błąd API | Konwencje wspólne; US-06, US-09, US-13 |
| Wyczerpany limit klucza (429) | Konwencje wspólne; US-03, US-09 |
| STT nic nie rozpoznał | Konwencje wspólne; US-06, US-15, US-23, US-25 |
| Pusta rozmowa | US-09 |
| Bardzo długa rozmowa / nagranie | US-06, US-25 |
| Zmiana języka w trakcie rozmowy | US-06 |
| Brak TTS na urządzeniu | US-08, US-29 (NFR-11) |
