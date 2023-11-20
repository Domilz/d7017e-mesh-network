package com.epiroc.wifiaware.Screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(
    navController: NavController,
    Content: @Composable (Modifier) -> Unit,
) {
    Scaffold(
        topBar = {
            androidx.compose.material3.TopAppBar(
                title = { Text(text = "Eprioc Mesh Network") },
                navigationIcon = {
                    if (navController.previousBackStackEntry != null) {
                        run {
                            IconButton(onClick = { navController.navigateUp() }) {
                                Icon(
                                    imageVector = Icons.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        }
                    } else {
                        null
                    }
                }

            )
        },
        content = {padding ->
            Content(Modifier.padding(padding))
        }
    )
}