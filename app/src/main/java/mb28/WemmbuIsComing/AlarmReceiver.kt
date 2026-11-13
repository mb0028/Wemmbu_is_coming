package mb28.WemmbuIsComing

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        fireNow(context)
        scheduleAlarms(context)
    }
}