package com.example.powerfailure

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var batteryPercentageTextView: TextView
    private lateinit var chargingTimeTextView: TextView
    private lateinit var dischargingTimeTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        batteryPercentageTextView = findViewById(R.id.batteryPercentage)
        chargingTimeTextView = findViewById(R.id.chargingTime)
        dischargingTimeTextView = findViewById(R.id.dischargingTime)

        val batteryStatusIntentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        registerReceiver(batteryStatusReceiver, batteryStatusIntentFilter)
    }

    private val batteryStatusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)

            val batteryPercentage = (level / scale.toFloat() * 100).toInt()
            batteryPercentageTextView.text = "Battery Percentage: $batteryPercentage%"

            val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            if (status == BatteryManager.BATTERY_STATUS_CHARGING) {
                val chargingTime = calculateChargingTime(level, scale, intent)
                chargingTimeTextView.text = "Charging Time: $chargingTime minutes"
                dischargingTimeTextView.text = "Discharging Time: N/A"
            } else {
                chargingTimeTextView.text = "Charging Time: N/A"
                val dischargingTime = calculateDischargingTime(level, scale, intent)
                dischargingTimeTextView.text = "Discharging Time: $dischargingTime minutes"
            }
        }
    }

    private fun calculateChargingTime(level: Int, scale: Int, intent: Intent): Int {
        val batteryStatus = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val batteryPlugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)

        if (batteryStatus == BatteryManager.BATTERY_STATUS_CHARGING && batteryPlugged != 0) {
            val currentBatteryPercentage = level / scale.toFloat()
            val timeToFullCharge = ((1.0 - currentBatteryPercentage) * 60.0).toInt()
            return timeToFullCharge
        }

        return 0
    }

    private fun calculateDischargingTime(level: Int, scale: Int, intent: Intent): Int {
        val batteryStatus = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)

        if (batteryStatus == BatteryManager.BATTERY_STATUS_DISCHARGING) {
            val currentBatteryPercentage = level / scale.toFloat()
            val timeToEmpty = (currentBatteryPercentage * 60.0).toInt()
            return timeToEmpty
        }

        return 0
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(batteryStatusReceiver)
    }
}
