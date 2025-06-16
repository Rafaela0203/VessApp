package br.edu.utfpr.vessapp.data.local

import br.edu.utfpr.vessapp.domain.entity.Config
import kotlinx.coroutines.flow.Flow

/**
 * Declaração 'expect' para a Fonte de Dados Local de Configuração.
 * Define os métodos para interagir com o armazenamento de dados local (e.g., SharedPreferences, UserDefaults).
 * As implementações 'actual' serão fornecidas por cada plataforma.
 */
expect interface ConfigLocalDataSource {

    /**
     * Obtém a configuração salva localmente como um Flow, permitindo observação de mudanças.
     * @return Um Flow que emite o objeto Config salvo.
     */
    fun getConfig(): Flow<Config>

    /**
     * Salva o objeto Config no armazenamento local.
     * @param config O objeto Config a ser salvo.
     */
    suspend fun saveConfig(config: Config)
}
