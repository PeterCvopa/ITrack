package com.example.itrack.location

import java.math.BigDecimal

data class Statistics(
    val maxSpeed: Float = 0f,
    val minSpeed: Float = 0f,
    val avgSpeed: Float = 0f,
    val distance: BigDecimal = BigDecimal.ZERO,
    val maxAlt: Double = 0.0,
    val minAlt: Double = 0.0,
    val avgAlt: Double = 0.0
)