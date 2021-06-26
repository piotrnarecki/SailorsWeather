package pl.piotrnarecki.sailorsweather.models

import java.io.Serializable

data class Main(
    val temp: Double,
    val feelsLike: Double,
    val tempMin: Double,
    val tempMax: Double,
    val pressure: Double,
    val humidity: Int,



    ):Serializable {
}