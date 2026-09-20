# 19. Specyfikacja MVP v2: "AI English Coach Live"

> Projekt osobisty, jednoosobowy (tylko dla Ciebie) — brak kont, brak
> monetyzacji, brak wielu użytkowników. Cel: wycisnąć z rozmowy głosowej z AI
> maksymalnie dużo nauki — żywa korekta, trwała baza błędów, która realnie
> wraca w kolejnych rozmowach, i praktyka na własnych tekstach/tematykach.

## 1. Dlaczego nowe MVP, a nie kolejna iteracja v1

Obecna aplikacja (`voicefuck`, v1) ma solidny szkielet inżynierski, ale jej
**silnik konwersacji głosowej jest architektonicznie za wolny i za "głuchy" na
błędy w locie**, żeby zrealizować to, o co Ci chodzi (podkreślanie zdań na
żywo, baza pomyłek faktycznie używana w kolejnych rozmowach). To nie są
niedoróbki do doszlifowania — to konsekwencja wyboru technologii z 2026-07
(system `SpeechRecognizer`/`TextToSpeech` + zwykłe Chat Completions), którą
trzeba wymienić na rdzeniu. Stąd "nowe MVP", nie łatka.

## 2. Analiza SWOT projektu v1

### Mocne strony (zachować/reużyć)

- **Czysta architektura, w pełni testowalna.** `core/domain` bez zależności
  od Androida, use case'y, `AppResult`/`AppError` jako jawny model błędów,
  87+ testów jednostkowych, DI przez Hilt. To się przenosi 1:1 do v2.
- **Offline-first dane lokalne.** Room (`core/database`) jako źródło prawdy,
  zaszyfrowany klucz API w `EncryptedSharedPreferences`
  (`core/datastore`) — dokładnie taki model prywatności, jakiego potrzebuje
  appka "tylko dla mnie".
- **RAG pamięci długoterminowej już istnieje.** `MemoryRetrievalService` +
  `MemoryManager` (podobieństwo kosinusowe nad embeddingami) — mechanizm
  "asystent pamięta rzeczy o mnie" jest gotowy jako wzorzec; trzeba go
  rozszerzyć o *błędy*, nie tylko fakty.
- **SM-2 (spaced repetition) już zaimplementowany** dla słownictwa
  (`SrsState`, `VocabularyRepository`) — dokładnie ten mechanizm chcesz mieć
  dla błędów gramatycznych/leksykalnych, tylko podłączony inaczej (patrz
  §5).
- **Analiza błędów po rozmowie już istnieje** (`AnalyzeConversationUseCase`,
  `UserError`) — model danych błędu (`category`, `original`, `corrected`,
  `explanation`) jest dobrym punktem startu dla "banku pomyłek".

### Słabe strony względem Twojego celu

1. **Korekta działa wyłącznie wsadowo, po zakończeniu rozmowy.**
   `AnalyzeConversationUseCase` wywołuje `aiTutorService.analyzeConversation()`
   jednym strzałem na całej transkrypcji dopiero gdy user kończy rozmowę
   (`EndConversationUseCase`). W trakcie mówienia nic nie jest oznaczane —
   nie ma śladu "podkreślania zdań na żywo, jak jest nieprawidłowo".
2. **Błędy z przeszłości nigdy nie wracają do przyszłej rozmowy.**
   Sprawdziłem: `UserError` jest czytany tylko przez
   `GenerateExercisesUseCase`/`generateExercises()` (osobne ćwiczenia).
   `StartConversationUseCase`/`SendMessageUseCase` (czyli to, co faktycznie
   buduje prompt do modelu w trakcie rozmowy) **w ogóle nie sięga do
   `UserErrorRepository`** — tylko do `MemoryRetrievalService`, który operuje
   na ogólnych "wspomnieniach" (`AiMemory`), nie na strukturalnych błędach.
   To jest dokładnie luka, o której piszesz: "żeby asystent głosowy przy
   kolejnych rozmowach używał tych z bazy danych z którymi miałem problem" —
   dziś tego mechanizmu nie ma.
