package com.example.pi_iii_grupo6

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

class DateTextFormat(private val editText: EditText) : TextWatcher {

    private var current = ""

    override fun afterTextChanged(s: Editable?) {}

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        if (s.toString() != current) {
            val input = s.toString()
            if (input.length == 2 || input.length == 5) {
                val newText = input + "/"
                editText.setText(newText)
                editText.setSelection(newText.length)
            }
            current = editText.text.toString()
        }
    }
}
