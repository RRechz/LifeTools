// File: ui/truthordare/TruthOrDareHubScreen.kt
package com.babelsoftware.lifetools.ui.truthordare

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.babelsoftware.lifetools.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TruthOrDareHubScreen(
    onNavigateToTruthQuestions: () -> Unit,
    onNavigateToSpinner: () -> Unit,
    onNavigateToSpinnerWithQuestions: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.truth_or_dare)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Button(onClick = onNavigateToTruthQuestions, modifier = Modifier.fillMaxWidth()) {
                Text("Sadece Doğruluk Soruları (AI)")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onNavigateToSpinner, modifier = Modifier.fillMaxWidth()) {
                Text("Çarkıfelek Oyunu")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onNavigateToSpinnerWithQuestions, modifier = Modifier.fillMaxWidth()) {
                Text("Çarkıfelek + AI Soruları")
            }
        }
    }
}