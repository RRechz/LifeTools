// File: ui/truthordare/SpinnerWithQuestionsScreen.kt
package com.babelsoftware.lifetools.ui.truthordare

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack // Geri ikonu için import
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
// import androidx.compose.ui.res.stringResource // Eğer başlık için string resource kullanıyorsanız
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpinnerWithQuestionsScreen(onNavigateBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Çarkıfelek + AI Soruları") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { // Geri butonu eklendi
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        }
    ) { paddingValues -> // Bu paddingValues Scaffold'dan geliyor
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // <<--- DÜZELTME: Scaffold'un padding'ini uygula
                .padding(16.dp), // Kendi ek padding'iniz (opsiyonel, paddingValues'tan sonra)
            contentAlignment = Alignment.Center
        ) {
            Text("Çarkıfelek + AI Soruları Ekranı (Yakında!)")
        }
    }
}