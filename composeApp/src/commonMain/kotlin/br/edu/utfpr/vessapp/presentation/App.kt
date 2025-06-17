package br.edu.utfpr.vessapp.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
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
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.toInstant

import br.edu.utfpr.vessapp.domain.entity.Config
import br.edu.utfpr.vessapp.domain.useCase.GetConfigUseCase
import br.edu.utfpr.vessapp.domain.useCase.SaveConfigUseCase
import br.edu.utfpr.vessapp.data.repository.ConfigRepositoryImpl
import br.edu.utfpr.vessapp.presentation.configurations.ConfigurationsScreen
import br.edu.utfpr.vessapp.presentation.configurations.ConfigurationsViewModel
import br.edu.utfpr.vessapp.util.PlatformContext
import br.edu.utfpr.vessapp.util.getPlatformContext

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

// --- Shared State Management (Centralizado no AppViewModel principal) ---
enum class Screen {
    MENU,
    CONFIGURATIONS,
    NEW_EVALUATION_DESCRIPTION,
    EVALUATION_SAMPLE,
    EVALUATION_RESULT,
    FINAL_SUMMARY,
    HISTORY
}

// ViewModel principal para gerenciar a navegação e estados globais
class AppViewModel(
    val configurationsViewModel: ConfigurationsViewModel // Injetando o ViewModel de configurações
) {
    private val _currentScreen = MutableStateFlow(Screen.MENU)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // O AppViewModel não gerencia mais diretamente a Config, ele delega ao ConfigurationsViewModel
    // private val _currentConfig = MutableStateFlow(Config())
    // val currentConfig: StateFlow<Config> = _currentConfig.asStateFlow()

    // O AppViewModel pode observar o configState do ConfigurationsViewModel se precisar
    // de acesso a ele para alguma lógica global
    val currentConfig: StateFlow<Config> = configurationsViewModel.configState

    // Métodos de navegação
    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    // Funções placeholder para as telas de Avaliação (ainda não implementadas com ViewModels dedicados)
    private val _currentEvaluationSession = MutableStateFlow<EvaluationSession?>(null)
    val currentEvaluationSession: StateFlow<EvaluationSession?> = _currentEvaluationSession.asStateFlow()

    private val _currentSample = MutableStateFlow<Sample?>(null)
    val currentSample: StateFlow<Sample?> = _currentSample.asStateFlow()

    private val _completedEvaluationSessions = MutableStateFlow<List<EvaluationSession>>(emptyList())
    val completedEvaluationSessions: StateFlow<List<EvaluationSession>> = _completedEvaluationSessions.asStateFlow()

    fun startNewEvaluationSession(description: String) {
        val sessionId = "eval-${Clock.System.now().toEpochMilliseconds()}"
        _currentEvaluationSession.value = EvaluationSession(id = sessionId, description = description)
        navigateTo(Screen.EVALUATION_SAMPLE)
    }

    fun addSampleToCurrentEvaluation(sample: Sample) {
        _currentEvaluationSession.value?.let { session ->
            val updatedSamples = session.samples + sample
            _currentEvaluationSession.value = session.copy(samples = updatedSamples)
        }
        _currentSample.value = sample
    }

    fun completeCurrentEvaluationSession() {
        _currentEvaluationSession.value?.let { session ->
            val completedSession = session.copy(endTime = Clock.System.now().toEpochMilliseconds())
            _completedEvaluationSessions.value = _completedEvaluationSessions.value + completedSession
            _currentEvaluationSession.value = null
            _currentSample.value = null
        }
    }

    fun clearCurrentSample() {
        _currentSample.value = null
    }

    fun setCurrentSample(sample: Sample) {
        _currentSample.value = sample
    }
}

val LocalAppViewModel = staticCompositionLocalOf<AppViewModel> {
    error("No AppViewModel provided")
}

// --- Composable Functions (UI Principal) ---

