package com.nestorian87.eter.ui.screens.auth

private val EMAIL_REGEX = Regex(
    pattern = "^[A-Za-z0-9+_.%-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
)

fun isValidEmail(value: String): Boolean = EMAIL_REGEX.matches(value)
