package de.jackBeBack.dnc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import de.jackBeBack.dnc.Map.GameLayout
import de.jackBeBack.dnc.ui.theme.DnCTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DnCTheme {
                GameLayout()
            }
        }
    }
}