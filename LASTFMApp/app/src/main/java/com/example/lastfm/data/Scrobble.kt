package com.example.lastfm.data

data class Scrobble (
    val id: String = "",
    val title: String = "",
    val user: String = "",
    val userId: String = "",
    val genero: String = "",
    val info: String = ""
    ) {
        constructor() : this("", "", "", "", "", "")
    }
