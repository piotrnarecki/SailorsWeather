package pl.piotrnarecki.sailorsweather.models

import java.io.Serializable

data class Wind(
    val speed: Double,
    val deg: Double,


    ) : Serializable {
}