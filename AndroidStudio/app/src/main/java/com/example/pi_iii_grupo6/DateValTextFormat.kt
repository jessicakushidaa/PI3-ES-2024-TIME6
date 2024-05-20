package com.example.pi_iii_grupo6

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

class DateValTextFormat(private val editText: EditText) : TextWatcher {

    // Flag para evitar recursões infinitas ao atualizar o texto
    private var isUpdating = false

    override fun afterTextChanged(s: Editable?) {}

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        // Verifica se a atualização do texto está em progresso para evitar recursões
        if (isUpdating) {
            return
        }

        // Obtém o texto atual do EditText
        val input = s.toString()

        // Remove todos os caracteres que não são dígitos
        val digitsOnly = input.replace("[^\\d]".toRegex(), "")

        // Verifica se o comprimento dos dígitos é adequado para formatação (até 4 dígitos)
        if (digitsOnly.length <= 4) {
            // Constrói a string formatada com uma barra após o 2º dígito
            val monthYear = buildString {
                for (i in digitsOnly.indices) {
                    append(digitsOnly[i])
                    if (i == 1 && i != digitsOnly.length - 1) {
                        append('/')
                    }
                }
            }

            // Ativa a flag de atualização
            isUpdating = true
            // Define o texto formatado no EditText
            editText.setText(monthYear)
            // Define a posição do cursor após a barra
            editText.setSelection(monthYear.length)
            // Desativa a flag de atualização
            isUpdating = false
        }

    }
}
