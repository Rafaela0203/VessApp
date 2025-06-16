// composeApp/src/androidMain/kotlin/br/edu/utfpr/vessapp/data/local/AndroidConfigLocalDataSource.kt
package br.edu.utfpr.vessapp.data.local

import android.content.Context
import android.content.SharedPreferences
import br.edu.utfpr.vessapp.domain.entity.Config
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.json.Json // Importar Json do kotlinx.serialization.json

/**
 * Implementação 'actual' da ConfigLocalDataSource para a plataforma Android.
 * Utiliza SharedPreferences para armazenar e recuperar o objeto Config.
 *
 * @param context O contexto da aplicação Android, necessário para acessar SharedPreferences.
 */
actual class ConfigLocalDataSource(private val context: Context) {

    private val preferences: SharedPreferences =
        context.getSharedPreferences("vess_app_config", Context.MODE_PRIVATE)

    // Flow interno para emitir atualizações de configuração.
    // Inicializado com a configuração salva ou uma configuração padrão.
    private val _configFlow = MutableStateFlow(loadConfigFromPrefs())
    actual fun getConfig(): Flow<Config> = _configFlow.asStateFlow()

    /**
     * Carrega o objeto Config das SharedPreferences.
     * Se não houver configuração salva, retorna uma Config padrão.
     * @return O objeto Config carregado.
     */
    private fun loadConfigFromPrefs(): Config {
        val jsonString = preferences.getString("user_config", null)
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
     * Salva o objeto Config nas SharedPreferences.
     * Após salvar, atualiza o MutableStateFlow para que os observadores sejam notificados.
     * @param config O objeto Config a ser salvo.
     */
    actual suspend fun saveConfig(config: Config) {
        val jsonString = Json.encodeToString(config)
        preferences.edit().putString("user_config", jsonString).apply()
        _configFlow.update { config } // Atualiza o Flow para refletir a nova configuração
    }
}
