package br.edu.utfpr.vessapp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform