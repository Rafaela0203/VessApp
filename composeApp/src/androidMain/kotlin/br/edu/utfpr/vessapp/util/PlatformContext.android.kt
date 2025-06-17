package br.edu.utfpr.vessapp.util

import android.content.Context

actual class PlatformContext(val androidContext: Context) {
}

private lateinit var applicationContext: Context

fun setAndroidApplicationContext(context: Context) {
    applicationContext = context.applicationContext
}

actual fun getPlatformContext(): PlatformContext {
    if (!::applicationContext.isInitialized) {
        error("Android Application Context não foi inicializado. Chame setAndroidApplicationContext(context) na sua MainActivity ou Application.")
    }
    return PlatformContext(applicationContext)
}
