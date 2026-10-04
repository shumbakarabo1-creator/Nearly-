package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.NearlyApp
import com.example.ui.NearlyViewModel
import com.example.ui.theme.NearlyTheme

class MainActivity : ComponentActivity() {

    private val viewModel: NearlyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
            val systemDark = isSystemInDarkTheme()
            val isDarkTheme = when (userProfile.themeMode) {
                "dark" -> true
                "light" -> false
                else -> systemDark
            }

            NearlyTheme(darkTheme = isDarkTheme) {
                NearlyApp(viewModel = viewModel)
            }
        }
    }
}