@Composable
fun App() {
    val platformContext = getPlatformContext()

    val configLocalDataSource = remember {
        when (platformContext) {
            is PlatformContext -> {
                // Supondo que AndroidPlatformContext tem 'androidContext'
                // e IosPlatformContext não precisa de parâmetro para o construtor
                try {
                    // Tenta criar para Android (se o cast for seguro e a classe exista)
                    val androidContextField = platformContext::class.members.find { it.name == "androidContext" }
                    if (androidContextField != null) {
                        val androidContext = androidContextField.call(platformContext) as android.content.Context
                        br.edu.utfpr.vessapp.data.local.ConfigLocalDataSource(androidContext)
                    } else {
                        // Se não for Android, tentar criar para iOS (ou outro, se houver)
                        br.edu.utfpr.vessapp.data.local.ConfigLocalDataSource()
                    }
                } catch (e: Exception) {
                    println("Erro ao instanciar ConfigLocalDataSource: ${e.message}")
                    // Fallback para iOS se o Android falhar ou for iOS
                    br.edu.utfpr.vessapp.data.local.ConfigLocalDataSource()
                }
            }
            else -> error("Tipo de PlatformContext desconhecido")
        }
    }


    // 3. Criar a implementação do repositório de configuração
    val configRepository = remember {
        ConfigRepositoryImpl(configLocalDataSource)
    }

    // 4. Criar os casos de uso de configuração
    val getConfigUseCase = remember {
        GetConfigUseCase(configRepository)
    }
    val saveConfigUseCase = remember {
        SaveConfigUseCase(configRepository)
    }

    // 5. Criar o ViewModel de configurações, injetando os casos de uso
    val configurationsViewModel = remember {
        ConfigurationsViewModel(getConfigUseCase, saveConfigUseCase)
    }

    // 6. Criar o AppViewModel principal, injetando o ConfigurationsViewModel
    val appViewModel = remember {
        AppViewModel(configurationsViewModel)
    }

    val currentScreen by appViewModel.currentScreen.collectAsState()

    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography
    ) {
        Surface(modifier = Modifier.fillMaxSize()) {
            CompositionLocalProvider(LocalAppViewModel provides appViewModel) {
                when (currentScreen) {
                    Screen.MENU -> MainMenuScreen()
                    Screen.CONFIGURATIONS -> ConfigurationsScreen(
                        configurationsViewModel = configurationsViewModel,
                        appViewModel = appViewModel
                    )
                    // As outras telas ainda usam a implementação do AppViewModel original.
                    // Em um futuro, elas também terão seus próprios ViewModels injetados.
                    Screen.NEW_EVALUATION_DESCRIPTION -> NewEvaluationDescriptionScreen()
                    Screen.EVALUATION_SAMPLE -> EvaluationSampleScreen()
                    Screen.EVALUATION_RESULT -> EvaluationResultScreen()
                    Screen.FINAL_SUMMARY -> FinalSummaryScreen()
                    Screen.HISTORY -> HistoryScreen()
                }
            }
        }
    }
}

@Composable
fun MainMenuScreen() {
    val viewModel = LocalAppViewModel.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightColorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "VESS",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = LightColorScheme.primary,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        MenuButton(text = "AVALIAR") {
            viewModel.navigateTo(Screen.NEW_EVALUATION_DESCRIPTION)
        }
        Spacer(Modifier.height(16.dp))
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
        MenuButton(text = "Minhas avaliações") { viewModel.navigateTo(Screen.HISTORY) }
        Spacer(Modifier.height(8.dp))
        MenuButton(text = "Sobre o App") { /* TODO: Navegar para tela "Sobre" */ }
        Spacer(Modifier.height(8.dp))
        MenuButton(text = "Configurações") { viewModel.navigateTo(Screen.CONFIGURATIONS) }
    }
}

@Composable
fun MenuButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = LightColorScheme.secondary),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(text = text, color = LightColorScheme.onSecondary, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
    }
}

