package de.jackBeBack.dnc.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun Chip(text: String, modifier: Modifier = Modifier){
    Card(modifier.background(Color.DarkGray.copy(alpha = 0.5f))) {
        Text(text, color = Color.White, fontSize = 24.sp, modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 2.dp, bottom = 2.dp))
    }
}