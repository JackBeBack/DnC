package UI

import android.media.MediaPlayer
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import de.jackBeBack.dnc.R
import de.jackBeBack.dnc.data.RollEntity
import kotlinx.coroutines.delay


@Composable
fun DiceBox(modifier: Modifier, rollEntity: RollEntity, onFinish: () -> Unit) {
    var selectedDice by remember { mutableStateOf(DiceType.D20) }
    val context = LocalContext.current
    var showDice by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf(false) }
    var currentDisplayValue by remember { mutableStateOf(0) }

    // Animation state
    var isRolling by remember { mutableStateOf(false) }
    var playHitSound by remember { mutableStateOf(false) }

    // Coroutine scope for animations
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        delay(50)
        showDice = true

        // Start rolling animation
        isRolling = true

        // Animate random numbers
        repeat(5) { iteration ->
            val animationDelay = 50L + (iteration * 25L) // Gradually slow down
            currentDisplayValue = (1..20).random()
            delay(animationDelay)
        }

        // Show final value
        currentDisplayValue = rollEntity.value
        isRolling = false

        // Handle success check if target exists
        if (rollEntity.target != null) {
            delay(500)
        }else{
            isSuccess = rollEntity.isSuccess()
            if (isSuccess){
                playHitSound = true
            }
        }
        delay(1000)
        onFinish()

    }


    if (playHitSound){
        SoundPlayer(R.raw.hit){ onFinish() }
    }

    Box(modifier) {
        SoundPlayer(R.raw.roll, 0.5f)
        Column(Modifier.align(Alignment.Center)) {
            if (showDice) {
                Box {
                    Image(
                        painterResource(R.drawable.dice),
                        contentDescription = "Dice",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .scale(3f)
                    )
                    Text(
                        "$currentDisplayValue",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .scale(3f),
                        color = Color.White,
                        style = TextStyle(
                            fontWeight = if (isRolling) FontWeight.Normal else FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    )
                }
            } else {
                if (rollEntity.target != null) {
                    Text(
                        "[${rollEntity.target}]",
                        modifier = Modifier.scale(3f),
                        color = Color.Yellow
                    )
                }
            }
        }

        if (isSuccess) {
            //SoundPlayer(R.raw.success, 0.5f)
        }
    }
}

fun DrawScope.drawTriangle(color: Color, size: Float, center: Offset) {
    val topPoint = Offset(center.x, center.y - size)
    val bottomLeftPoint = Offset(center.x - size, center.y + size)
    val bottomRightPoint = Offset(center.x + size, center.y + size)

    drawPath(
        path = androidx.compose.ui.graphics.Path().apply {
            moveTo(topPoint.x, topPoint.y)
            lineTo(bottomLeftPoint.x, bottomLeftPoint.y)
            lineTo(bottomRightPoint.x, bottomRightPoint.y)
            close()
        },
        color = color
    )
}

fun getRandomDiceValue(type: DiceType, luckMode: Boolean = false): Int {
    if (luckMode) {
        return 20
    }
    return when (type) {
        DiceType.D4 -> (1..4).random()
        DiceType.D6 -> (1..6).random()
        DiceType.D8 -> (1..8).random()
        DiceType.D10 -> (1..10).random()
        DiceType.D12 -> (1..12).random()
        DiceType.D20 -> (1..20).random()
        DiceType.D100 -> (1..100).random()
    }
}

enum class DiceType {
    D6, D8, D10, D12, D20, D4, D100
}

@Composable
fun SoundPlayer(res: Int, volume: Float = 1f, onFinish: () -> Unit = {}) {
    val context = LocalContext.current
    var mediaPlayer: MediaPlayer? by remember { mutableStateOf(null) }
    var isPlaying by remember { mutableStateOf(false) }

    DisposableEffect(context) {
        onDispose {
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    fun playSound() {
        try {
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer.create(context, res) // Replace with your sound file
                mediaPlayer?.setVolume(volume, volume)
                mediaPlayer?.setOnCompletionListener {
                    isPlaying = false
                    onFinish()
                }
            }
            if (!isPlaying) {
                mediaPlayer?.start()
                isPlaying = true
            } else {
                mediaPlayer?.pause()
                isPlaying = false
            }

        } catch (e: Exception) {
            // Handle exceptions, e.g., file not found
            println("Error playing sound: ${e.message}")
        }
    }

    LaunchedEffect(Unit) {
        playSound()
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    SoundPlayer(R.raw.roll)
}