// O ConfigInputField foi movido para ConfigurationsScreen.kt, mas está aqui para referência
// para o caso de ainda ser necessário no App.kt para outros composables.
// Se já estiver em ConfigurationsScreen.kt e não for usado em outro lugar, pode remover daqui.
@Composable
fun ConfigInputField(label: String, value: String, onValueChange: (String) -> Unit, enabled: Boolean = true, isMultiLine: Boolean = false, trailingIcon: @Composable (() -> Unit)? = null) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(text = label, fontSize = 16.sp, color = LightColorScheme.primary, modifier = Modifier.padding(bottom = 4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedBorderColor = LightColorScheme.primary,
                unfocusedBorderColor = LightColorScheme.secondary,
                cursorColor = LightColorScheme.primary,
                focusedLabelColor = LightColorScheme.primary,
                unfocusedLabelColor = LightColorScheme.secondary
            ),
            shape = RoundedCornerShape(8.dp),
            enabled = enabled,
            trailingIcon = trailingIcon,
            singleLine = !isMultiLine
        )
    }
}

// Placeholder para outras telas que serão refatoradas posteriormente
@Composable
fun NewEvaluationDescriptionScreen() {
    val viewModel = LocalAppViewModel.current
    val currentConfig by viewModel.currentConfig.collectAsState()

    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val initialDescription = "Avaliação ${now.date} - ${now.time.hour.toString().padStart(2, '0')}:${now.time.minute.toString().padStart(2, '0')}"
    var evaluationDescription by remember { mutableStateOf(initialDescription) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightColorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Iniciar Nova Avaliação",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = LightColorScheme.primary,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        ConfigInputField(label = "Avaliador:", value = currentConfig.name, onValueChange = { /* Não editável aqui */ }, enabled = false)
        Spacer(Modifier.height(16.dp))
        ConfigInputField(label = "Descrição da Avaliação:", value = evaluationDescription, onValueChange = { evaluationDescription = it })

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                viewModel.startNewEvaluationSession(evaluationDescription)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = LightColorScheme.primary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = "Iniciar Avaliação", color = LightColorScheme.onPrimary, fontSize = 18.sp)
        }
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { viewModel.navigateTo(Screen.MENU) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = "Cancelar", color = Color.DarkGray, fontSize = 18.sp)
        }
    }
}


