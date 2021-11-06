package com.akeshishi.tracker.base.extensions

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText


/**
 * Used as a listener of EditText when the user is typing text and cleaning it.
 *
 * @param action Do somethings after text changed.
 */
fun EditText.textChangeMonitor(action: () -> Unit) {
    addTextChangedListener(
        object : TextWatcher {
            var change = false
            override fun afterTextChanged(p0: Editable?) {
                if (change)
                    action()
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit
            override fun onTextChanged(char: CharSequence?, p1: Int, p2: Int, p3: Int) {
                change = true
            }
        }
    )
}