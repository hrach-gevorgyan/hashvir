package com.hrach.hashvir

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.hrach.hashvir.ui.PaletteDebugScreen

/** Debug-only launcher entry for the step 2 palette and font check. */
class PaletteDebugActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { PaletteDebugScreen() }
    }
}