@Composable
fun EvaluationSampleScreen() {
    val viewModel = LocalAppViewModel.current
    val currentConfig by viewModel.currentConfig.collectAsState() // Obter a configuração para o avaliador
    val currentEvaluationSession by viewModel.currentEvaluationSession.collectAsState()
    val currentSample by viewModel.currentSample.collectAsState()

    val sampleInitialCount = remember(currentEvaluationSession) { currentEvaluationSession?.samples?.size?.plus(1) ?: 1 }
    val sampleId = remember(currentEvaluationSession) { "amostra-${Clock.System.now().toEpochMilliseconds()}" }
    var sampleName by remember(currentSample) { mutableStateOf(currentSample?.name ?: "Amostra № $sampleInitialCount") }
    var numLayers by remember(currentSample) { mutableStateOf(currentSample?.numLayers?.toString() ?: "1") }
    var location by remember(currentSample) { mutableStateOf(currentSample?.location ?: "") }
    var otherInfo by remember(currentSample) { mutableStateOf(currentSample?.otherInfo ?: "") }

    val layersState = remember(currentSample) {
        mutableStateListOf<MutableState<LayerEvaluation>>().apply {
            currentSample?.layers?.forEach { add(mutableStateOf(it)) }
            if (isEmpty() && numLayers.toIntOrNull() ?: 1 > 0) {
                repeat(numLayers.toIntOrNull() ?: 1) {
                    add(mutableStateOf(LayerEvaluation()))
                }
            }
        }
    }

    LaunchedEffect(numLayers) {
        val targetLayers = numLayers.toIntOrNull() ?: 1
        if (targetLayers > layersState.size) {
            repeat(targetLayers - layersState.size) {
                layersState.add(mutableStateOf(LayerEvaluation()))
            }
        } else if (targetLayers < layersState.size) {
            while (layersState.size > targetLayers && layersState.size > 0) {
                layersState.removeAt(layersState.lastIndex)
            }
        }
    }


    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(LightColorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text(
                text = "Avaliações",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = LightColorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Amostra № ${currentEvaluationSession?.samples?.size?.plus(1) ?: 1}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = LightColorScheme.primary
                )
                Button(
                    onClick = { /* Lógica para editar nome da amostra */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color(0xFF4285F4)),
                    elevation = null
                ) {
                    Text(text = "Editar")
                }
            }

            Spacer(Modifier.height(8.dp))

            ConfigInputField(label = "Nome da Amostra:", value = sampleName, onValueChange = { sampleName = it })

            Text(
                text = "Quantas camadas de solo deseja avaliar?",
                fontSize = 16.sp,
                color = LightColorScheme.primary,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                (1..5).forEach { number ->
                    NumberPickerButton(
                        number = number,
                        isSelected = numLayers.toIntOrNull() == number,
                        onClick = { numLayers = number.toString() }
                    )
                }
            }

            ConfigInputField(
                label = "Local/propriedade (GPS):",
                value = location,
                onValueChange = { location = it },
                trailingIcon = {
                    Icon(Icons.Filled.Info, "Obter localização GPS", modifier = Modifier.clickable {
                        location = "Localização obtida via GPS (simulado)"
                    })
                }
            )

            ConfigInputField(label = "Avaliador:", value = currentConfig.name, onValueChange = { /* Não editável aqui */ }, enabled = false)

            Spacer(Modifier.height(16.dp))

            layersState.forEachIndexed { index, layerState ->
                val layer = layerState.value
                Text(
                    text = "Camada ${index + 1}:",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = LightColorScheme.primary,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
                ConfigInputField(
                    label = "Comprimento camada ${index + 1}:",
                    value = layer.length,
                    onValueChange = { layerState.value = layer.copy(length = it) }
                )
                ConfigInputField(
                    label = "Nota camada ${index + 1}:",
                    value = layer.score,
                    onValueChange = { layerState.value = layer.copy(score = it) }
                )
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { /* TODO: Implementar upload de arquivo */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LightColorScheme.secondary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "Upload de Arquivo", color = LightColorScheme.onSecondary, fontSize = 18.sp)
            }

            Spacer(Modifier.height(16.dp))

            ConfigInputField(
                label = "Outras informações importantes:",
                value = otherInfo,
                onValueChange = { otherInfo = it },
                isMultiLine = true,
                trailingIcon = {
                    Icon(Icons.Filled.Info, "Sugestões", modifier = Modifier.clickable {
                    })
                }
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    val newSample = Sample(
                        id = sampleId,
                        name = sampleName,
                        numLayers = numLayers.toIntOrNull() ?: 1,
                        location = location,
                        evaluator = currentConfig.name,
                        layers = layersState.map { it.value },
                        otherInfo = otherInfo
                    )
                    viewModel.addSampleToCurrentEvaluation(newSample)
                    viewModel.navigateTo(Screen.EVALUATION_RESULT)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LightColorScheme.primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "Avaliar", color = LightColorScheme.onPrimary, fontSize = 18.sp)
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    viewModel.clearCurrentSample()
                    viewModel.navigateTo(Screen.MENU)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "Cancelar Avaliação da Amostra", color = Color.DarkGray, fontSize = 18.sp)
            }
        }
    }
}

@Composable
fun NumberPickerButton(number: Int, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .size(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) LightColorScheme.primary else LightColorScheme.secondary
        ),
        shape = RoundedCornerShape(50)
    ) {
        Text(text = number.toString(), color = LightColorScheme.onPrimary, fontSize = 18.sp)
    }
}

