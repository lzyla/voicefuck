package com.aienglishcoach.feature.learningpath

/**
 * Static, on-device curriculum sample data. There is no lesson-authoring
 * backend yet — this is a fixed set of units/lessons shipped with the app,
 * matching the design handoff's "Learning Path" concept closely enough to
 * demo the flow (flashcards -> exercise -> completion) without inventing a
 * content pipeline.
 */
data class Flashcard(
    val front: String,
    val back: String,
)

data class ExerciseQuestion(
    val prompt: String,
    val options: List<String>,
    val correctOptionIndex: Int,
)

data class Lesson(
    val id: String,
    val title: String,
    val xpReward: Int,
    val flashcards: List<Flashcard>,
    val exercise: List<ExerciseQuestion>,
)

data class LearningUnit(
    val id: String,
    val title: String,
    val subtitle: String,
    val lessons: List<Lesson>,
)

val LEARNING_PATH: List<LearningUnit> = listOf(
    LearningUnit(
        id = "unit-everyday",
        title = "Everyday Conversations",
        subtitle = "Greetings, small talk, daily routines",
        lessons = listOf(
            Lesson(
                id = "everyday-1",
                title = "Saying hello",
                xpReward = 10,
                flashcards = listOf(
                    Flashcard("Hello, how are you?", "Cześć, jak się masz?"),
                    Flashcard("Nice to meet you.", "Miło Cię poznać."),
                    Flashcard("What's up?", "Co słychać?"),
                ),
                exercise = listOf(
                    ExerciseQuestion(
                        prompt = "\"Miło Cię poznać\" means:",
                        options = listOf("Nice to meet you.", "See you later.", "Good night."),
                        correctOptionIndex = 0,
                    ),
                    ExerciseQuestion(
                        prompt = "Choose the casual greeting:",
                        options = listOf("Good evening, sir.", "What's up?", "How do you do?"),
                        correctOptionIndex = 1,
                    ),
                ),
            ),
            Lesson(
                id = "everyday-2",
                title = "Daily routines",
                xpReward = 10,
                flashcards = listOf(
                    Flashcard("I wake up at seven.", "Budzę się o siódmej."),
                    Flashcard("I commute to work.", "Dojeżdżam do pracy."),
                ),
                exercise = listOf(
                    ExerciseQuestion(
                        prompt = "\"Dojeżdżam do pracy\" means:",
                        options = listOf("I commute to work.", "I go to sleep.", "I cook dinner."),
                        correctOptionIndex = 0,
                    ),
                ),
            ),
        ),
    ),
    LearningUnit(
        id = "unit-travel",
        title = "Travel English",
        subtitle = "Airports, hotels, directions",
        lessons = listOf(
            Lesson(
                id = "travel-1",
                title = "At the airport",
                xpReward = 15,
                flashcards = listOf(
                    Flashcard("Where is the gate?", "Gdzie jest wyjście do samolotu?"),
                    Flashcard("I'd like to check in.", "Chciałbym się odprawić."),
                ),
                exercise = listOf(
                    ExerciseQuestion(
                        prompt = "\"Gdzie jest wyjście do samolotu?\" means:",
                        options = listOf("Where is the gate?", "Where is the bathroom?", "Where is my luggage?"),
                        correctOptionIndex = 0,
                    ),
                ),
            ),
            Lesson(
                id = "travel-2",
                title = "Asking for directions",
                xpReward = 15,
                flashcards = listOf(
                    Flashcard("Excuse me, how do I get to the station?", "Przepraszam, jak dostać się na dworzec?"),
                    Flashcard("Turn left at the corner.", "Skręć w lewo na rogu."),
                ),
                exercise = listOf(
                    ExerciseQuestion(
                        prompt = "\"Skręć w lewo na rogu\" means:",
                        options = listOf("Turn left at the corner.", "Go straight ahead.", "Take the bus."),
                        correctOptionIndex = 0,
                    ),
                ),
            ),
        ),
    ),
    LearningUnit(
        id = "unit-work",
        title = "Business & Work",
        subtitle = "Meetings, emails, interviews",
        lessons = listOf(
            Lesson(
                id = "work-1",
                title = "In a meeting",
                xpReward = 20,
                flashcards = listOf(
                    Flashcard("Let's get started.", "Zaczynajmy."),
                    Flashcard("Could you clarify that?", "Czy mógłbyś to wyjaśnić?"),
                ),
                exercise = listOf(
                    ExerciseQuestion(
                        prompt = "\"Czy mógłbyś to wyjaśnić?\" means:",
                        options = listOf("Could you clarify that?", "Can I leave early?", "Are you free tomorrow?"),
                        correctOptionIndex = 0,
                    ),
                ),
            ),
        ),
    ),
)
