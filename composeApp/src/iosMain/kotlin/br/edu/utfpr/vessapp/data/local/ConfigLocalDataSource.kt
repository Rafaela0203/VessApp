package br.edu.utfpr.vessapp.data.local

import br.edu.utfpr.vessapp.domain.entity.Config
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import platform.Foundation.NSUserDefaults

/**
 * Implementação 'actual' da ConfigLocalDataSource para a plataforma iOS.
 * Utiliza NSUserDefaults para armazenar e recuperar o objeto Config.
 */
actual class ConfigLocalDataSource { // REMOVIDO: : br.edu.utfpr.vessapp.data.local.ConfigLocalDataSource()

    private val userDefaults: NSUserDefaults = NSUserDefaults.standardUserDefaults()

    private val _configFlow = MutableStateFlow(loadConfigFromUserDefaults())
    actual fun getConfig(): Flow<Config> = _configFlow.asStateFlow()

    private fun loadConfigFromUserDefaults(): Config {
        val jsonString = userDefaults.stringForKey("user_config")
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
        userDefaults.setObject(jsonString, "user_config")
        userDefaults.synchronize()
        _configFlow.update { config }
    }
}