@Composable
fun EvaluationResultScreen() {
    val viewModel = LocalAppViewModel.current
    val currentSample by viewModel.currentSample.collectAsState()

    val sampleScore = remember(currentSample) {
        currentSample?.let { sample ->
            if (sample.layers.isNotEmpty()) {
                val sumScores = sample.layers.sumOf { it.score.toDoubleOrNull() ?: 0.0 }
                val avgScore = sumScores / sample.layers.size
                String.format("%.1f", avgScore)
            } else "N/A"
        } ?: "N/A"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightColorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Avaliações",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = LightColorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Escore Qe-VESS da amostra ${currentSample?.name ?: "X"}:",
            fontSize = 20.sp,
            color = LightColorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier
                .width(150.dp)
                .height(80.dp),
            colors = CardDefaults.cardColors(containerColor = LightColorScheme.secondary),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text(text = sampleScore, fontSize = 48.sp, fontWeight = FontWeight.Bold, color = LightColorScheme.onSecondary)
            }
        }
        Text(
            text = "Ball et al. (2017)",
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
        )

        Text(
            text = "Decisão de manejo:",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = LightColorScheme.primary,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )
        val decisionText = when (sampleScore.toDoubleOrNull() ?: 5.0) {
            in 1.0..2.9 -> "Solo com boa qualidade estrutural e não requer mudanças no manejo."
            in 3.0..3.9 -> "Solo com qualidade estrutural razoável que pode ser melhorado. Considere rotação de culturas e outras práticas."
            in 4.0..5.0 -> "Danos às funções do solo, comprometendo sua capacidade de suporte. Intervenção direta é geralmente necessária."
            else -> "Avaliação inválida."
        }
        Text(
            text = decisionText,
            fontSize = 16.sp,
            color = LightColorScheme.onBackground,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        )

        Text(
            text = "Resumo da avaliação:",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = LightColorScheme.primary,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )
        currentSample?.let { sample ->
            sample.layers.forEachIndexed { index, layer ->
                Text(
                    text = "Comprimento camada ${index + 1}: ${layer.length} cm; nota ${index + 1}: ${layer.score}",
                    fontSize = 16.sp,
                    color = LightColorScheme.onBackground,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Outras informações importantes:",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = LightColorScheme.primary,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )
        Text(
            text = currentSample?.otherInfo ?: "N/A",
            fontSize = 16.sp,
            color = LightColorScheme.onBackground,
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = {
                    viewModel.completeCurrentEvaluationSession()
                    viewModel.navigateTo(Screen.FINAL_SUMMARY)
                },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .padding(end = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LightColorScheme.primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "FINALIZAR", color = LightColorScheme.onPrimary, fontSize = 16.sp)
            }
            Button(
                onClick = {
                    viewModel.clearCurrentSample()
                    viewModel.navigateTo(Screen.EVALUATION_SAMPLE)
                },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .padding(start = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LightColorScheme.secondary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "PRÓXIMA AMOSTRA", color = LightColorScheme.onSecondary, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun FinalSummaryScreen() {
    val viewModel = LocalAppViewModel.current
    val completedSessions by viewModel.completedEvaluationSessions.collectAsState()
    val lastSession = completedSessions.lastOrNull()

    val averageScore = remember(lastSession) {
        lastSession?.samples?.let { samples ->
            if (samples.isNotEmpty()) {
                val totalScores = samples.sumOf { sample ->
                    sample.layers.sumOf { it.score.toDoubleOrNull() ?: 0.0 }
                }
                val totalLayers = samples.sumOf { it.layers.size }
                if (totalLayers > 0) {
                    String.format("%.1f", totalScores / totalLayers)
                } else "N/A"
            } else "N/A"
        } ?: "N/A"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightColorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Avaliações",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = LightColorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Escore Qe-VESS médio do local X:",
            fontSize = 20.sp,
            color = LightColorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier
                .width(150.dp)
                .height(80.dp),
            colors = CardDefaults.cardColors(containerColor = LightColorScheme.secondary),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text(text = averageScore, fontSize = 48.sp, fontWeight = FontWeight.Bold, color = LightColorScheme.onSecondary)
            }
        }
        Text(
            text = "Ball et al. (2017)",
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
        )

        Text(
            text = "Decisão de manejo para o local:",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = LightColorScheme.primary,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )
        val localManejoDecision = when (averageScore.toDoubleOrNull() ?: 5.0) {
            in 1.0..2.9 -> "Solo com boa qualidade estrutural para o local e não requer mudanças no manejo."
            in 3.0..3.9 -> "Solo com qualidade estrutural razoável para o local que pode ser melhorado. Considere rotação de culturas e outras práticas."
            in 4.0..5.0 -> "Danos às funções do solo no local, comprometendo sua capacidade de suporte. Intervenção direta é geralmente necessária."
            else -> "Avaliação média inválida."
        }
        Text(
            text = localManejoDecision,
            fontSize = 16.sp,
            color = LightColorScheme.onBackground,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        )

        Text(
            text = "Resumo da avaliação:",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = LightColorScheme.primary,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )

        lastSession?.let { session ->
            val totalSamples = session.samples.size
            val startTime = Instant.fromEpochMilliseconds(session.startTime).toLocalDateTime(TimeZone.currentSystemDefault())
            val endTime = session.endTime?.let { Instant.fromEpochMilliseconds(it).toLocalDateTime(TimeZone.currentSystemDefault()) }

            Text(text = "$totalSamples amostras", fontSize = 16.sp, color = LightColorScheme.onBackground)
            Text(text = "Avaliador: ${viewModel.currentConfig.value.name}", fontSize = 16.sp, color = LightColorScheme.onBackground)
            Text(text = "Data da avaliação: ${startTime.date}", fontSize = 16.sp, color = LightColorScheme.onBackground)
            Text(text = "Hora de início: ${startTime.time.hour.toString().padStart(2, '0')}:${startTime.time.minute.toString().padStart(2, '0')}", fontSize = 16.sp, color = LightColorScheme.onBackground)
            if (endTime != null) {
                Text(text = "Hora de término: ${endTime.time.hour.toString().padStart(2, '0')}:${endTime.time.minute.toString().padStart(2, '0')}", fontSize = 16.sp, color = LightColorScheme.onBackground)

                val startInstant = Instant.fromEpochMilliseconds(session.startTime)
                val endInstant = endTime.toInstant(TimeZone.currentSystemDefault())
                val durationMillis = endInstant - startInstant
                val minutes = durationMillis.inWholeMinutes

                Text(text = "Tempo de avaliação: $minutes minutos", fontSize = 16.sp, color = LightColorScheme.onBackground)
            }
        } ?: Text(text = "Nenhuma sessão de avaliação finalizada recentemente.", fontSize = 16.sp, color = Color.Gray)


        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                // TODO: Salvar a sessão de avaliação no Firestore/Storage
                viewModel.navigateTo(Screen.HISTORY)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = LightColorScheme.primary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = "Salvar e Finalizar", color = LightColorScheme.onPrimary, fontSize = 18.sp)
        }
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { viewModel.navigateTo(Screen.MENU) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = "Voltar ao Menu Principal", color = Color.DarkGray, fontSize = 18.sp)
        }
    }
}

