// composeApp/src/iosMain/kotlin/br/edu/utfpr/vessapp/data/local/ConfigLocalDataSource.kt
package br.edu.utfpr.vessapp.data.local

import br.edu.utfpr.vessapp.domain.entity.Config
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.json.Json
import platform.Foundation.NSUserDefaults // Importar NSUserDefaults

/**
 * Implementação 'actual' da ConfigLocalDataSource para a plataforma iOS.
 * Utiliza NSUserDefaults para armazenar e recuperar o objeto Config.
 */
actual class ConfigLocalDataSource {

    private val userDefaults: NSUserDefaults = NSUserDefaults.standardUserDefaults()

    // Flow interno para emitir atualizações de configuração.
    // Inicializado com a configuração salva ou uma configuração padrão.
    private val _configFlow = MutableStateFlow(loadConfigFromUserDefaults())
    actual fun getConfig(): Flow<Config> = _configFlow.asStateFlow()

    /**
     * Carrega o objeto Config da NSUserDefaults.
     * Se não houver configuração salva, retorna uma Config padrão.
     * @return O objeto Config carregado.
     */
    private fun loadConfigFromUserDefaults(): Config {
        val jsonString = userDefaults.stringForKey("user_config")
        return if (jsonString != null) {
            try {
                Json.decodeFromString<Config>(jsonString)
            } catch (e: Exception) {
                // Em caso de erro na desserialização, retorna uma configuração padrão
                // e loga o erro para depuração.
                println("Erro ao desserializar Config: ${e.message}")
                Config()
            }
        } else {
            Config() // Retorna uma Config padrão se não houver nada salvo
        }
    }

    /**
     * Salva o objeto Config na NSUserDefaults.
     * Após salvar, atualiza o MutableStateFlow para que os observadores sejam notificados.
     * @param config O objeto Config a ser salvo.
     */
    actual suspend fun saveConfig(config: Config) {
        val jsonString = Json.encodeToString(config)
        userDefaults.setObject(jsonString, "user_config")
        userDefaults.synchronize() // Garante que os dados sejam salvos imediatamente
        _configFlow.update { config } // Atualiza o Flow para refletir a nova configuração
    }
}
