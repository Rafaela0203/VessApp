package br.edu.utfpr.vessapp.data.local

import br.edu.utfpr.vessapp.domain.entity.Config
import kotlinx.coroutines.flow.Flow

expect class ConfigLocalDataSource {

    fun getConfig(): Flow<Config>

    suspend fun saveConfig(config: Config)
}