@Composable
fun HistoryScreen() {
    val viewModel = LocalAppViewModel.current
    val completedSessions by viewModel.completedEvaluationSessions.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightColorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Histórico de Avaliações",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = LightColorScheme.primary,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        if (completedSessions.isEmpty()) {
            Text(text = "Nenhuma avaliação realizada ainda.", color = Color.Gray)
        } else {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(completedSessions) { session ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable { /* TODO: Implementar visualização detalhada da avaliação */ },
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = LightColorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = session.description,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = LightColorScheme.primary
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Amostras: ${session.samples.size}",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                            val startTime = Instant.fromEpochMilliseconds(session.startTime).toLocalDateTime(TimeZone.currentSystemDefault())
                            Text(
                                text = "Início: ${startTime.date}",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { viewModel.navigateTo(Screen.MENU) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = "Voltar ao Menu", color = Color.DarkGray, fontSize = 18.sp)
        }
    }
}

// --- Theme & Typography (Material 3) ---
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6B4423),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC7A88B),
    onPrimaryContainer = Color.Black,
    secondary = Color(0xFFC7A88B),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFA0846C),
    onSecondaryContainer = Color.White,
    tertiary = Color(0xFF4285F4),
    onTertiary = Color.White,
    background = Color(0xFFEFE9DC),
    onBackground = Color.Black,
    surface = Color.White,
    onSurface = Color.Black,
    error = Color(0xFFB00020),
    onError = Color.White,
    outline = Color(0xFF6B4423)
)

private val Typography = Typography(
    // Defina seus estilos de texto aqui
)
