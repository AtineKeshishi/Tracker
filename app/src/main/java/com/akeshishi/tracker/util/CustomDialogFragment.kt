package com.akeshishi.tracker.util

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.akeshishi.tracker.R

class CustomDialogFragment : DialogFragment() {

    // On rotation, the buttonFunction is null and never gets called
    var buttonFunction: (() -> Unit)? = null
    var title = 0
    var message = 0

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(getString(R.string.yes)) { _, _ -> buttonFunction?.let { it() } }
            .setNegativeButton(getString(R.string.no)) { dialog, _ -> dialog.cancel() }
            .create()
    }
}

class CustomActionDialogFragment : DialogFragment() {

    var shareFunction: (() -> Unit)? = null
    var deleteFunction: (() -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val actions = arrayOf("Share", "Delete")
        return AlertDialog.Builder(context)
            .setItems(actions) { _, item ->
                when (item){
                    0 -> shareFunction?.let { it() }
                    1 -> deleteFunction?.let { it() }
                }
            }
            .create()
    }
}