3. **Silnik głosowy to STT→tekst→LLM→TTS na systemowych API Androida**
   (`SpeechToTextService`/`TextToSpeechService` nad `SpeechRecognizer`/
   `TextToSpeech`), nie żywy model mowa-do-mowy. Skutki: zauważalne opóźnienie
   między turami, brak naturalnego przerywania (barge-in), zerowa prozodia
   (model "nie słyszy" wahania/intonacji, tylko surowy tekst po rozpoznaniu
   mowy), niska jakość rozpoznawania akcentu.
4. **Brak trybu "wklej tekst i porozmawiajmy o nim".** `ConversationScenario`
   to zamknięty enum ośmiu ogólnych scenariuszy (small talk, rozmowa
   kwalifikacyjna, podróże...) — nie ma miejsca na własny tekst ani
   wąską dziedzinę (prawo, medioznawstwo, konkretna branża biznesowa).
5. **Poziom CEFR to globalne ustawienie, nie kontrola per-sesja.**
   `UserPreferences.englishLevel` jest jeden na całą aplikację — nie da się
   w locie powiedzieć "tę rozmowę chcę na C1, o kontraktach handlowych".

**Wniosek:** v1 dobrze rozwiązuje "czy mogę porozmawiać z AI i dostać ogólny
feedback po fakcie". Twój cel — nauka *w trakcie* rozmowy, z pamięcią
własnych słabości — wymaga żywego pipeline'u z narzędziami (function calling)
wywoływanymi w trakcie mowy, nie przebudowy UI.

## 3. Naukowe podstawy — co faktycznie działa w nauce konwersacji

Krótki przegląd badań, które bezpośrednio kształtują decyzje w §4–§5 (nie
ogólniki — każdy punkt ma konkretną konsekwencję projektową).

