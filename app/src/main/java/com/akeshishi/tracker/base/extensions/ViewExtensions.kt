package com.akeshishi.tracker.base.extensions

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import com.akeshishi.tracker.R
import com.google.android.material.snackbar.Snackbar

fun View.makeVisible() {
    visibility = View.VISIBLE
}

fun View.makeGone() {
    visibility = View.GONE
}

fun View.makeClickable() {
    isClickable = true
}

fun View.makeUnClickable() {
    isClickable = false
}

fun View.hideKeyboard() {
    val inputManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputManager.hideSoftInputFromWindow(windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
}

fun View.showSnackBar(message: String) {
    Snackbar.make(this, message, Snackbar.LENGTH_SHORT)
        .apply { view.elevation = 1000F }
        .setBackgroundTint(ContextCompat.getColor(context, R.color.amber_500))
        .show()
}
