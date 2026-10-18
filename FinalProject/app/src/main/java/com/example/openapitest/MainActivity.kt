package com.example.openapitest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.openapitest.data.settings.AppSettings
import com.example.openapitest.ui.theme.CodexTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as CodexApplication).container
        setContent {
            val settings by container.settingsRepository.settings
                .collectAsStateWithLifecycle(initialValue = AppSettings())
            CodexTheme(themeMode = settings.theme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    App(container = container)
                }
            }
        }
    }
}
