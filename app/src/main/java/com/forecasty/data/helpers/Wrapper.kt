package com.forecasty.data.helpers

data class Wrapper<out T> (
    val status: Status,
    val data: T?,
    val error: Throwable?
)