package com.starrift.starlock.data

/**
 * Gruplu liste başlıkları için anahtar tipi.
 * Letter: A-Z / Z-A modunda harf başlığı.
 * DateGroup: LAST_UPDATED modunda gün bazlı başlık.
 *   - epochDay: o günün epoch gün sayısı (sıralama için).
 *   - itemCount: o gün içinde güncellenen öğe sayısı.
 *   - singleUpdatedAtMillis: itemCount == 1 ise o tek öğenin tam zaman damgası (tarih+saat gösterimi için), aksi halde null.
 */
sealed class SortGroupKey {
    data class Letter(val char: Char) : SortGroupKey()
    data class DateGroup(
        val epochDay: Long,
        val itemCount: Int,
        val singleUpdatedAtMillis: Long?
    ) : SortGroupKey()
}
