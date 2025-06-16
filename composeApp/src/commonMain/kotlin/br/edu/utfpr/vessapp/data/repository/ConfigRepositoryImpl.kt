package br.edu.utfpr.vessapp.data.repository

import br.edu.utfpr.vessapp.data.local.ConfigLocalDataSource
import br.edu.utfpr.vessapp.domain.entity.Config
import br.edu.utfpr.vessapp.domain.repository.ConfigRepository
import kotlinx.coroutines.flow.Flow

/**
 * Implementação concreta do ConfigRepository.
 * Esta classe é responsável por coordenar a obtenção e o salvamento dos dados de Config
 * usando a ConfigLocalDataSource.
 *
 * @param localDataSource A fonte de dados local para Config (expect/actual).
 */
class ConfigRepositoryImpl(
    private val localDataSource: ConfigLocalDataSource
) : ConfigRepository {

    /**
     * Obtém o Flow de Config da fonte de dados local.
     * @return Um Flow que emite o objeto Config.
     */
    override fun getConfig(): Flow<Config> {
        return localDataSource.getConfig()
    }

    /**
     * Salva o objeto Config usando a fonte de dados local.
     * @param config O objeto Config a ser salvo.
     */
    override suspend fun saveConfig(config: Config) {
        localDataSource.saveConfig(config)
    }
}
