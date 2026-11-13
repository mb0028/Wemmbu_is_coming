package mb28.WemmbuIsComing

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSliderState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import mb28.WemmbuIsComing.ui.theme.WemmbuIsComingTheme
import androidx.core.content.edit
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import kotlin.math.roundToInt

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
class MainActivity : ComponentActivity() {
    companion object {
        const val WEMMBU = "Wemmbu"
        const val WEMMBU_IS_ON = "WemmbuIsOn"
        const val WEMMBU_INTERVAL = "WEMMBU_INTERVAL"
        const val WEMMBU_DISMISS_AFTER_30S = "WEMMBU_DISMISS_AFTER_30S"
    }
    var isOn by mutableStateOf(false)
    var dismissAfter30s by mutableStateOf(true)
    var alertInterval by mutableIntStateOf(13)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.isNavigationBarContrastEnforced = false

        val sp = getSharedPreferences(WEMMBU, 0)

        isOn = sp.getBoolean(WEMMBU_IS_ON, false)
        dismissAfter30s = sp.getBoolean(WEMMBU_DISMISS_AFTER_30S, true)
        alertInterval = sp.getInt(WEMMBU_INTERVAL, 13)

        fun toggle(value: Boolean) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 0)
                }
                return
            }
            val nm = NotificationManagerCompat.from(this)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                if (!nm.canUseFullScreenIntent()) {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT,
                        "package:$packageName".toUri())
                    startActivity(intent)
                    return
                }
            }

            isOn = value
            sp.edit(true) {
                putBoolean(WEMMBU_IS_ON, value)
            }
            if (isOn) {
                scheduleAlarms(this)
            } else {
                cancelAllAlarms(this)
            }
        }

        setContent {
            WemmbuIsComingTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ) { _ ->
                    Column(
                        Modifier.fillMaxSize().padding(horizontal = 10.dp),
                        Arrangement.Center,
                        Alignment.CenterHorizontally
                    ) {
                        SegmentedListItem(
                            trailingContent = {
                                Switch(
                                    isOn,
                                    {
                                        toggle(it)
                                    }
                                )
                            },
                            supportingContent = {
                                Text("Shows 'Wemmbu is flying above your area' alert")
                            },
                            verticalAlignment = Alignment.CenterVertically,
                            shapes = ListItemDefaults.segmentedShapes(0, 2)
                        ) {
                            Text("Turn on Wemmbu alerts")
                        }

                        Spacer(Modifier.height(3.dp))

                        SegmentedListItem(
                            trailingContent = {
                                Switch(
                                    dismissAfter30s,
                                    {
                                        dismissAfter30s = it
                                        sp.edit(true) {
                                            putBoolean(WEMMBU_DISMISS_AFTER_30S, it)
                                        }
                                    }
                                )
                            },
                            supportingContent = {
                                Text("Automatically dismisses alarm after 30s")
                            },
                            verticalAlignment = Alignment.CenterVertically,
                            shapes = ListItemDefaults.segmentedShapes(1, 3)
                        ) {
                            Text("Auto dismiss")
                        }

                        Spacer(Modifier.height(3.dp))

                        val state = rememberSliderState(
                            alertInterval.toFloat(),
                            16,
                            trackRange = 1f..18f
                        )
                        SegmentedListItem(
                            supportingContent = {
                                Slider(
                                    state,
                                    {
                                        alertInterval = it.roundToInt()
                                        state.value = it
                                    },
                                    onValueChangeFinished = {
                                        sp.edit(true) {
                                            putInt(WEMMBU_INTERVAL, alertInterval)
                                        }
                                    },
                                    enabled = isOn
                                )
                            },
                            shapes = ListItemDefaults.segmentedShapes(1, 3)
                        ) {
                            Text("Alerts Interval: ${alertInterval}h")
                        }

                        Spacer(Modifier.height(3.dp))

                        SegmentedListItem(
                            onClick = {
                                val intent = Intent(this@MainActivity, WemmbuAlarmActivity::class.java)
                                    .putExtra(WemmbuAlarmActivity.EXTRA_FORCE_FIRE, true)
                                startActivity(intent)
                            },
                            shapes = ListItemDefaults.segmentedShapes(2, 4)
                        ) {
                            Text("Test alarm")
                        }

                        Spacer(Modifier.height(3.dp))

                        SegmentedListItem(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW,
                                    "https://github.com/mb0028/Wemmbu_is_coming".toUri())
                                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                startActivity(intent)
                            },
                            shapes = ListItemDefaults.segmentedShapes(3, 4)
                        ) {
                            Text("Source code (Github)")
                        }
                    }
                }
            }
        }
    }
}
