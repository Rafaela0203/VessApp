// shared/src/commonMain/kotlin/App.kt
package com.vessapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.* // Importa tudo do Material Design 3
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Clock

// --- Data Models (Shared) ---
// Representa a configuração do usuário
data class Config(
    val name: String = "",
    val email: String = "",
    val country: String = "",
    val address: String = "",
    val cityState: String = "",
    val language: String = "Português (Brasil)"
)

// Representa os dados de uma camada de solo
data class LayerEvaluation(
    val length: String = "", // Comprimento da camada
    val score: String = ""   // Nota da camada
)

// Representa os dados de uma amostra
data class Sample(
    val id: String,
    val name: String,
    val numLayers: Int = 1,
    val location: String = "",
    val evaluator: String = "",
    val layers: List<LayerEvaluation> = emptyList(),
    val otherInfo: String = "",
    val timestamp: Long = Clock.System.now().toEpochMilliseconds() // Adicionado timestamp
)

// Representa uma avaliação completa, que pode conter múltiplas amostras
data class EvaluationSession(
    val id: String,
    val description: String, // ex: Avaliação 01/01/2025 - 19h00
    val samples: List<Sample> = emptyList(),
    val startTime: Long = Clock.System.now().toEpochMilliseconds(),
    var endTime: Long? = null // Nullable para avaliação em andamento
)


// --- Shared State Management (Simplificado) ---
// Define as telas que o aplicativo pode ter. Apenas o MENU é usado inicialmente.
enum class Screen {
    MENU,
    CONFIGURATIONS,
    NEW_EVALUATION_DESCRIPTION,
    EVALUATION_SAMPLE,
    EVALUATION_RESULT,
    FINAL_SUMMARY,
    HISTORY
}

// ViewModel simplificado para gerenciar o estado da aplicação
class AppViewModel {
    // Estado da tela atual, começa no MENU
    private val _currentScreen = MutableStateFlow(Screen.MENU)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Estado da configuração (simulado, apenas para o avaliador no menu)
    private val _currentConfig = MutableStateFlow(Config(name = "Avaliador Padrão"))
    val currentConfig: StateFlow<Config> = _currentConfig.asStateFlow()

    // Navega para uma nova tela
    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    // Funções placeholder para a tela de Configurações, para evitar erros de referência no menu
    fun updateConfig(newConfig: Config) {
        _currentConfig.value = newConfig
    }

    // Funções placeholder para as telas de Avaliação
    fun startNewEvaluationSession(description: String) {}
    fun addSampleToCurrentEvaluation(sample: Sample) {}
    fun completeCurrentEvaluationSession() {}
    fun clearCurrentSample() {}
    fun setCurrentSample(sample: Sample) {}
}

// CompositionLocal para fornecer o ViewModel para os composables filhos
val LocalAppViewModel = staticCompositionLocalOf<AppViewModel> {
    error("No AppViewModel provided")
}

// --- Composable Functions (UI Principal) ---

@Composable
fun App() {
    // Cria e lembra uma instância do ViewModel para o ciclo de vida do App
    val viewModel = remember { AppViewModel() }
    // Observa o estado da tela atual do ViewModel
    val currentScreen by viewModel.currentScreen.collectAsState()

    // Aplica o tema Material Design 3
    MaterialTheme(
        colorScheme = LightColorScheme, // Esquema de cores personalizado
        typography = Typography // Tipografia personalizada
    ) {
        // Superfície de fundo que preenche a tela inteira
        Surface(modifier = Modifier.fillMaxSize()) {
            // Fornece o ViewModel para todos os composables abaixo nesta hierarquia
            CompositionLocalProvider(LocalAppViewModel provides viewModel) {
                // Exibe a tela correspondente ao estado atual
                when (currentScreen) {
                    Screen.MENU -> MainMenuScreen()
                    // Outras telas não serão alcançadas com esta configuração inicial,
                    // mas mantidas no enum para futura expansão
                    else -> Text(text = "Tela em construção: ${currentScreen.name}") // Placeholder para telas não implementadas
                }
            }
        }
    }
}

