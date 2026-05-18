package com.bhanu.aegis

import android.app.Application
import com.bhanu.aegis.core.llm.LiteRTEngineManager
import dagger.hilt.android.HiltAndroidApp

/**
 * Aegis-Edge Application — entry point for Hilt dependency injection.
 * No analytics, no crash reporting, no external services.
 * All data stays on-device.
 */
@HiltAndroidApp
class ChakuliApplication : Application() {

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        LiteRTEngineManager.onTrimMemory(level)
    }
}