- **Prompty (elicitacja) skuteczniejsze niż recasty.** Metaanaliza korekty
  błędów w SLA pokazuje, że korekta ma istotny i trwały wpływ na rozwój
  języka, a efekt jest **większy dla promptów (np. "czy na pewno?", "spróbuj
  inaczej") niż dla recastów** (ciche przeformułowanie poprawnej wersji).
  Recasty generują "uptake" (aktywne poprawienie się przez ucznia) tylko w
  ~31% przypadków, jawna korekta — w ~50% ([Li 2010](https://www.researchgate.net/publication/229940242_The_Effectiveness_of_Corrective_Feedback_in_SLA_A_Meta-Analysis),
  [Lyster meta-analiza](https://www.cambridge.org/core/journals/studies-in-second-language-acquisition/article/abs/oral-feedback-in-classroom-sla/4999EE1C8379B2BF026B148EAF373CA1)).
  **Konsekwencja:** persona tutora w system prompt powinna domyślnie próbować
  elicitacji ("Are you sure about that verb form? Try again.") zanim poda
  gotową poprawkę — nie tylko cicho poprawiać.
- **Immediate vs. delayed feedback: brak różnicy w przyroście nauki, ale
  natychmiastowy feedback poprawia doświadczenie i zaangażowanie.**
  Badanie na 66 uczących się L2 z chatbotem LLM (Frontiers in Education,
  2026) nie znalazło istotnej różnicy w efektach nauki między korektą
  natychmiastową a odroczoną, ale natychmiastowa dawała wyraźnie lepszy
  odbiór narzędzia ([Frontiers 2026](https://www.frontiersin.org/journals/education/articles/10.3389/feduc.2026.1703664/full)).
  **Konsekwencja:** nie trzeba przerywać *każdego* błędu na żywo (to męczące
  i psuje płynność) — wystarczy **cicho oznaczać błędy w tle (function call,
  bez przerywania mowy)** i dawać krótkie podsumowanie na koniec tury/sesji;
  ewentualna korekta na żywo powinna być rzadka i zarezerwowana dla błędów,
  które faktycznie utrudniają zrozumienie.
- **Output Hypothesis / Noticing (Swain, Schmidt): produkcja języka, nie
  tylko słuchanie, wymusza zauważenie luki między tym, co user chce
  powiedzieć, a tym, co potrafi.** "Pushed output" (zmuszenie do
  precyzyjniejszej, bardziej złożonej wypowiedzi) daje większy rozwój
  językowy niż wygodne, proste zdania
  ([przegląd hipotezy output](https://files.eric.ed.gov/fulltext/EJ1095572.pdf)).
  **Konsekwencja:** tutor powinien aktywnie dopytywać, prosić o
  doprecyzowanie, podbijać trudność pytań ("why", "how would you explain
  that to a client"), a nie tylko podążać za userem.
- **AI jako rozmówca ≠ rozmowa z człowiekiem — ryzyko "wspieranego
  monologu".** Badanie 78 studentów niemieckiego (2026) pokazało, że dialog z
  AI ma mniej, za to dłuższe tury, mniejszy udział ucznia w "podłodze"
  rozmowy niż dialog z człowiekiem
  ([EDM 2026](https://educationaldatamining.org/edm2026/proceedings/2026.EDM.full-papers.210/index.html)).
  **Konsekwencja:** trzeba świadomie projektować prompt tak, by model mówił
  krócej i częściej oddawał głos (np. twardy limit długości wypowiedzi
  asystenta + instrukcja "ask one question, then stop").
- **Retrieval practice + spaced repetition działają też na płynność mówienia,
  nie tylko na słówka.** Case study z dorosłymi uczącymi się A1 pokazało
  wzrost długości wypowiedzi (MLR) i płynności po wdrożeniu spaced retrieval
  practice ([Frontiers 2025](https://www.frontiersin.org/journals/education/articles/10.3389/feduc.2025.1715111/full));
  metaanaliza Kim (2022) potwierdza efekt rozłożonej praktyki w SLA ogólnie
  ([Language Learning 2022](https://onlinelibrary.wiley.com/doi/10.1111/lang.12479)).
  **Konsekwencja:** bank pomyłek powinien działać jak SM-2, ale
  *wpleciony w żywą rozmowę* — model dostaje "listę do przetestowania" i ma
  naturalnie stworzyć okazję do użycia danej struktury, a nie tylko pokazywać
  fiszkę.
- **AI redukuje lęk przed mówieniem** (bezpieczne, nieoceniające środowisko)
  ([Humanities and Social Sciences Communications 2025](https://www.nature.com/articles/s41599-025-05550-z)) —
  potwierdza sens aplikacji czysto prywatnej, bez presji społecznej.

## 4. Rdzeń techniczny: Realtime API zamiast STT→LLM→TTS

### 4.1 Model

**`gpt-realtime-2.1`** (OpenAI Realtime API, WebRTC lub WebSocket) jako
zamiennik obecnego trio `SpeechRecognizer` + Chat Completions + `TextToSpeech`.
Dlaczego to jest właściwy wybór na dziś (2026-09):

- Mowa-do-mowy bez pośredniego tekstu → naturalna prozodia, dużo niższe
  opóźnienie, prawdziwe przerywanie (barge-in) obsługiwane natywnie przez
  serwerowy VAD.
- **Równoległe wywoływanie narzędzi (parallel tool calling) w trakcie
  mówienia** — model może po cichu wywołać funkcję *nie przerywając* swojej
  wypowiedzi (to jest mechanizm, którym rozwiążemy "podkreślanie na żywo" bez
  zamieniania appki w ciągłe przerywanie).
- Kontekst 128K, konfigurowalny wysiłek rozumowania — starcza na długi system
  prompt z pełną historią błędów + fragment własnego tekstu do dyskusji.
- Koszt: ok. **$1.15/h słuchania + $4.61/h mówienia** przy domyślnych
  ustawieniach (czyli ~$5–6 za godzinę żywej rozmowy) — dla użytku
  jednoosobowego akceptowalne
  ([Forasoft](https://www.forasoft.com/blog/article/openai-realtime-api-pricing-2026),
  [HackerNoon](https://hackernoon.com/openai-realtime-api-pricing-in-2026-real-world-data-from-4000-measured-sessions)).
  Do lżejszych sesji (dyskusja o tekście, mniej krytyczna na lag) opcjonalnie
  `gpt-realtime-2.1-mini` — tańszy wariant tej samej rodziny.

### 4.2 Mechanizm "podkreślania błędów na żywo"

Dwa function toole zdefiniowane w sesji Realtime, wywoływane przez model
**w trakcie** rozmowy, bez przerywania jej głosem:

```
flag_mistake(
  original: string,        // dokładnie to, co user powiedział
  corrected: string,       // poprawna forma
  category: "grammar" | "vocabulary" | "pronunciation" | "fluency" | "register",
  severity: "minor" | "meaning_affecting",
  explanation: string,     // 1 zdanie, po polsku albo po angielsku (do ustawienia)
)

test_focus_item(
  mistake_bank_id: string, // który znany, wcześniejszy błąd właśnie przetestowano
  succeeded: boolean,      // czy user tym razem użył formy poprawnie
)
```

- Wywołanie `flag_mistake` **nie generuje odpowiedzi głosowej** — to czysto
  "boczny kanał" do zapisu w bazie. Model dostaje w system prompt instrukcję:
  *"Whenever the user makes a grammar/vocabulary mistake, silently call
  flag_mistake before continuing your spoken reply. Only interrupt your
  speech to correct aloud if the mistake blocks understanding, or roughly
  once every 4–5 minor mistakes using elicitation ('are you sure about that
  verb?') rather than just restating the correct form."* — to bezpośrednio
  wdraża wnioski z §3 (prompty > recasty, nie przerywać za każdym razem).
- Aplikacja renderuje w UI listę wypowiedzi live (transkrypt z
  `input_audio_transcription`, który Realtime API i tak emituje równolegle do
  audio) i **podkreśla na czerwono fragment**, gdy przyjdzie zdarzenie
  `flag_mistake` powiązane z tym fragmentem czasowo — to jest to
  "automatyczne podkreślanie zdań podczas rozmowy" z Twojego opisu.
- `test_focus_item` zamyka pętlę: kiedy model celowo sprawdza znany, stary
  błąd (patrz §5) i user go tym razem powie dobrze, stan w banku pomyłek się
  aktualizuje (interwał SM-2 rośnie) — to jest "did I actually fix this?", nie
  tylko "did I flag this once?".

### 4.3 Niezawodność function-callingu w mowie

Model czasem pominie wywołanie narzędzia w środku płynnej wypowiedzi
(realny, znany problem agentów głosowych). Zabezpieczenie: równolegle do
sesji Realtime, **po zakończeniu każdej tury (nie całej rozmowy)**, tani
przebieg tekstowy (`gpt-5-mini` na samej transkrypcji tury) jako siatka
bezpieczeństwa — dogania błędy pominięte na żywo, zanim user zdąży
zapomnieć kontekstu. To jest tańsze niż się wydaje: sama transkrypcja jednej
wypowiedzi to grosze tekstowego tokena, nie audio.

## 5. Bank pomyłek (Mistake Bank) — trwałość i realne ponowne użycie

### 5.1 Model danych (nowa tabela Room, obok istniejącego `UserError`)

```
MistakeBankItem
- id
- category            (grammar | vocabulary | pronunciation | fluency | register)
- pattern             // np. "past perfect vs past simple", "third conditional",
                       // "collocation: make vs do"
- firstSeenExample    // oryginalne zdanie usera z pierwszego wystąpienia
- correctedExample
- explanation
- timesFlagged         Int
- timesTestedSuccess   Int
- timesTestedFail      Int
- srs: SrsState        // REUŻYCIE istniejącego SM-2 z core/domain — ten sam
                       // algorytm co dla słówek, inny obiekt do planowania
- domainTags           // np. ["business", "contracts"] — powiązanie z §7
- active               Boolean // wyłączane ręcznie, gdy uznasz "już umiem"
```

`UserError` (istniejące) zostaje jako log surowych zdarzeń z pojedynczej
rozmowy; `MistakeBankItem` to zagregowany, żyjący między sesjami "wzorzec
błędu", który faktycznie steruje przyszłymi rozmowami — to rozróżnienie jest
kluczowe: bez agregacji "trzy razy pomyliłem past perfect w trzech różnych
rozmowach" wygląda jak trzy niepowiązane zdarzenia, a nie jeden wzorzec do
przećwiczenia.

### 5.2 Jak wraca do kolejnej rozmowy

Przy starcie każdej sesji Realtime:

1. Pobierz z `MistakeBankItem` top N (np. 5–8) pozycji z `srs.dueAt <= now`,
   posortowanych po `category`/`domainTags` pasujących do wybranego tematu
   sesji.
2. Wstrzyknij do system prompt jako **"focus list"**, jawnie z instrukcją
   *"Over the course of this conversation, try to naturally create at least
   2–3 opportunities for the user to use these specific structures. When you
   do, call test_focus_item with the result. Do not read this list to the
   user or make it feel like a test."*
3. To jest dokładnie "retrieval practice wpleciony w rozmowę" z §3 — ćwiczenie
   przez użycie w naturalnym kontekście, nie osobny quiz.

## 6. Tryb "wklej tekst i porozmawiajmy o nim"

- Ekran: pole tekstowe (wklejenie własnego artykułu/dokumentu/maila) + wybór
  poziomu (B1/B2/C1) + opcjonalny wybór dziedziny (§7).
- System prompt sesji: pełny tekst jako kontekst + instrukcja *"Discuss this
  text with the user at [level]. Ask comprehension and opinion questions,
  introduce relevant vocabulary from the text, correct mistakes per the
  standard rules above."*
- Poziom **B1/B2/C1 kontroluje nie tylko trudność słownictwa tutora, ale i
  próg, od którego coś jest "błędem wartym flagowania"** — np. na B1 nie
  flagujemy subtelnych błędów rejestru, na C1 flagujemy nawet drobne
  nienaturalności kolokacyjne. To rozwiązuje słabość #5 z SWOT (poziom
  per-sesja, nie globalny).

## 7. Domain packs (tematyka specjalistyczna)

Zestaw startowy (łatwo rozszerzalny — to tylko dodatkowy plik z promptem +
słownictwem, nie nowy kod):

- **Prawo** — negocjacje kontraktowe, rozmowa z klientem, terminologia
  umowna.
- **Medioznawstwo / dziennikarstwo** — wywiad, pytania kontrolne, retoryka.
- **Biznes (per branża)** — np. SaaS/sprzedaż, negocjacje handlowe,
  prezentacja produktu, rozmowa inwestorska — konfigurowalne jako dowolny
  tekst opisujący branżę, nie sztywna lista.
- **Custom** — użytkownik wpisuje własny opis kontekstu ("rozmowa
  kwalifikacyjna na stanowisko X w firmie Y") — persona i słownictwo
  generowane na podstawie tego opisu, nie z gotowej listy.

Każdy pakiet to: nazwa, opis kontekstu (wstrzykiwany do system prompt),
opcjonalna lista kluczowego słownictwa/zwrotów do wplecenia, domyślny
poziom CEFR. Przechowywane lokalnie w Room, edytowalne w appce (nie trzeba
rekompilować, żeby dodać nową dziedzinę).

## 8. Kluczowe user flow

1. **Rozmowa swobodna z tematem** — wybierasz domain pack (albo "swobodna"),
   poziom, appka startuje sesję Realtime z wstrzykniętą top-N listą pomyłek
   do przećwiczenia. W trakcie: transkrypt na żywo z podkreślonymi błędami.
   Po sesji: krótkie podsumowanie (co poszło dobrze, co trafiło do banku
   pomyłek, co zostało przetestowane skutecznie).
2. **Dyskusja o tekście** — wklejasz tekst, appka od razu zaczyna sesję
   głosową na jego temat na wybranym poziomie.
3. **Przegląd banku pomyłek** — lista wzorców błędów z historią (ile razy
   flagowany, ile razy przetestowany skutecznie), możliwość ręcznego
   wyłączenia pozycji ("już to ogarniam").
4. **Zarządzanie domain packs** — dodawanie/edycja własnych tematyk.

## 9. Zakres MVP — co wchodzi, co świadomie zostaje poza

**W zakresie:**
- Sesja głosowa oparta o Realtime API z live flaggingiem błędów.
- Bank pomyłek z SM-2 i wstrzykiwaniem "focus list" do kolejnych sesji.
- Tryb dyskusji o wklejonym tekście.
- Domain packs (startowy zestaw + własne).
- Transkrypt live z podkreśleniami w UI.
- Lokalna baza (Room), lokalny klucz API — bez backendu, bez kont (zgodnie z
  "appka tylko dla mnie").

**Świadomie poza MVP** (można dodać później, nie blokuje nauki):
- Wiele użytkowników / logowanie / synchronizacja w chmurze.
- Płatności / paywall.
- Analiza wymowy na poziomie fonemów (Realtime model ocenia sens i
  poprawność gramatyczną z transkryptu, nie dokładność fonetyczną — to inny
  problem techniczny, ewentualnie osobny moduł na Whisper + phoneme scoring
  później).
- Statystyki/wykresy poza tym, co potrzebne do pokazania postępu w banku
  pomyłek (bez rozbudowanego modułu `statistics` z v1 — można go podłączyć
  później, jeśli zabraknie).

## 10. Co reużyć z obecnego repo, co wymienić

| Warstwa | Decyzja |
|---|---|
| `core/domain` (modele, use case'y, SM-2) | Reużyć w całości, dodać `MistakeBankItem` i use case'y do niego |
| `core/database`, `core/datastore` | Reużyć, dodać tabelę/DAO dla `MistakeBankItem` i `DomainPack` |
| `core/audio` (`SpeechRecognizer`/`TextToSpeech`) | **Zastąpić** nowym `core/realtime` (klient WebRTC/WebSocket do Realtime API) |
| `core/ai` (`OpenAiTutorService` na Chat Completions) | Zastąpić `RealtimeTutorService` — sesja, function calling, event stream; `analyzeConversation`/`generateExercises` z v1 mogą zostać jako fallback/dodatek offline |
| `feature/conversation`, `feature/onboarding`, `feature/learningpath`, `feature/chat`, `feature/profile` (z redesignu Liquid Glass) | Nowy, znacznie prostszy zestaw ekranów pod jednego użytkownika — większość UI z v1/v2 (gamifikacja, paywall, wiele zakładek) odpada jako zbędna dla appki osobistej |
| Wizualny system "Liquid Glass" (`core/designsystem/glass`) | Można zachować jako warstwę wizualną — nie ma konfliktu z nowym rdzeniem głosowym |

**Rekomendacja:** nowy moduł `core/realtime` + `feature/livecoach` (jeden,
scentralizowany ekran: transkrypt + mikrofon + boczny panel "focus list" na
tę sesję) zamiast rozbudowanej nawigacji z 5 zakładek — to nie jest appka do
przeglądania, tylko appka do rozmawiania.

## 11. Plan wdrożenia (kroki)

1. **Proof of concept: klient Realtime API w Kotlinie.** WebSocket do
   `gpt-realtime-2.1`, strumieniowanie audio z mikrofonu, odtwarzanie audio
   zwrotnego, bez żadnego UI poza logami — potwierdzić, że nagrywanie/
   odtwarzanie na Androidzie (AudioRecord/AudioTrack) działa z formatem,
   jakiego oczekuje API (PCM16, 24kHz).
2. **Function calling: `flag_mistake` do konsoli.** Podłączyć narzędzie,
   zweryfikować w praktyce jak często model faktycznie je wywołuje przy
   różnych sformułowaniach system promptu (to wymaga iteracji promptu, nie
   tylko kodu).
3. **Model danych `MistakeBankItem` + zapis z `flag_mistake`.**
4. **UI: live transkrypt z podkreśleniami.**
5. **Wstrzykiwanie focus list przy starcie sesji + `test_focus_item`.**
6. **Tryb dyskusji o tekście.**
7. **Domain packs (startowy zestaw + edycja własnych).**
8. **Bezpiecznik: tekstowy re-pass po turze** (§4.3), jeśli w praktyce okaże
   się, że model pomija za dużo błędów na żywo.

## 12. Ryzyka

- **Koszt przy częstym korzystaniu.** ~$5–6/h — przy codziennych,
  długich sesjach to realny miesięczny koszt do zaakceptowania świadomie
  (dla porównania: v1 na Chat Completions był rzędu grosze/rozmowę, ale bez
  natywnej mowy i live feedbacku).
- **Jakość function-callingu w strumieniu mowy nie jest 100% pewna** —
  dlatego §4.3 (bezpiecznik tekstowy) jest częścią MVP, nie opcjonalnym
  dodatkiem.
- **Za dużo korekt na żywo psuje płynność rozmowy** — dlatego domyślna
  strategia to *ciche flagowanie + rzadka, celowa korekta głosowa*, zgodnie
  z badaniami z §3, a nie "poprawiaj wszystko natychmiast".
- **API Realtime i jego pricing zmieniają się szybko** (kilka wersji modelu
  w 2026 samych: gpt-realtime, -2, -2.1) — warto izolować klienta za
  interfejsem domenowym (analogicznie do `AiTutorService` w v1), żeby zmiana
  modelu/dostawcy nie dotykała UI.

## Źródła

- [Li, S. (2010). The Effectiveness of Corrective Feedback in SLA: A Meta-Analysis](https://www.researchgate.net/publication/229940242_The_Effectiveness_of_Corrective_Feedback_in_SLA_A_Meta-Analysis)
- [Lyster — Oral Feedback in Classroom SLA: A Meta-Analysis](https://www.cambridge.org/core/journals/studies-in-second-language-acquisition/article/abs/oral-feedback-in-classroom-sla/4999EE1C8379B2BF026B148EAF373CA1)
- [Personalized Language Learning With an LLM Chatbot: Effects of Immediate vs. Delayed Corrective Feedback (Frontiers in Education, 2026)](https://www.frontiersin.org/journals/education/articles/10.3389/feduc.2026.1703664/full)
- [Testing the Noticing Function of the Output Hypothesis](https://files.eric.ed.gov/fulltext/EJ1095572.pdf)
- [What Changes When the Interlocutor Is an AI? Interactional Fluency and Linguistic Uptake in L2 Spoken Dialogue (EDM 2026)](https://educationaldatamining.org/edm2026/proceedings/2026.EDM.full-papers.210/index.html)
- [Unlocking words and fluency: Spaced Retrieval practice with A1 EFL adult learners (Frontiers, 2025)](https://www.frontiersin.org/journals/education/articles/10.3389/feduc.2025.1715111/full)
- [Kim et al. (2022). The Effects of Spaced Practice on Second Language Learning: A Meta-Analysis](https://onlinelibrary.wiley.com/doi/10.1111/lang.12479)
- [Investigating the role of AI-powered conversation bots in enhancing L2 speaking skills and reducing speaking anxiety (Humanities and Social Sciences Communications, 2025)](https://www.nature.com/articles/s41599-025-05550-z)
- [Introducing gpt-realtime and Realtime API updates for production voice agents (OpenAI)](https://openai.com/index/introducing-gpt-realtime/)
- [OpenAI Realtime API Pricing 2026: Real-World Data From 4,000 Measured Sessions (HackerNoon)](https://hackernoon.com/openai-realtime-api-pricing-in-2026-real-world-data-from-4000-measured-sessions)
- [OpenAI Realtime API Pricing: The Real Cost Per Minute (Forasoft)](https://www.forasoft.com/blog/article/openai-realtime-api-pricing)
