package com.a5starcompany.sprintchecksdk.util

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.regula.facesdk.model.results.LivenessResponse

object LivenessResponseUtil {
    fun response(context: Context?, livenessResponse: LivenessResponse) {
        if (livenessResponse.exception != null) {
            Toast.makeText(
                context,
                "Error: " + livenessResponse.exception!!.message,
                Toast.LENGTH_SHORT
            ).show()
            Log.d("TAG", "liveness response: ${livenessResponse.exception!!.message}")
        }else {
            Toast.makeText(
                context,
                "Liveness status: " + livenessResponse.liveness.name,
                Toast.LENGTH_SHORT
            ).show()
            Log.d("TAG", "liveness response: ${livenessResponse.liveness.name}")
        }
    }
}