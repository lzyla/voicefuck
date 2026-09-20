# ETAP 8 — Projekt Figma (build plan)

> Ten dokument to kompletna specyfikacja odtworzenia projektu **AI English Coach**
> w Figma — dostarczamy plan budowy pliku, nie sam plik. Wartości (kolory,
> typografia, spacing, komponenty, ekrany) są 1:1 zgodne z
> `docs/00-brief-decyzje-projektowe.md` oraz `docs/07-ui-design-system.md`.

Device referencyjny wszystkich frame'ów ekranów: **Pixel 8 — 412 × 915** (dp).

---

## 1. Struktura pliku

Jeden plik Figma: **"AI English Coach — Design"**, pages w kolejności:

| Page | Zawartość |
|---|---|
| `🎨 Foundations` | zmienne (kolory, spacing, radius, typografia), style tekstowe, siatki, elevation, dokumentacja tokenów, ikonografia (Material Symbols Rounded) |
| `🧩 Components` | wszystkie komponenty kanoniczne z wariantami + komponenty pomocnicze (NavigationBar, chipy, list itemy, wykresy) |
| `📱 Screens Light` | 22 frame'y ekranów w motywie jasnym |
| `📱 Screens Dark` | 22 frame'y ekranów w motywie ciemnym (te same layouty, tryb zmiennych: Dark) |
| `🔗 Prototype` | kopie kluczowych ekranów połączone flow (onboarding, rozmowa, nauka) + diagram przepływów |

Zasada: ekrany budujemy **wyłącznie z instancji komponentów** z page `🧩 Components`
i zmiennych z `🎨 Foundations`. Zero "odklejonych" kolorów i tekstów stylowanych ręcznie.

---

## 2. Zmienne Figma (Variables)

### 2.1 Collection `color/primitive` (1 mode)

Prymitywy — surowe hexy palety marki (Tailwind-owe tony indygo/teal/bursztyn/czerwieni + neutrale):

| Zmienna | Wartość |
|---|---|
| `indigo/50` | `#EEF2FF` |
| `indigo/100` | `#E0E7FF` |
| `indigo/300` | `#A5B4FC` |
| `indigo/600` | `#4F46E5` |
| `indigo/800` | `#3730A3` |
| `indigo/900` | `#312E81` |
| `indigo/950` | `#1E1B4B` |
| `teal/100` | `#CCFBF1` |
| `teal/300` | `#5EEAD4` |
| `teal/500` | `#14B8A6` |
| `teal/700` | `#0F766E` |
| `teal/900` | `#134E4A` |
| `amber/100` | `#FEF3C7` |
| `amber/300` | `#FCD34D` |
| `amber/500` | `#F59E0B` |
| `amber/700` | `#B45309` |
| `amber/900` | `#78350F` |
| `red/100` | `#FEE2E2` |
| `red/200` | `#FECACA` |
| `red/300` | `#FCA5A5` |
| `red/600` | `#DC2626` |
| `red/800` | `#991B1B` |
| `red/900` | `#7F1D1D` |
| `neutral/0` | `#FFFFFF` |
| `neutral/bg-light` | `#FAFAFF` |
| `neutral/10` | `#1B1B22` |
| `neutral/surface-dark` | `#121218` |
| `neutral/surface-dark-2` | `#1C1C24` |
| `neutral/1000` | `#000000` |

(oraz pozostałe neutrale powierzchni z tabeli 3.1 etapu 7: `#F4F3FA`, `#EEEDF4`,
`#E8E7EF`, `#E2E1E9`, `#E4E1F0`, `#C8C5D0`, `#78767F`, `#918F9A`, `#47464F`,
`#303036`, `#F2F0F7`, `#E4E1E9`, `#0D0D12`, `#20202A`, `#2A2A35`, `#35353F` —
nazwane `neutral/surface-*` wg roli).

### 2.2 Collection `color/semantic` (2 modes: **Light**, **Dark**)

Aliasy na prymitywy — nazwy identyczne z rolami Material 3. To jedyna kolekcja,
z której korzystają komponenty i ekrany.