@Composable
fun MainMenuScreen() {
    // Obtém a instância do ViewModel fornecida pelo CompositionLocalProvider
    val viewModel = LocalAppViewModel.current
    Column(
        modifier = Modifier
            .fillMaxSize() // Preenche toda a área disponível
            .background(LightColorScheme.background) // Cor de fundo do tema
            .padding(16.dp), // Preenchimento interno
        horizontalAlignment = Alignment.CenterHorizontally, // Centraliza o conteúdo horizontalmente
        verticalArrangement = Arrangement.Center // Centraliza o conteúdo verticalmente
    ) {
        // Título principal do aplicativo
        Text(
            text = "VESS",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = LightColorScheme.primary, // Cor de texto marrom escura do tema
            modifier = Modifier.padding(bottom = 32.dp) // Espaçamento abaixo do título
        )

        // Botões do menu, cada um com sua ação de navegação
        MenuButton(text = "AVALIAR") {
            // No momento, apenas navega para NEW_EVALUATION_DESCRIPTION.
            // Esta tela será implementada no próximo passo.
            viewModel.navigateTo(Screen.NEW_EVALUATION_DESCRIPTION)
        }
        Spacer(Modifier.height(16.dp)) // Espaçador entre o primeiro e os demais botões
        MenuButton(text = "Processo de avaliação") { /* TODO: Navegar para tutorial */ }
        Spacer(Modifier.height(8.dp))
        MenuButton(text = "Equipamentos") { /* TODO: Navegar para tutorial */ }
        Spacer(Modifier.height(8.dp))
        MenuButton(text = "Onde amostrar") { /* TODO: Navegar para tutorial */ }
        Spacer(Modifier.height(8.dp))
        MenuButton(text = "Quando amostrar") { /* TODO: Navegar para tutorial */ }
        Spacer(Modifier.height(8.dp))
        MenuButton(text = "Extração da amostra") { /* TODO: Navegar para tutorial */ }
        Spacer(Modifier.height(8.dp))
        MenuButton(text = "Fragmentação da amostra") { /* TODO: Navegar para tutorial */ }
        Spacer(Modifier.height(8.dp))
        MenuButton(text = "Escores VESS") { /* TODO: Navegar para tutorial */ }
        Spacer(Modifier.height(8.dp))
        MenuButton(text = "Extras") { /* TODO: Navegar para tutorial */ }
        Spacer(Modifier.height(8.dp))
        MenuButton(text = "Decisão de manejo") { /* TODO: Navegar para tutorial */ }
        Spacer(Modifier.height(8.dp))
        MenuButton(text = "Informações complementares") { /* TODO: Navegar para tutorial */ }
        Spacer(Modifier.height(8.dp))
        MenuButton(text = "O que é o VESS") { /* TODO: Navegar para tutorial */ }
        Spacer(Modifier.height(8.dp))
        MenuButton(text = "Minhas avaliações") { viewModel.navigateTo(Screen.HISTORY) } // Navega para o histórico (ainda não implementado)
        Spacer(Modifier.height(8.dp))
        MenuButton(text = "Sobre o App") { /* TODO: Navegar para tela "Sobre" */ }
        Spacer(Modifier.height(8.dp))
        MenuButton(text = "Configurações") { viewModel.navigateTo(Screen.CONFIGURATIONS) } // Navega para configurações (ainda não implementado)
    }
}

@Composable
fun MenuButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth() // Preenche a largura disponível
            .height(56.dp) // Altura fixa
            .padding(horizontal = 16.dp), // Preenchimento horizontal
        colors = ButtonDefaults.buttonColors(containerColor = LightColorScheme.secondary), // Cor de fundo do botão do tema
        shape = RoundedCornerShape(12.dp) // Cantos arredondados
    ) {
        Text(text = text, color = LightColorScheme.onSecondary, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
    }
}

// --- Theme & Typography (Material 3) ---
// Define o esquema de cores claras para o tema Material 3
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6B4423), // Marrom escuro para elementos primários
    onPrimary = Color.White, // Cor do texto sobre a cor primária
    primaryContainer = Color(0xFFC7A88B), // Container primário, pode ser usado para botões, cards
    onPrimaryContainer = Color.Black, // Cor do texto sobre o container primário
    secondary = Color(0xFFC7A88B), // Marrom claro para elementos secundários (botões de menu)
    onSecondary = Color.White, // Cor do texto sobre a cor secundária
    secondaryContainer = Color(0xFFA0846C), // Container secundário
    onSecondaryContainer = Color.White, // Cor do texto sobre o container secundário
    tertiary = Color(0xFF4285F4), // Cor terciária (ex: Azul do Google para "Editar")
    onTertiary = Color.White, // Cor do texto sobre a cor terciária
    background = Color(0xFFEFE9DC), // Fundo bege claro da tela
    onBackground = Color.Black, // Cor do texto sobre o fundo
    surface = Color.White, // Cor de superfície (ex: para Cards)
    onSurface = Color.Black, // Cor do texto sobre a superfície
    error = Color(0xFFB00020), // Cor de erro
    onError = Color.White, // Cor do texto sobre erro
    outline = Color(0xFF6B4423) // Cor para contornos de campos de texto, etc.
)

// Define a tipografia para o tema Material 3
private val Typography = androidx.compose.material3.Typography(
    // Por enquanto, uma tipografia vazia usando os valores padrão do Material 3.
    // Você pode personalizá-la aqui no futuro.
)
