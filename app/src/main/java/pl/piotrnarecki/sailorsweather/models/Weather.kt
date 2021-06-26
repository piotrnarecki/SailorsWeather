package pl.piotrnarecki.sailorsweather.models

import java.io.Serializable

data class Weather(
    val id: Int,
    val main: String,
    val descripion: String,
    val icon: String


) : Serializable {
}