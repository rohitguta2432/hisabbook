package com.hisabbook.app.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri

object IntentHelpers {
    fun dial(ctx: Context, phone: String) {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${phone.filter { it.isDigit() || it == '+' }}"))
        ctx.startActivity(intent)
    }

    fun whatsappText(ctx: Context, phone: String?, text: String) {
        val normalized = phone?.filter { it.isDigit() }?.let { if (it.startsWith("91") || phone.startsWith("+91")) it else "91$it" }
        val uri = if (normalized != null) "https://wa.me/$normalized?text=${Uri.encode(text)}" else "https://wa.me/?text=${Uri.encode(text)}"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        ctx.startActivity(intent)
    }

    fun share(ctx: Context, text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        ctx.startActivity(Intent.createChooser(intent, null))
    }

    fun createBackupFile(ctx: Context, launcher: androidx.activity.result.ActivityResultLauncher<Intent>, suggestedName: String) {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/octet-stream"
            putExtra(Intent.EXTRA_TITLE, suggestedName)
        }
        launcher.launch(intent)
    }

    fun openBackupFile(ctx: Context, launcher: androidx.activity.result.ActivityResultLauncher<Intent>) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
        launcher.launch(intent)
    }
}
