package com.forecasty.data.helpers

data class Result<out T> (
    val status: Status,
    val data: T?,
    val error: Throwable?
)