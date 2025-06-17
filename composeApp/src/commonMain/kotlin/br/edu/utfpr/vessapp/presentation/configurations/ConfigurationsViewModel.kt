package br.edu.utfpr.vessapp.presentation.configurations

import br.edu.utfpr.vessapp.domain.entity.Config
import br.edu.utfpr.vessapp.domain.useCase.GetConfigUseCase
import br.edu.utfpr.vessapp.domain.useCase.SaveConfigUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ConfigurationsViewModel(
    private val getConfigUseCase: GetConfigUseCase,
    private val saveConfigUseCase: SaveConfigUseCase
) {
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _configState = MutableStateFlow(Config())
    val configState: StateFlow<Config> = _configState.asStateFlow()

    init {
        viewModelScope.launch {
            getConfigUseCase().collect { config ->
                _configState.value = config
            }
        }
    }

    fun saveConfig(newConfig: Config) {
        viewModelScope.launch {
            saveConfigUseCase(newConfig)
        }
    }
}
