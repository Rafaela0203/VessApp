package br.edu.utfpr.vessapp.domain.entity

import kotlinx.serialization.Serializable // Adicione este import

/**
 * Representa a configuração do usuário
 */
@Serializable // Adicione esta anotação
data class Config(
    val name: String = "",
    val email: String = "",
    val country: String = "",
    val address: String = "",
    val cityState: String = "",
    val language: String = "Português (Brasil)"
)