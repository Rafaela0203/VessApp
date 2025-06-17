package br.edu.utfpr.vessapp.util

actual class PlatformContext {
}

actual fun getPlatformContext(): PlatformContext {
    return PlatformContext()
}
