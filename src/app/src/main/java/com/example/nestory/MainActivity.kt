package com.example.nestory

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import com.example.nestory.ui.navigation.NestoryApp
import com.example.nestory.ui.theme.NestoryTheme

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NestoryTheme {
                NestoryApp()
            }
        }
    }
} 
