package com.example.remotedesktop

import android.view.KeyEvent.ACTION_DOWN
import android.view.KeyEvent.ACTION_UP
import android.view.KeyEvent.KEYCODE_0
import android.view.KeyEvent.KEYCODE_1
import android.view.KeyEvent.KEYCODE_2
import android.view.KeyEvent.KEYCODE_3
import android.view.KeyEvent.KEYCODE_4
import android.view.KeyEvent.KEYCODE_5
import android.view.KeyEvent.KEYCODE_6
import android.view.KeyEvent.KEYCODE_7
import android.view.KeyEvent.KEYCODE_8
import android.view.KeyEvent.KEYCODE_9
import android.view.KeyEvent.KEYCODE_A
import android.view.KeyEvent.KEYCODE_APOSTROPHE
import android.view.KeyEvent.KEYCODE_BACKSLASH
import android.view.KeyEvent.KEYCODE_CAPS_LOCK
import android.view.KeyEvent.KEYCODE_COMMA
import android.view.KeyEvent.KEYCODE_DPAD_RIGHT
import android.view.KeyEvent.KEYCODE_DPAD_UP
import android.view.KeyEvent.KEYCODE_ENTER
import android.view.KeyEvent.KEYCODE_EQUALS
import android.view.KeyEvent.KEYCODE_ESCAPE
import android.view.KeyEvent.KEYCODE_F1
import android.view.KeyEvent.KEYCODE_F12
import android.view.KeyEvent.KEYCODE_GRAVE
import android.view.KeyEvent.KEYCODE_LEFT_BRACKET
import android.view.KeyEvent.KEYCODE_MINUS
import android.view.KeyEvent.KEYCODE_PAGE_DOWN
import android.view.KeyEvent.KEYCODE_PAGE_UP
import android.view.KeyEvent.KEYCODE_PERIOD
import android.view.KeyEvent.KEYCODE_RIGHT_BRACKET
import android.view.KeyEvent.KEYCODE_SEMICOLON
import android.view.KeyEvent.KEYCODE_SHIFT_LEFT
import android.view.KeyEvent.KEYCODE_SLASH
import android.view.KeyEvent.KEYCODE_SPACE
import android.view.KeyEvent.KEYCODE_TAB
import android.view.KeyEvent.KEYCODE_Z
import androidx.compose.ui.input.key.NativeKeyEvent

object KeyCoder {
    fun toCode(code: Int): Int {
        // If an android update reorders these then I'll be sad
        val codes = ArrayList<Int>()
            .then(KEYCODE_0..KEYCODE_9)
            .then(KEYCODE_DPAD_UP..KEYCODE_DPAD_RIGHT)
            .then(KEYCODE_A..KEYCODE_Z)
            .then(KEYCODE_COMMA..KEYCODE_SPACE)
            .then(KEYCODE_ENTER..KEYCODE_SLASH)
            .then(KEYCODE_PAGE_UP..KEYCODE_PAGE_DOWN)
            .then(KEYCODE_ESCAPE..KEYCODE_CAPS_LOCK)
            .then(KEYCODE_F1..KEYCODE_F12)
        return codes.indexOf(code)
    }

    fun charToCode(char: Char): List<NativeKeyEvent> {
        return when (char) {
            in 'a'..'z' -> (char - 'a' + KEYCODE_A).click()
            in 'A'..'Z' -> (char - 'A' + KEYCODE_A).click().shifted()
            in '0'..'9' -> (char - '0' + KEYCODE_0).click()
            ' ' -> KEYCODE_SPACE.click()
            ',' -> KEYCODE_COMMA.click()
            '<' -> KEYCODE_COMMA.click().shifted()
            '.' -> KEYCODE_PERIOD.click()
            '>' -> KEYCODE_PERIOD.click().shifted()
            '!' -> KEYCODE_1.click().shifted()
            '@' -> KEYCODE_2.click().shifted()
            '#' -> KEYCODE_3.click().shifted()
            '$' -> KEYCODE_4.click().shifted()
            '%' -> KEYCODE_5.click().shifted()
            '^' -> KEYCODE_6.click().shifted()
            '&' -> KEYCODE_7.click().shifted()
            '*' -> KEYCODE_8.click().shifted()
            '(' -> KEYCODE_9.click().shifted()
            ')' -> KEYCODE_0.click().shifted()
            '-' -> KEYCODE_MINUS.click()
            '_' -> KEYCODE_MINUS.click().shifted()
            '=' -> KEYCODE_EQUALS.click()
            '+' -> KEYCODE_EQUALS.click().shifted()
            '[' -> KEYCODE_LEFT_BRACKET.click()
            '{' -> KEYCODE_LEFT_BRACKET.click().shifted()
            ']' -> KEYCODE_RIGHT_BRACKET.click()
            '}' -> KEYCODE_RIGHT_BRACKET.click().shifted()
            '\\' -> KEYCODE_BACKSLASH.click()
            '|' -> KEYCODE_BACKSLASH.click().shifted()
            ';' -> KEYCODE_SEMICOLON.click()
            ':' -> KEYCODE_SEMICOLON.click().shifted()
            '\'' -> KEYCODE_APOSTROPHE.click()
            '"' -> KEYCODE_APOSTROPHE.click().shifted()
            '/' -> KEYCODE_SLASH.click()
            '?' -> KEYCODE_SLASH.click().shifted()
            '`' -> KEYCODE_GRAVE.click()
            '~' -> KEYCODE_GRAVE.click().shifted()
            '\n' -> KEYCODE_ENTER.click()
            '\t' -> KEYCODE_TAB.click()
            else -> listOf()
        }
    }

    fun List<NativeKeyEvent>.shifted(): List<NativeKeyEvent> {
        val shiftDown = NativeKeyEvent(ACTION_DOWN, KEYCODE_SHIFT_LEFT)
        val shiftUp = NativeKeyEvent(ACTION_UP, KEYCODE_SHIFT_LEFT)
        return listOf(shiftDown) + this + listOf(shiftUp)
    }

    fun Int.click(): List<NativeKeyEvent> {
        return listOf(NativeKeyEvent(ACTION_DOWN, this), NativeKeyEvent(ACTION_UP, this))
    }

    private fun ArrayList<Int>.then(range: IntRange): ArrayList<Int> {
        for (i in range) {
            this.add(i)
        }
        return this
    }

    fun ArrayList<Int>.then(i: Int): ArrayList<Int> {
        this.add(i)
        return this
    }
}