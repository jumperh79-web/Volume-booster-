package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.MainVolumeBoosterScreen
import com.example.ui.theme.VolumeBoosterTheme
import com.example.viewmodel.VolumeBoosterViewModel

class MainActivity : ComponentActivity() {

  private val viewModel: VolumeBoosterViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      VolumeBoosterTheme {
        MainVolumeBoosterScreen(viewModel = viewModel)
      }
    }
  }
}

