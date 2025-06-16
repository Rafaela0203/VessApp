package br.edu.utfpr.vessapp.domain.repository

import br.edu.utfpr.vessapp.domain.entity.Config
import kotlinx.coroutines.flow.Flow

/**
 * Interface de Repositório para a entidade Config.
 * Define o contrato para operações de dados relacionadas à configuração do usuário,
 * abstraindo a fonte de dados subjacente.
 */
interface ConfigRepository {

    /**
     * Obtém o estado atual da configuração do usuário como um Flow.
     * Um Flow é usado para que a UI possa reagir a mudanças em tempo real.
     * @return Um Flow que emite o objeto Config atual.
     */
    fun getConfig(): Flow<Config>

    /**
     * Salva uma nova configuração de usuário.
     * @param config O objeto Config a ser salvo.
     */
    suspend fun saveConfig(config: Config)
}
