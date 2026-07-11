package com.example.data.ai

/**
 * Curated motivational quotes for study inspiration.
 * Mix of English and Hinglish quotes. No API call needed — these are local.
 *
 * Used by:
 * - HomeScreen daily quote card
 * - Notification worker for daily motivation
 */
object MotivationalQuotes {

    val quotes = listOf(
        "Success is the sum of small efforts repeated day in and day out.",
        "The expert in anything was once a beginner.",
        "Don't watch the clock; do what it does. Keep going.",
        "The future depends on what you do today.",
        "Hard work beats talent when talent doesn't work hard.",
        "Focus on being productive, not busy.",
        "Dreams don't work unless you do.",
        "The pain of studying is temporary, the pain of regret is permanent.",
        "Aaj mehnat karo, kal sapne pure karo.",
        "Padhai me koi shortcut nahi hota — bas rasta straight hai.",
        "Har question solve karne par tu ek step aage badhta hai.",
        "Tumhari consistency hi tumhara sabse bada competitor hai.",
        "Aaj ka effort kal ka result hai.",
        "Stop wishing, start doing.",
        "One day or day one — you decide.",
        "Tum itne weak nahi ho jitna tum sochte ho.",
        "Every expert was once a beginner who refused to give up.",
        "Study like there's no tomorrow — because one day there won't be.",
        "Tumhari taiyari tumhara confidence banati hai.",
        "The only bad study session is the one you didn't do.",
        "Discipline is choosing between what you want now and what you want most.",
        "Padhai se bada koi dhoka nahi — wo hamesha phal deti hai.",
        "Success isn't given, it's earned — on the study table, not in dreams.",
        "Tumhare competitors abhi padh rahe hain. Tum kya kar rahe ho?",
        "Focus on progress, not perfection.",
        "The clock is ticking. Make every minute count.",
        "Apne goals itne bade rakho ki dar ke liye time hi na mile.",
        "Mehnat se pehle kamyabi nahi aati — kamyabi mehnat ka natija hai.",
        "Tu single thread se jeet sakta hai, bas focus rakh."
    )

    /** Returns a random quote. */
    fun random(): String = quotes.random()

    /** Returns a quote based on the day of year (same quote all day). */
    fun daily(): String {
        val dayOfYear = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_YEAR)
        return quotes[dayOfYear % quotes.size]
    }

    /**
     * Returns a guilt-trip message for night time (9 PM - midnight) when the
     * user hasn't studied enough today. Different messages for different levels
     * of inactivity.
     */
    fun nightGuiltTrip(todayStudyMinutes: Int): String {
        return when {
            todayStudyMinutes == 0 -> listOf(
                "Aaj pura din gaya — ek minute bhi padhai nahi ki? Kal exam ke time yaad aayega. Abhi bhi 30 min de de.",
                "Din khatam hone wala hai aur tumne abhi tak kuch nahi kiya. Yeh guilt kal aur bhi heavy hoga. Abhi shuru kar.",
                "Phone chhod, kitab utha. Aaj ka din khaali gaya toh kal ka pressure double hoga."
            ).random()
            todayStudyMinutes < 30 -> listOf(
                "Sirf $todayStudyMinutes min? Yeh kafi nahi hai. Kal ko 'kaash aaj thoda aur padh leta' bolna padega. Abhi 30 min aur de.",
                "$todayStudyMinutes minutes me kya hoga? Thoda aur mehnat kar — kal ka self khush dekhega."
            ).random()
            todayStudyMinutes < 60 -> listOf(
                "$todayStudyMinutes min padha — theek hai par aur ho sakta tha. Kal thoda aur push kar."
            ).random()
            else -> listOf(
                "Aaj $todayStudyMinutes min padha — good effort! Kal bhi yahi consistency rakh."
            ).random()
        }
    }
}
