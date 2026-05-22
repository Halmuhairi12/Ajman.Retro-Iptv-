package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.ui.IptvViewModel
import com.example.ui.screens.MainIptvScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Supports full Edge-to-Edge immersion and safe margins out-of-the-box
        enableEdgeToEdge()
        
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val viewModel: IptvViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
                    MainIptvScreen(viewModel = viewModel)
                }
            }
        }
    }
}
