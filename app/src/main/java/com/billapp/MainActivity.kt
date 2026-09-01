package com.billapp

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.billapp.data.FileAaRepository
import com.billapp.data.FileBillRepository
import com.billapp.data.FileTravelRepository
import com.billapp.ui.BillApp
import com.billapp.ui.BillTheme
import com.billapp.ui.BillViewModel
import com.billapp.ui.BillViewModelFactory

class MainActivity : ComponentActivity() {
    private val viewModel: BillViewModel by viewModels {
        BillViewModelFactory(
            FileBillRepository(applicationContext),
            FileAaRepository(applicationContext),
            FileTravelRepository(applicationContext),
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BillTheme {
                BillApp(viewModel)
            }
        }
        if (savedInstanceState == null) {
            handleShareIntent(intent)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleShareIntent(intent)
    }

    private fun handleShareIntent(intent: Intent?) {
        if (intent?.action != Intent.ACTION_SEND) return

        val text = intent.getStringExtra(Intent.EXTRA_TEXT)
            ?: intent.getCharSequenceExtra(Intent.EXTRA_TEXT)?.toString()
            ?: intent.getStringExtra(Intent.EXTRA_SUBJECT)
        if (!text.isNullOrBlank()) {
            val message = viewModel.handleTravelShareText(text)
            showTravelShareToastIfNeeded(message)
            return
        }

        val uri = intent.getSharedImageUri()
        if (uri != null) {
            contentResolver.takePersistableReadPermissionIfAvailable(intent, uri)
            val message = viewModel.handleTravelShareImage()
            showTravelShareToastIfNeeded(message)
        }
    }

    private fun showTravelShareToastIfNeeded(message: String) {
        if (message == "请先创建一个出行计划") {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }
}

private fun Intent.getSharedImageUri(): Uri? {
    val streamUri: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
    } else {
        @Suppress("DEPRECATION")
        getParcelableExtra(Intent.EXTRA_STREAM)
    }
    return streamUri ?: clipData?.takeIf { it.itemCount > 0 }?.getItemAt(0)?.uri
}

private fun android.content.ContentResolver.takePersistableReadPermissionIfAvailable(
    intent: Intent,
    uri: Uri,
) {
    if (intent.flags and Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION == 0) return
    runCatching {
        takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
}
