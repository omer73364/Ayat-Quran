package app.rork.quran.domain.model

data class Ayah(
    val id: String,
    val surahNumber: Int,
    val surahNameArabic: String,
    val ayahNumber: Int,
    val textArabic: String,
)
