// composeApp/src/androidMain/kotlin/br/edu/utfpr/vessapp/data/local/ConfigLocalDataSource.kt
package br.edu.utfpr.vessapp.data.local

import android.content.Context
import android.content.SharedPreferences
import br.edu.utfpr.vessapp.domain.entity.Config
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

actual class ConfigLocalDataSource(private val context: Context) { // REMOVIDO: : br.edu.utfpr.vessapp.data.local.ConfigLocalDataSource()

    private val preferences: SharedPreferences =
        context.getSharedPreferences("vess_app_config", Context.MODE_PRIVATE)

    private val _configFlow = MutableStateFlow(loadConfigFromPrefs())
    actual fun getConfig(): Flow<Config> = _configFlow.asStateFlow()

    private fun loadConfigFromPrefs(): Config {
        val jsonString = preferences.getString("user_config", null)
        return if (jsonString != null) {
            try {
                Json.decodeFromString<Config>(jsonString)
            } catch (e: Exception) {
                println("Erro ao desserializar Config: ${e.message}")
                Config()
            }
        } else {
            Config()
        }
    }

    actual suspend fun saveConfig(config: Config) {
        val jsonString = Json.encodeToString(config)
        preferences.edit().putString("user_config", jsonString).apply()
        _configFlow.update { config }
    }
}
