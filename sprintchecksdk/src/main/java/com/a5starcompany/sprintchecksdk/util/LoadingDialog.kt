package com.a5starcompany.sprintchecksdk.util

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.a5starcompany.sprintchecksdk.R

class LoadingDialog : DialogFragment() {

    private var loadingText: String = "Processing..."
    private var subText: String = "Please wait while we verify your face"

    companion object {
        fun newInstance(
            loadingText: String = "Processing...",
            subText: String = "Please wait while we verify your face"
        ): LoadingDialog {
            return LoadingDialog().apply {
                this.loadingText = loadingText
                this.subText = subText
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.dialog_loading, null)

        view.findViewById<TextView>(R.id.loading_text).text = loadingText
        view.findViewById<TextView>(R.id.loading_sub_text).text = subText

        builder.setView(view)

        val dialog = builder.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)

        return dialog
    }

    fun updateText(newText: String) {
        dialog?.findViewById<TextView>(R.id.loading_text)?.text = newText
    }
}