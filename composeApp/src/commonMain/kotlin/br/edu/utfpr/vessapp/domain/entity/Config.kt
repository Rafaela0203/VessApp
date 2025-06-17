package br.edu.utfpr.vessapp.domain.entity

import kotlinx.serialization.Serializable

@Serializable
data class Config(
    val name: String = "",
    val email: String = "",
    val country: String = "",
    val address: String = "",
    val cityState: String = "",
    val language: String = "Português (Brasil)"
)