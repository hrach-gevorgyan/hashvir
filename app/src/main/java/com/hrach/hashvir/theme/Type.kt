package com.hrach.hashvir.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.hrach.hashvir.R

/**
 * Bundled so Armenian never falls back to the system font, which renders tofu on
 * devices without an Armenian-locale font. Both weights carry Latin digits, so the
 * numeral and the word in [NumberGlyph] come from the same family.
 */
val Armenian = FontFamily(
    Font(R.font.noto_sans_armenian_bold, FontWeight.Bold),
    Font(R.font.noto_sans_armenian_black, FontWeight.Black),
)
