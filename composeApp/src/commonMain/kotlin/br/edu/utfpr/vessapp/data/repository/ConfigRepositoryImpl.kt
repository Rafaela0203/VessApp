package br.edu.utfpr.vessapp.data.repository

import br.edu.utfpr.vessapp.data.local.ConfigLocalDataSource
import br.edu.utfpr.vessapp.domain.entity.Config
import br.edu.utfpr.vessapp.domain.repository.ConfigRepository
import kotlinx.coroutines.flow.Flow

class ConfigRepositoryImpl(
    private val localDataSource: ConfigLocalDataSource
) : ConfigRepository {

    override fun getConfig(): Flow<Config> {
        return localDataSource.getConfig()
    }

    override suspend fun saveConfig(config: Config) {
        localDataSource.saveConfig(config)
    }
}
