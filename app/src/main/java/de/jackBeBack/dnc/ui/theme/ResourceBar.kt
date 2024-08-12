package de.jackBeBack.dnc.ui.theme

import Resource
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ResourceBar(resource: Resource, color: Color) {
    val currentFraction = resource.current.toFloat() / (resource.max + resource.buff)
    val buffFraction = (resource.current + resource.buff).toFloat() / (resource.max + resource.buff)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(30.dp)
            .background(Color.Gray, shape = RoundedCornerShape(4.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(buffFraction)
                .background(Color.Yellow.copy(alpha = 0.5f), shape = RoundedCornerShape(4.dp))
        )

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(currentFraction)
                .background(color, shape = RoundedCornerShape(4.dp))
        )



        Text(
            text = resource.toString(),
            color = Color.Black,
            fontSize = 14.sp,
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}