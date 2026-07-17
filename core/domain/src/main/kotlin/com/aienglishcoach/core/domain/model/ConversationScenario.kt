package com.aienglishcoach.core.domain.model

/**
 * Predefined conversation scenarios. Each scenario changes the tutor's role
 * and vocabulary focus in the system prompt.
 */
enum class ConversationScenario(val topicHint: String) {
    FREE_TALK("Casual, open-ended conversation about anything the learner brings up"),
    SMALL_TALK("Everyday small talk: weather, weekend plans, hobbies"),
    JOB_INTERVIEW("A realistic job interview for the learner's profession"),
    BUSINESS_MEETING("A workplace meeting: status updates, opinions, negotiation"),
    TRAVEL("Travel situations: airport, hotel check-in, asking for directions"),
    RESTAURANT("Ordering food, making reservations, handling problems politely"),
    SHOPPING("Shopping, returns, complaints and negotiating prices"),
    HEALTH("Doctor appointments and describing symptoms"),
}
