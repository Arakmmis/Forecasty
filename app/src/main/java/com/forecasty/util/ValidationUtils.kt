package com.forecasty.util

object ValidationUtils {

    const val LAT_LON_REGEX =
        "^[-+]?([1-8]?\\d(\\.\\d+)?|90(\\.0+)?) *,\\s*[-+]?(180(\\.0+)?|((1[0-7]\\d)|([1-9]?\\d))(\\.\\d+)?)\$"

    const val ZIP_CODE_REGEX =
        "\\d*"
}