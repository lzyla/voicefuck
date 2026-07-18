# 18. Redesign "Liquid Glass" (2026-07-17)

Ten dokument opisuje drugą, dużą iterację wizualną aplikacji, zrealizowaną na
podstawie zewnętrznego handoffu projektowego (`design_handoff_ai_english_coach`,
17 ekranów, 5 sekcji: Onboarding, Learning, Speaking & Chat, Practice &
Progress, Account). Handoff wprowadzał nowy język wizualny — półprzezroczyste,
"szklane" panele nad gradientowym tłem w stylu zorzy polarnej — oraz kilka
koncepcji produktowych nieobecnych w pierwszej wersji aplikacji (konta
użytkowników, adaptacyjny test poziomujący, program nauki w jednostkach/
lekcjach z fiszkami, gamifikacja XP/streak, oddzielny czat tekstowy, ekran
profilu z odznakami, ekran płatny "Go Pro").

## Co jest realne, a co jest makietą

Redesign celowo **nie** kopiuje handoffu 1:1 tam, gdzie oznaczałoby to
podszywanie się pod funkcjonalność, której backend nie ma. Zasada: jeśli dany
ekran w oryginalnej aplikacji miał już prawdziwe dane/logikę (Home, Rozmowa,
Podsumowanie rozmowy), redesign zachowuje tę logikę i zmienia tylko warstwę
wizualną. Nowe koncepcje z handoffu, które wymagałyby nieistniejącego backendu,
zostały zaimplementowane jako w pełni działający **frontend** z jawnie
udokumentowanymi uproszczeniami:

| Ekran / funkcja | Status |
|---|---|
| Onboarding (Welcome, Sign up, Test poziomujący, Wynik, Cele) | UI w pełni działające; "Sign up" to lokalne pole (brak backendu kont); test poziomujący to statyczna pula 6 pytań ocenianych na urządzeniu, nie adaptacyjny bank pytań |
| Home | Prawdziwe dane (`ObserveHomeSummaryUseCase`), zmieniona tylko warstwa wizualna |
| Ścieżka nauki (Learning Path) | Nowa funkcja: statyczny, wbudowany w aplikację program (3 jednostki, 5 lekcji z fiszkami i ćwiczeniami). Postęp (`LearningPathProgressStore`) trzymany w pamięci na czas życia procesu — brak trwałości w Room/DataStore, bo to przykładowa treść, nie prawdziwy content pipeline |
| Rozmowa głosowa + Podsumowanie | Prawdziwe dane i logika (STT/TTS, analiza błędów), zmieniona tylko warstwa wizualna |
| Czat tekstowy | **Prawdziwa funkcja** — korzysta z tego samego backendu co rozmowa głosowa (`StartConversationUseCase`/`SendMessageUseCase`/`ObserveConversationDetailUseCase`, scenariusz `FREE_TALK`), tylko bez STT/TTS |
| Profil | Prawdziwe dane tam, gdzie istnieją (poziom, streak z `ObservePreferencesUseCase`/`ObserveHomeSummaryUseCase`); odznaki to statyczna lista odblokowywana progiem streaka |
| Ekran "Go Pro" (Paywall) | Statyczny — brak integracji z Play Billing; każdy przycisk po prostu zamyka ekran |
| Ustawienia | **Celowo pozostawione bez zmian wizualnych** (nadal Material 3) — to gęsty ekran konfiguracyjny z wieloma interaktywnymi kontrolkami (Slider, Switch, dialogi); ryzyko przepisania całości na nowy system wizualny przewyższało wartość kosmetyczną na tym etapie |
| Ćwiczenia / Słownictwo / Statystyki (Practice hub) | **Celowo pozostawione bez zmian wizualnych** — z tego samego powodu co Ustawienia: to głębokie, w pełni funkcjonalne ekrany z realną logiką powtórek (SRS), których przepisanie na nowy system wizualny nie było priorytetem względem czasu poświęconego na nowe funkcje z handoffu |

## Fundament wizualny

Nowy system wizualny żyje w osobnym pakiecie, równolegle do istniejącego
motywu Material 3 (`core.designsystem.theme`), ponieważ są fundamentalnie różne
(ciemne/przezroczyste/gradientowe vs. jasny, tokenowy system M3):

- `core/designsystem/glass/GlassTokens.kt` — kolory, promienie, odstępy
  przepisane z README handoffu.
- `core/designsystem/glass/AuroraBackground.kt` — tło generowane proceduralnie
  (`Canvas` + `Brush.radialGradient`), **nie** z dostarczonego obrazu
  `aurora-background.avif` — ten plik miał widoczny, nielicencjonowany znak
  wodny Freepik, więc nie trafił do repozytorium. README handoffu wprost
  dopuszcza taki fallback ("generate an equivalent gradient").
- `core/designsystem/glass/GlassComponents.kt`, `GlassBottomNav.kt` — panel,
  przyciski, chipy, przełącznik, pasek postępu, dolna nawigacja.

**Uproszczenie względem oryginalnego designu:** referencyjne makiety używają
CSS `backdrop-filter: blur(...)`, czyli prawdziwego rozmycia tła pod panelem.
Stabilny Jetpack Compose (bez zewnętrznej biblioteki) tego nie ma. Sprawdzona
alternatywa — biblioteka [Haze](https://github.com/chrisbanes/haze) — wymaga
Kotlina 2.2.20, a projekt jest na 2.0.21; dodanie jej niosło ryzyko złamania
budowy dla korzyści czysto kosmetycznej. Zamiast tego panele używają
półprzezroczystego wypełnienia + obramowania + cienia, co na gładkim,
gradientowym tle wygląda niemal identycznie jak realne rozmycie.

## Nowe moduły

Trzy nowe moduły `:feature:*`, każdy podłączony do `settings.gradle.kts` i
`app/build.gradle.kts` zgodnie z konwencją "moduł feature nie zależy od innego
modułu feature":

- `feature/learningpath` — ścieżka nauki, ekran lekcji (fiszki → ćwiczenie →
  podsumowanie), `LearningPathProgressStore` (singleton Hilt, tylko w pamięci).
- `feature/chat` — czat tekstowy.
- `feature/profile` — profil i ekran "Go Pro".

## Nawigacja

`CoachNavHost` przeszedł z 4-zakładkowego `NavigationBar` Material 3 na
5-zakładkowy, pływający `GlassBottomNav` (Home / Ścieżka / Praktyka / Czat /
Profil), zgodnie z handoffem. Lista rozmów i Statystyki (wcześniej osobne
zakładki) są teraz dostępne pośrednio: z ikony historii na ekranie Home i z
wiersza "Zobacz szczegółowe statystyki" na ekranie Profilu — nie zniknęła
żadna funkcjonalność, zmienił się tylko punkt wejścia.
