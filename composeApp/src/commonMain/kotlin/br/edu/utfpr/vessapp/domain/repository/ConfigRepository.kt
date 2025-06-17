package br.edu.utfpr.vessapp.domain.repository

import br.edu.utfpr.vessapp.domain.entity.Config
import kotlinx.coroutines.flow.Flow

interface ConfigRepository {

    fun getConfig(): Flow<Config>

    suspend fun saveConfig(config: Config)
}
