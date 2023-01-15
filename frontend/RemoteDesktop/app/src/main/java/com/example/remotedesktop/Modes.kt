package com.example.remotedesktop

import androidx.compose.runtime.MutableState

data class Modes(private val modes: MutableState<Set<String>>) {
    fun addMode(mode: String) {
        modes.update { it + mode }
    }

    fun removeMode(mode: String) {
        modes.update { it - mode }
    }

    fun inMode(mode: String): Boolean {
        return if (mode.startsWith("!")) {
            !modes.component1().contains(mode.substring(1))
        } else {
            modes.component1().contains(mode)
        }
    }

    private fun <T> MutableState<T>.update(f: (T) -> T) {
        this.component2()(f(this.component1()))
    }
}