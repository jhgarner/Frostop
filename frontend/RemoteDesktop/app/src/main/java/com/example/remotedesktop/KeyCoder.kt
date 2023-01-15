package com.example.remotedesktop

import android.view.KeyEvent.*

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

    fun ArrayList<Int>.then(range: IntRange): ArrayList<Int> {
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