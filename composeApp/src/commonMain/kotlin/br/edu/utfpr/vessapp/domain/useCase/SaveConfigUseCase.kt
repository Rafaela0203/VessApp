package br.edu.utfpr.vessapp.domain.useCase

import br.edu.utfpr.vessapp.domain.entity.Config
import br.edu.utfpr.vessapp.domain.repository.ConfigRepository

class SaveConfigUseCase(
    private val configRepository: ConfigRepository
) {
    suspend operator fun invoke(config: Config) {
        configRepository.saveConfig(config)
    }
}
