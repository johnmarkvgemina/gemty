package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.ScrollHeader
import com.example.ui.screens.ParticipantLoggerScreen
import com.example.ui.screens.ResearcherVaultScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppMode
import com.example.ui.viewmodel.ParticipantTab
import com.example.ui.viewmodel.ResearchViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                ScrollApp()
            }
        }
    }
}

@Composable
fun ScrollApp(
    viewModel: ResearchViewModel = viewModel()
) {
    val currentMode by viewModel.appMode.collectAsStateWithLifecycle()
    val participantTab by viewModel.participantTab.collectAsStateWithLifecycle()
    val notification by viewModel.userNotification.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(notification) {
        notification?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearNotification()
        }
    }

    // Back handling
    BackHandler(enabled = currentMode == AppMode.RESEARCHER_VAULT || participantTab != ParticipantTab.DEMOGRAPHIC_QUESTIONNAIRE) {
        if (currentMode == AppMode.RESEARCHER_VAULT) {
            viewModel.setAppMode(AppMode.PARTICIPANT_APP)
        } else {
            viewModel.setParticipantTab(ParticipantTab.DEMOGRAPHIC_QUESTIONNAIRE)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            ScrollHeader(
                currentMode = currentMode,
                onModeChange = { newMode -> viewModel.setAppMode(newMode) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentMode) {
                AppMode.PARTICIPANT_APP -> {
                    ParticipantLoggerScreen(viewModel = viewModel)
                }
                AppMode.RESEARCHER_VAULT -> {
                    ResearcherVaultScreen(viewModel = viewModel)
                }
            }
        }
    }
}
