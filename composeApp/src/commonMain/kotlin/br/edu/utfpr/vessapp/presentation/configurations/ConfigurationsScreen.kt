package br.edu.utfpr.vessapp.presentation.configurations

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.edu.utfpr.vessapp.domain.entity.Config
import br.edu.utfpr.vessapp.presentation.AppViewModel
import br.edu.utfpr.vessapp.LightColorScheme
import br.edu.utfpr.vessapp.LocalAppViewModel
import br.edu.utfpr.vessapp.Screen
import br.edu.utfpr.vessapp.AppViewModel

@Composable
fun ConfigurationsScreen(
    configurationsViewModel: ConfigurationsViewModel,
    appViewModel: AppViewModel
) {
    val config by configurationsViewModel.configState.collectAsState()

    var name by remember { mutableStateOf(config.name) }
    var email by remember { mutableStateOf(config.email) }
    var country by remember { mutableStateOf(config.country) }
    var address by remember { mutableStateOf(config.address) }
    var cityState by remember { mutableStateOf(config.cityState) }
    var language by remember { mutableStateOf(config.language) }

    LaunchedEffect(config) {
        name = config.name
        email = config.email
        country = config.country
        address = config.address
        cityState = config.cityState
        language = config.language
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
                text = "Configurações",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = LightColorScheme.primary,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            ConfigInputField(label = "Nome:", value = name, onValueChange = { name = it })
            ConfigInputField(label = "E-mail:", value = email, onValueChange = { email = it })
            ConfigInputField(label = "País:", value = country, onValueChange = { country = it })
            ConfigInputField(label = "Endereço:", value = address, onValueChange = { address = it })
            ConfigInputField(label = "Cidade - Estado:", value = cityState, onValueChange = { cityState = it })
            ConfigInputField(label = "Idioma:", value = language, onValueChange = { language = it }, enabled = false)

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    val updatedConfig = Config(name, email, country, address, cityState, language)
                    configurationsViewModel.saveConfig(updatedConfig)
                    appViewModel.navigateTo(Screen.MENU)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LightColorScheme.primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "Salvar Configurações", color = LightColorScheme.onPrimary, fontSize = 18.sp)
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { /* TODO: Navegar para Termos e Condições */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LightColorScheme.secondary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "Termos e condições de uso", color = LightColorScheme.onSecondary, fontSize = 18.sp)
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { appViewModel.navigateTo(Screen.MENU) },
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
}

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
