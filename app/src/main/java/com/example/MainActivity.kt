package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.StudioDatabase
import com.example.data.repository.StudioRepository
import com.example.ui.components.StudioTopBar
import com.example.ui.screens.BookingScreen
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.CameramanScreen
import com.example.ui.screens.EditingScreen
import com.example.ui.screens.VcrScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.StudioDarkNavy
import com.example.ui.viewmodel.StudioViewModel

class StudioViewModelFactory(private val repository: StudioRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StudioViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StudioViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                StudioApp()
            }
        }
    }
}

@Composable
fun StudioApp() {
    val context = LocalContext.current
    val database = StudioDatabase.getDatabase(context)
    val repository = StudioRepository(database.studioDao())
    val viewModel: StudioViewModel = viewModel(factory = StudioViewModelFactory(repository))

    val selectedTab by viewModel.selectedTab.collectAsState()

    // Handle back button on sub-tabs to return to primary bookings tab
    BackHandler(enabled = selectedTab != 0) {
        viewModel.setTab(0)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            StudioTopBar(
                selectedTab = selectedTab,
                onTabSelected = { viewModel.setTab(it) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(StudioDarkNavy)
        ) {
            when (selectedTab) {
                0 -> BookingScreen(viewModel = viewModel)
                1 -> CalendarScreen(viewModel = viewModel)
                2 -> CameramanScreen(viewModel = viewModel)
                3 -> VcrScreen(viewModel = viewModel)
                4 -> EditingScreen(viewModel = viewModel)
            }
        }
    }
}