| Zmienna | Mode: Light | Mode: Dark |
|---|---|---|
| `primary` | `indigo/600` (#4F46E5) | `indigo/300` (#A5B4FC) |
| `onPrimary` | `neutral/0` (#FFFFFF) | `indigo/900` (#312E81) |
| `primaryContainer` | `indigo/100` (#E0E7FF) | `indigo/800` (#3730A3) |
| `onPrimaryContainer` | `indigo/950` (#1E1B4B) | `indigo/100` (#E0E7FF) |
| `secondary` | `teal/500` (#14B8A6) | `teal/300` (#5EEAD4) |
| `onSecondary` | `neutral/0` (#FFFFFF) | `teal/900` (#134E4A) |
| `secondaryContainer` | `teal/100` (#CCFBF1) | `teal/700` (#0F766E) |
| `onSecondaryContainer` | `teal/900` (#134E4A) | `teal/100` (#CCFBF1) |
| `tertiary` | `amber/500` (#F59E0B) | `amber/300` (#FCD34D) |
| `onTertiary` | `neutral/0` (#FFFFFF) | `amber/900` (#78350F) |
| `tertiaryContainer` | `amber/100` (#FEF3C7) | `amber/700` (#B45309) |
| `onTertiaryContainer` | `amber/900` (#78350F) | `amber/100` (#FEF3C7) |
| `error` | `red/600` (#DC2626) | `red/300` (#FCA5A5) |
| `onError` | `neutral/0` (#FFFFFF) | `red/900` (#7F1D1D) |
| `errorContainer` | `red/100` (#FEE2E2) | `red/800` (#991B1B) |
| `onErrorContainer` | `red/900` (#7F1D1D) | `red/100` (#FEE2E2) |
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
| `inversePrimary` | `indigo/300` | `indigo/600` |
| `scrim` | `neutral/1000` | `neutral/1000` |

Frame'y na page `📱 Screens Dark` mają ustawiony mode kolekcji `color/semantic`
na **Dark** — wszystkie instancje przełączają się automatycznie.

### 2.3 Collection `spacing` (1 mode, typ: number)

| Zmienna | Wartość |
|---|---|
| `space/1` | 4 |
| `space/2` | 8 |
| `space/3` | 12 |
| `space/4` | 16 |
| `space/6` | 24 |
| `space/8` | 32 |
| `touch/min` | 48 |
| `screen/padding-h` | 16 |

### 2.4 Collection `radius` (1 mode, typ: number)

| Zmienna | Wartość | Użycie |
|---|---|---|
| `radius/card` | 12 | CoachCard, StatTile, ErrorBanner |
| `radius/sheet` | 16 | bottom sheets, hero, fiszki, MessageBubble |
| `radius/bubble-tail` | 4 | "ogonek" MessageBubble |
| `radius/full` | 999 | FAB, przyciski, MicButton, chipy, LevelChip |
| `radius/bar` | 4 | słupki wykresów, progress bar |

### 2.5 Collection `typography` (1 mode; number + string)

| Zmienna | Wartość |
|---|---|
| `font/family` | `Inter` |
| `font/size/display-s` | 36 |
| `font/size/headline-l` | 32 |
| `font/size/headline-m` | 28 |
| `font/size/headline-s` | 24 |
| `font/size/title-l` | 22 |
| `font/size/title-m` | 16 |
| `font/size/title-s` | 14 |
| `font/size/body-l` | 16 |
| `font/size/body-m` | 14 |
| `font/size/body-s` | 12 |
| `font/size/label-l` | 14 |
| `font/size/label-m` | 12 |
| `font/size/label-s` | 11 |
| `font/weight/regular` | 400 |
| `font/weight/medium` | 500 |
| `font/weight/semibold` | 600 |

(pełne line height wg tabeli 3.2 etapu 7 — ustawiane w stylach tekstowych, p. 3).

---

## 3. Style tekstowe i kolorystyczne

### 3.1 Style tekstowe (Text styles) — font **Inter**

Nazewnictwo `M3/<grupa>/<rozmiar>`; wartości z etapu 7 (tabela 3.2):

| Styl Figma | Size / Line height | Weight | Letter spacing |
|---|---|---|---|
| `M3/display/large` | 57 / 64 | 400 | -0.25 |
| `M3/display/medium` | 45 / 52 | 400 | 0 |
| `M3/display/small` | 36 / 44 | 500 | 0 |
| `M3/headline/large` | 32 / 40 | 600 | 0 |
| `M3/headline/medium` | 28 / 36 | 600 | 0 |
| `M3/headline/small` | 24 / 32 | 600 | 0 |
| `M3/title/large` | 22 / 28 | 600 | 0 |
| `M3/title/medium` | 16 / 24 | 600 | 0.15 |
| `M3/title/small` | 14 / 20 | 600 | 0.1 |
| `M3/body/large` | 16 / 24 | 400 | 0.5 |
| `M3/body/medium` | 14 / 20 | 400 | 0.25 |
| `M3/body/small` | 12 / 16 | 400 | 0.4 |
| `M3/label/large` | 14 / 20 | 500 | 0.1 |
| `M3/label/medium` | 12 / 16 | 500 | 0.5 |
| `M3/label/small` | 11 / 16 | 500 | 0.5 |

Dodatkowe: `Mono/body-large` (Roboto Mono 16/24, klucz API, IPA) oraz warianty
`…/tabular` z włączonym OpenType `tnum` (timery, statystyki).

### 3.2 Style kolorystyczne / efekty

- Kolory wyłącznie przez zmienne `color/semantic` (nie tworzymy osobnych color
  styles — zmienne z modes zastępują je w M3 workflow).
- **Effect styles (elevation M3):**
  - `Elevation/Level 1` — shadow y=1, blur=2, `scrim` 15%
  - `Elevation/Level 2` — shadow y=1, blur=3 + y=2, blur=6, `scrim` 15%
  - `Elevation/Level 3` — shadow y=4, blur=8, `scrim` 20%
  - W trybie Dark elevation oddawana tłem `surfaceContainer*` (bez cieni poza FAB/dialogami).
- **Grid styles:** `Grid/4dp` (siatka 4), `Grid/Screen` (kolumna 1, margins 16) —
  przypięte do wszystkich frame'ów ekranów.

---

## 4. Komponenty z wariantami (Component properties + Auto Layout)

Wszystkie komponenty na page `🧩 Components`, w sekcjach (Figma sections):
*Buttons*, *Cards*, *Voice*, *Chat*, *Data display*, *Chips*, *Feedback*, *Navigation*.

Konwencja properties: `property=wartość1|wartość2` (Variant), `#tekst` (Text prop),
`◇slot` (Instance swap), `showX` (Boolean).

### 4.1 CoachPrimaryButton

- **Properties:** `size=fullWidth|compact` · `state=enabled|disabled|loading|pressed`
  · `#label` (Text) · `showIcon` (Boolean) · `◇icon` (Instance swap, domyślnie `mic`)
- **Auto Layout:** horizontal; padding 24/16 (fullWidth, h=56) lub 16/10
  (compact, h=40); gap `space/2`; wyrównanie center/center; fill `primary`,
  radius `radius/full`; fullWidth → szerokość "Fill container".
- Stany: disabled — fill `onSurface` 12%, tekst `onSurface` 38%; loading — label
  podmieniony na instancję `LoadingIndicator/circular-18`; pressed — nakładka
  `onPrimary` 12%.

### 4.2 CoachCard

- **Properties:** `variant=filled|outlined|highlight` · `state=default|selected|disabled`
  · `tone=primary|secondary|tertiary` (dla highlight) — treść jako slot
  (instance swap `◇content` lub komponent bazowy z children).
- **Auto Layout:** vertical; padding `space/4` (16); gap `space/3` (12); radius
  `radius/card` (12); fill `surfaceContainerLow` (filled) / `surface` + stroke
  `outlineVariant` 1 (outlined) / `*Container` wg `tone` (highlight);
  selected → stroke `primary` 2.

### 4.3 CoachTopBar

- **Properties:** `variant=root|child|modal` · `#title` · `showActions` ·
  `◇action1`, `◇action2` (Instance swap na IconButton)
- **Auto Layout:** horizontal; h=64; padding 4/8 (ikony mają własne 48dp hit
  area), gap `space/1`; tytuł "Fill container", styl `M3/title/large`;
  fill `surface`. `child` → leading `arrow_back`; `modal` → leading `close`;
  `root` → bez leading, tytuł z paddingiem 16.

### 4.4 MicButton ⭐ (klucz do smart animate)

- **Properties:** `state=idle|listening|processing|speaking` · `size=80|72|64`
- **Budowa (warstwy o STAŁYCH nazwach — warunek działania Smart animate):**
  1. `ring-outer` (elipsa, tylko listening: skala 160%, `primary` 0% — klatka
     końcowa fali; w idle: 100%, opacity 0)
  2. `ring-inner` (elipsa, listening: 130%, `primary` 15%)
  3. `container` (elipsa 80/72/64; fill: idle `primaryContainer`, listening
     `primary`, processing `secondaryContainer`, speaking `secondary`)
  4. `icon` (ikona 32: `mic` / `mic` / `hourglass_top` / `graphic_eq`; kolor
     on-rola odpowiednia dla kontenera)
  5. `progress-arc` (łuk 3, `secondary`, widoczny tylko processing)
- **Auto Layout:** brak (absolute w kontenerze stałym `size`×`size`+40 na pierścienie);
  zawartość wyśrodkowana constraints center/center.
- Nad przyciskiem osobny komponent `MicButton/EqualizerBars` (5 prostokątów 4dp,
  gap 4, wysokości 8/16/24/12/6, fill `primary`, radius `radius/bar`) używany na
  ekranach w stanie listening.

### 4.5 MessageBubble

- **Properties:** `variant=user|assistant` · `state=default|streaming|error` ·
  `showTranslation` · `showAudio` (assistant) · `#text` · `#translation` · `#time`
- **Auto Layout:** vertical; padding 12/16; gap `space/1`; max width 320
  (80% z 412 minus marginesy); radius: 16/16/16/4 (assistant — dół-lewy 4) lub
  16/16/4/16 (user — dół-prawy 4); fill `primary` (user) /
  `surfaceContainerHigh` (assistant). Streaming → sufiks "▊" w tekście;
  error → stroke `error` 1 + akcja "Ponów".

### 4.6 StatTile

- **Properties:** `tone=primary|secondary|tertiary` · `state=default|loading` ·
  `#value` · `#label` · `showDelta` · `#delta` · `showAction` · `#action` · `◇icon`
- **Auto Layout:** vertical; padding `space/4`; gap `space/2`; min h=96; radius
  `radius/card`; fill `surfaceContainerLow`; ikona w kółku 40 (`*Container` wg
  tone); wartość `M3/headline/small`; loading → prostokąty skeleton.

### 4.7 ProgressRing

- **Properties:** `size=48|64|72|120` · `tone=primary|secondary|tertiary|error` ·
  `progress=0|25|50|75|100` (warianty łuku) · `#value` · `#caption`
- **Budowa:** elipsa-tor (stroke `surfaceVariant` 6, round cap) + łuk (arc,
  stroke wg `tone`); tekst centralny Auto Layout vertical, gap 0.

### 4.8 LevelChip

- **Properties:** `level=A2|B1|B2|C1` · `variant=outlined|selected|display`
- **Auto Layout:** horizontal; h=32; padding 12/6; radius `radius/full`;
  outlined — stroke `outline`, tekst `onSurfaceVariant`; selected — fill
  `primary`, tekst `onPrimary`; display — fill `primaryContainer`, tekst
  `onPrimaryContainer`. Styl `M3/label/large`.

### 4.9 EmptyState

- **Properties:** `◇icon` · `#title` · `#description` · `showAction` · `#action`
- **Auto Layout:** vertical; padding `space/8`; gap `space/4`; align center;
  ikona 64 `onSurfaceVariant` 60%.

### 4.10 LoadingIndicator

- **Properties:** `variant=circular|dots|skeleton` · `size=18|32|48` (circular)
- circular: łuk `primary` stroke 4; dots: 3 elipsy 8 gap 8 (`primary`, opacity
  100/60/30 jako klatka animacji); skeleton: prostokąt fill
  `surfaceContainerHigh`, radius 8.

### 4.11 ErrorBanner

- **Properties:** `variant=inline|top` · `#message` · `showAction` · `#action`
- **Auto Layout:** horizontal; padding 12/16; gap `space/3`; radius `radius/card`
  (inline) / 0 (top); fill `errorContainer`; ikona `error` 24 `onErrorContainer`.

### 4.12 Komponenty pomocnicze

- `NavigationBar` — Auto Layout horizontal, h=80, fill `surfaceContainer`;
  property `active=home|conversations|practice|statistics`; item: ikona w pigułce
  64×32 (`secondaryContainer` gdy aktywny, fill 1) + label `M3/label/medium`.
- `FilterChip` (`state=default|selected`, `#label`, `showIcon`), `AssistChip`,
  `SuggestionChip` — wg M3.
- `ListItem` (`variant=oneLine|twoLine|threeLine`, `◇leading`, `◇trailing`,
  `#headline`, `#supporting`) — Settings, Vocabulary, VoiceNotes.
- `SegmentedButton` (`segments=2|3`, `active=1|2|3`, text props).
- `Slider` (`value=0|25|50|75|100`), `Switch` (`state=on|off`), `FAB`
  (`variant=regular|extended`), `SearchBar`, `TextField`
  (`state=default|focus|error|disabled`, `showTrailing`).
- `Chart/Bars7` (7 słupków, property `highlight=none|today`), `Chart/Line`
  (wektor linii + gradient fill) — StatisticsScreen.
- `Icon/…` — zestaw ikon Material Symbols Rounded (24dp, komponenty do
  instance swap; pełna lista per ekran w etapie 7, tabela 3.3).

---

## 5. Frame'y ekranów (📱 Screens Light / 📱 Screens Dark)

Każdy frame: **412 × 915** (Pixel 8), grid `Grid/Screen`, fill `background`,
Auto Layout vertical (sekcje), nazwa = `route` z briefu. Obie pages zawierają
identyczny zestaw — 22 frame'y; na Dark przełączony mode `color/semantic → Dark`.

| # | Nazwa frame'u | Route | Kluczowe instancje |
|---|---|---|---|
| 01 | `splash` | `splash` | logo, LoadingIndicator/dots |
| 02 | `onboarding-welcome` | `onboarding/welcome` | hero, page indicator, CoachPrimaryButton |
| 03 | `onboarding-level` | `onboarding/level` | CoachTopBar/child, 4× CoachCard(selected demo), LevelChip |
| 04 | `onboarding-goals` | `onboarding/goals` | FilterChip×6, Slider, CoachPrimaryButton |
| 05 | `onboarding-permissions` | `onboarding/permissions` | 2× CoachCard, OutlinedButton/check |
| 06 | `onboarding-api` | `onboarding/api` | TextField(password), CoachCard/highlight-secondary, ErrorBanner (wariant demo) |
| 07 | `home` | `home` | CoachTopBar/root, ProgressRing/72, CoachCard/highlight-primary (hero), 2× StatTile, NavigationBar(active=home) |
| 08 | `conversations` | `conversations` | SearchBar, CoachCard×3, FAB/extended, NavigationBar(active=conversations) |
| 09 | `conversation` | `conversation/{id}` | CoachTopBar/child, MessageBubble×3 (user/assistant/streaming), MicButton(state=listening,size=80), EqualizerBars |
| 10 | `conversation-summary` | `conversation/{id}/summary` | CoachCard/highlight, karty błędów, SuggestionChip×5, CoachPrimaryButton |
| 11 | `practice` | `practice` | CoachCard/highlight-tertiary (due), 4 kafle CoachCard, NavigationBar(active=practice) |
| 12 | `practice-exercises` | `practice/exercises` | SegmentedButton, CoachCard×3 z chipami typów, CoachPrimaryButton sticky |
| 13 | `practice-exercises-session` | `practice/exercises/session` | progress bar, karty opcji (4 stany), panel feedbacku, CoachPrimaryButton |
| 14 | `practice-vocabulary` | `practice/vocabulary` | SearchBar, FilterChip×4, ListItem×4 (statusy NEW/LEARNING/MASTERED), CoachPrimaryButton |
| 15 | `practice-vocabulary-review` | `practice/vocabulary/review` | fiszka (2 warianty frame: front/back), 4 przyciski ocen SM-2 |
| 16 | `practice-pronunciation` | `practice/pronunciation` | karta słowa (IPA), MicButton(size=80), karta wyniku z ProgressRing/64, ListItem×2 |
| 17 | `practice-notes` | `practice/notes` | ListItem×3 (play/pause/transkrypcja-w-toku), MicButton(state=idle,size=72) |
| 18 | `statistics` | `statistics` | SegmentedButton, 4× StatTile, Chart/Bars7, Chart/Line, CoachCard (raport AI), NavigationBar(active=statistics) |
| 19 | `settings` | `settings` | CoachTopBar/child, grupy ListItem, Switch, LevelChip/display |
| 20 | `settings-ai` | `settings/ai` | dropdown, ListItem (klucz maskowany), SegmentedButton US/GB, Slider |
| 21 | `settings-profile` | `settings/profile` | LevelChip×4, FilterChip×6, Slider, CoachPrimaryButton |
| 22 | `settings-data` | `settings/data` | CoachCard podsumowania, ListItem eksportu, ListItem disabled + Badge "Wkrótce", pozycja delete w `error` |

Dodatkowe frame'y pomocnicze (Light): `conversation — mic states` (4 kopie strefy
dolnej: idle/listening/processing/speaking — źródło Smart animate),
`vocabulary-review — back` (tył fiszki), `exercises-session — feedback-error`,
`dialog — theme`, `sheet — scenario picker`, `dialog — delete confirm`.

---

## 6. Prototyp (page 🔗 Prototype)

Ustawienia: device **Pixel 8**, startowy frame `splash`, domyślne przejście
**Smart animate 300 ms, ease out**; zakładki — **Dissolve 250 ms** (imitacja
fade-through); push — **Move in (left→right) 400 ms** (shared axis X); modalne —
**Move in (bottom) 300 ms**.

### 6.1 Tabela połączeń

| Z ekranu | Element (trigger) | Akcja | Cel | Przejście |
|---|---|---|---|---|
| `splash` | After delay 1500 ms | Navigate | `onboarding-welcome` | Dissolve 200 |
| `onboarding-welcome` | CoachPrimaryButton "Zaczynamy" (On tap) | Navigate | `onboarding-level` | Move in left 400 |
| `onboarding-level` | CoachCard B1 (On tap) | Change to | wariant selected (w miejscu) | Smart animate 200 |
| `onboarding-level` | "Dalej" (On tap) | Navigate | `onboarding-goals` | Move in left 400 |
| `onboarding-goals` | "Dalej" (On tap) | Navigate | `onboarding-permissions` | Move in left 400 |
| `onboarding-permissions` | "Dalej" (On tap) | Navigate | `onboarding-api` | Move in left 400 |
| `onboarding-api` | "Zakończ konfigurację" (On tap) | Navigate | `home` | Dissolve 250 |
| `home` | ikona `settings` (On tap) | Navigate | `settings` | Move in left 400 |
| `home` | hero "Zacznij rozmowę" (On tap) | Open overlay | `sheet — scenario picker` | Move in bottom 300 |
| `sheet — scenario picker` | scenariusz (On tap) | Navigate | `conversation` | Move in bottom 300 |
| `home` | StatTile "12 słówek" (On tap) | Navigate | `practice-vocabulary-review` | Move in bottom 300 |
| `home` | NavigationBar: Rozmowy / Nauka / Statystyki (On tap) | Navigate | `conversations` / `practice` / `statistics` | Dissolve 250 |
| `conversations` | FAB (On tap) | Open overlay | `sheet — scenario picker` | Move in bottom 300 |
| `conversations` | karta rozmowy (On tap) | Navigate | `conversation-summary` | Move in left 400 |
| `conversation` | **MicButton (On tap)** | Navigate | `conversation — mic states` (kolejny stan) | **Smart animate 300, ease in-out** |
| `conversation — mic states` | idle→listening→processing→speaking (On tap, pętla) | Navigate | następny stan | **Smart animate 300** (pierścienie i kolory animują się dzięki stałym nazwom warstw) |
| `conversation` | `stop_circle` (On tap) | Open overlay | dialog "Zakończyć?" → Navigate | `conversation-summary` | Move in bottom 300 |
| `conversation-summary` | "Wygeneruj ćwiczenia" (On tap) | Navigate | `practice-exercises` | Move in left 400 |
| `conversation-summary` | "Wróć do Home" (On tap) | Navigate | `home` | Dissolve 250 |
| `practice` | kafel Ćwiczenia / Słówka / Wymowa / Notatki (On tap) | Navigate | `practice-exercises` / `practice-vocabulary` / `practice-pronunciation` / `practice-notes` | Move in left 400 |
| `practice-exercises` | "Rozpocznij serię" (On tap) | Navigate | `practice-exercises-session` | Move in bottom 300 |
| `practice-exercises-session` | opcja odpowiedzi (On tap) | Change to | wariant selected | Smart animate 200 |
| `practice-exercises-session` | "Sprawdź" (On tap) | Navigate | `exercises-session — feedback` | Smart animate 300 |
| `practice-vocabulary` | "Powtórz due" (On tap) | Navigate | `practice-vocabulary-review` | Move in bottom 300 |
| `practice-vocabulary-review` | fiszka (On tap) | Navigate | `vocabulary-review — back` | **Smart animate 400** (obrót przez skalowanie X) |
| `vocabulary-review — back` | przycisk oceny (On tap) | Navigate | `practice-vocabulary-review` (następna) | Move in left 300 |
| `practice-pronunciation` | MicButton (On tap) | Navigate | wariant z kartą wyniku | Smart animate 300 |
| `statistics` | SegmentedButton "Miesiąc" (On tap) | Change to | wariant zakresu | Smart animate 200 |
| `settings` | "Profil i cele" / "AI i głos" / "Dane i eksport" (On tap) | Navigate | `settings-profile` / `settings-ai` / `settings-data` | Move in left 400 |
| `settings-data` | "Usuń wszystkie dane" (On tap) | Open overlay | `dialog — delete confirm` | Dissolve 200 |
| wszystkie child | `arrow_back` / `close` (On tap) | Back | poprzedni | (systemowe Back) |

### 6.2 Smart animate dla MicButton — wymagania

1. Wszystkie 4 stany to warianty jednego component setu z **identycznymi nazwami
   warstw** (`ring-outer`, `ring-inner`, `container`, `icon`, `progress-arc`).
2. Fala: w prototypie pętla idle→listening przez "After delay 800 ms" między
   dwiema klatkami pierścieni (skala 100%→160%, opacity 30%→0%) — daje ciągłą
   animację fali.
3. Zmiana koloru kontenera i podmiana ikony animują się crossfade'em w ramach
   Smart animate 300 ms ease in-out.

---

## 7. Checklist wykonania (krok po kroku)

**Faza A — Foundations**
1. [ ] Utwórz plik "AI English Coach — Design" i 5 pages (sekcja 1).
2. [ ] Zainstaluj font Inter (Google Fonts) i Material Symbols Rounded (plugin lub SVG).
3. [ ] Utwórz collection `color/primitive` — wpisz wszystkie prymitywy (2.1).
4. [ ] Utwórz collection `color/semantic` z modes **Light/Dark**; podepnij aliasy wg tabeli 2.2.
5. [ ] Utwórz collections `spacing`, `radius`, `typography` (2.3–2.5).
6. [ ] Utwórz 15 stylów tekstowych `M3/…` + `Mono/…` (3.1); sprawdź line height i letter spacing.
7. [ ] Utwórz effect styles `Elevation/Level 1–3` i grid styles (3.2).
8. [ ] Zbuduj na `🎨 Foundations` planszę dokumentacyjną: paleta, typografia, spacing, radius, ikony.

**Faza B — Components**
9. [ ] Zaimportuj ikony z tabeli 3.3 etapu 7 jako komponenty `Icon/<nazwa>` (24dp).
10. [ ] Zbuduj komponenty pomocnicze: IconButton, chipy, ListItem, Switch, Slider, TextField, SegmentedButton, SearchBar, FAB, NavigationBar (4.12).
11. [ ] Zbuduj `CoachPrimaryButton` (4.1) — wszystkie property i stany.
12. [ ] Zbuduj `CoachCard` (4.2), `CoachTopBar` (4.3).
13. [ ] Zbuduj `MicButton` (4.4) — pilnuj stałych nazw warstw; dodaj `EqualizerBars`.
14. [ ] Zbuduj `MessageBubble` (4.5), `StatTile` (4.6), `ProgressRing` (4.7), `LevelChip` (4.8).
15. [ ] Zbuduj `EmptyState`, `LoadingIndicator`, `ErrorBanner` (4.9–4.11).
16. [ ] Zbuduj `Chart/Bars7` i `Chart/Line`.
17. [ ] Przetestuj każdy komponent przełączając mode Light/Dark — żaden kolor nie może być "odklejony" od zmiennych.

**Faza C — Screens**
18. [ ] Utwórz 22 frame'y 412×915 na `📱 Screens Light` wg tabeli w sekcji 5, w kolejności flow.
19. [ ] Zbuduj ekrany onboardingu (01–06), potem główne zakładki (07, 08, 11, 18), rozmowę (09, 10), naukę (12–17), ustawienia (19–22).
20. [ ] Dodaj frame'y pomocnicze (mic states, tył fiszki, feedback, dialogi, sheet).
21. [ ] Zduplikuj całą page jako `📱 Screens Dark`; ustaw mode `color/semantic → Dark` na wszystkich frame'ach; przejrzyj kontrasty i ilustracje.

**Faza D — Prototype**
22. [ ] Skopiuj kluczowe ekrany na `🔗 Prototype`; ustaw flow startowy `splash`.
23. [ ] Podłącz wszystkie połączenia z tabeli 6.1 (triggery, akcje, przejścia).
24. [ ] Skonfiguruj pętlę Smart animate stanów MicButton (6.2) i obrót fiszki.
25. [ ] Test na urządzeniu (Figma Mirror / prezentacja Pixel 8): onboarding → home → rozmowa → summary → ćwiczenia → fiszki → statystyki → ustawienia.

**Faza E — QA i handoff**
26. [ ] Audyt: wszystkie teksty = style, wszystkie kolory = zmienne, wszystkie odstępy = tokeny spacing (plugin typu "Design Lint").
27. [ ] Kontrast AA: pary rola/on-rola + tekst na kontenerach (plugin "Able"/"Stark"); pamiętaj o zasadzie tertiary z etapu 7 (bez małego tekstu na `tertiary`).
28. [ ] Nazwy warstw i frame'ów = nazwy kanoniczne (route'y, komponenty `Coach*`, `MicButton`…), aby handoff do Compose był 1:1.
29. [ ] Włącz Dev Mode; opisz komponentom odpowiedniki Compose (np. `MicButton` → `core/designsystem/component/MicButton.kt`).
30. [ ] Udostępnij plik zespołowi (edit dla designu, view dla devów) i podlinkuj w README repo.
