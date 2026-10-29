package app.rork.quran.domain.model

fun String.searchKey(): String = java.text.Normalizer.normalize(this, java.text.Normalizer.Form.NFD)
    .replace(Regex("\\p{M}+"), "")
    .replace("ـ", "").replace('ى', 'ي').replace('ٱ', 'ا')
    .lowercase().trim()
