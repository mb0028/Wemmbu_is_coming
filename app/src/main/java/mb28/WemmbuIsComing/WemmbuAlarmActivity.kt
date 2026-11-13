package mb28.WemmbuIsComing

import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mb28.WemmbuIsComing.MainActivity.Companion.WEMMBU
import mb28.WemmbuIsComing.MainActivity.Companion.WEMMBU_DISMISS_AFTER_30S
import mb28.WemmbuIsComing.MainActivity.Companion.WEMMBU_IS_ON
import mb28.WemmbuIsComing.ui.theme.WemmbuIsComingTheme
import java.io.File
import kotlin.time.Duration.Companion.seconds

class WemmbuAlarmActivity : ComponentActivity() {
    companion object {
        const val EXTRA_FORCE_FIRE = "EXTRA_FORCE_FIRE"
    }
    val player = MediaPlayer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sp = getSharedPreferences(WEMMBU, 0)
        if (!intent.getBooleanExtra(EXTRA_FORCE_FIRE, false)) {
            if (!sp.getBoolean(WEMMBU_IS_ON, false)) {
                finish()
            }
        }

        setShowWhenLocked(true)
        setTurnScreenOn(true)

        if (sp.getBoolean(WEMMBU_DISMISS_AFTER_30S, false)) {
            lifecycleScope.launch {
                delay(30.seconds)
                finish()
            }
        }

        enableEdgeToEdge()
        window.isNavigationBarContrastEnforced = false
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.isAppearanceLightNavigationBars = false
        controller.hide(WindowInsetsCompat.Type.statusBars())

        val file = File(cacheDir.path + "alarm.mp3")
        if (!file.exists()) {
            val uri = "android.resource://$packageName/raw/nuke_alert".toUri()
            val audioFile = contentResolver.openAssetFileDescriptor(uri, "r")!!

            file.createNewFile()
            val stream = file.outputStream()
            val iStream = audioFile.createInputStream()
            with(stream) {
                write(iStream.readBytes())
                flush()
                close()
            }
            audioFile.close()
            iStream.close()
        }

        with(player) {
            setDataSource(file.path)
            prepare()
            isLooping = true
            start()
        }

        setContent {
            WemmbuIsComingTheme {
                Column(
                    Modifier.fillMaxSize().background(Color.Black),
                    Arrangement.Center,
                    Alignment.CenterHorizontally
                ) {

                    Text(
                        "!!! EMERGENCY ALERT !!!",
                        fontSize = 29.sp,
                        color = Color.White
                    )

                    Image(
                        painterResource(R.drawable.wemmbu3d),
                        null,
                        Modifier.size(200.dp).padding(vertical = 30.dp)
                    )

                    Text(
                        "Wemmbu is flying above your area.\nPlease stay calm and DO NOT GO OUTSIDE.\n",
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 15.dp)
                    )

                    Text(
                        "If you hear any fishing rod break noise,\nstand up and wait for your death.",
                        color = Color.Red,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 15.dp)
                    )

                    Spacer(Modifier.height(30.dp))

                    OutlinedButton(
                        {
                            finish()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        )
                    ) {
                        Text("Dismiss")
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        player.release()
        super.onDestroy()
    }
}