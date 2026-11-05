package app.rork.quran.domain.model

data class Ayah(
    val id: String,
    val surahNumber: Int,
    val surahNameArabic: String,
    val ayahNumber: Int,
    val textArabic: String,
    val verses: List<PassageVerse>? = null,
) {
    val verseIds: List<String> get() = verses?.map { it.id } ?: listOf(id)
    val reference: String get() = if (verses.orEmpty().size > 1)
        "الآيات $ayahNumber–${verses!!.last().number}" else "الآية $ayahNumber"
}

data class PassageVerse(val id: String, val number: Int, val text: String)
