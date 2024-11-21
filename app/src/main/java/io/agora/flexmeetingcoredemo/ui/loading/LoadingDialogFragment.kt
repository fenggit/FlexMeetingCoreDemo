package io.agora.flexmeetingcoredemo.ui.loading

import android.app.Dialog
import android.os.Bundle
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

/**
 * @author chenbinhang@agora.io
 * @date 2024/11/21 18:25
 */
class LoadingDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val progressBar = ProgressBar(requireContext())
        return AlertDialog.Builder(requireContext())
            .setView(progressBar)
            .setCancelable(false) // 点击外部不可取消
            .create()
    }
}