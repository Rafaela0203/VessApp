package br.edu.utfpr.vessapp.domain.useCase

import br.edu.utfpr.vessapp.domain.entity.Config
import br.edu.utfpr.vessapp.domain.repository.ConfigRepository
import kotlinx.coroutines.flow.Flow

class GetConfigUseCase(
    private val configRepository: ConfigRepository
) {
    operator fun invoke(): Flow<Config> {
        return configRepository.getConfig()
    }
}
