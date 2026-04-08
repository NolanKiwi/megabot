package com.megabot.engine.api

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build

/**
 * Device information API exposed to JavaScript scripts.
 */
class DeviceApi(private val context: Context) {

    fun getBuild(): Map<String, String> = mapOf(
        "brand" to Build.BRAND,
        "model" to Build.MODEL,
        "device" to Build.DEVICE,
        "product" to Build.PRODUCT,
        "sdkVersion" to Build.VERSION.SDK_INT.toString(),
        "androidVersion" to Build.VERSION.RELEASE
    )

    fun getPhoneBrand(): String = Build.BRAND
    fun getPhoneModel(): String = Build.MODEL
    fun getAndroidVersionCode(): Int = Build.VERSION.SDK_INT
    fun getAndroidVersionName(): String = Build.VERSION.RELEASE

    fun getBatteryLevel(): Int {
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        return bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }

    fun isCharging(): Boolean {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        return status == BatteryManager.BATTERY_STATUS_CHARGING ||
               status == BatteryManager.BATTERY_STATUS_FULL
    }

    fun getBatteryTemperature(): Float {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val temp = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
        return temp / 10f
    }
}
