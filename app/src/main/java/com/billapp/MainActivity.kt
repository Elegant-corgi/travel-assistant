package com.billapp

import android.os.Bundle
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
    }
}
