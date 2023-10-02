package com.epiroc.wifiaware

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.epiroc.wifiaware.Screens.HomeScreen
import com.epiroc.wifiaware.Screens.PublishScreen
import com.epiroc.wifiaware.Screens.SubscribeScreen
import com.epiroc.wifiaware.Screens.navController
import com.epiroc.wifiaware.ViewModels.HomeScreenViewModel
import com.epiroc.wifiaware.ViewModels.HomeScreenViewModelFactory
import com.epiroc.wifiaware.ui.theme.WifiAwareTransportTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize packageManager
        val packageManager = packageManager

        // Create ViewModel and pass it to HomeScreen function





        setContent {
            WifiAwareTransportTheme {
                navController = rememberNavController()
                val viewModel = viewModel<HomeScreenViewModel>(factory = HomeScreenViewModelFactory(context = LocalContext.current, packageManager))
                NavHost(navController = navController, startDestination = Screen.Home.route){
                    composable(Screen.Home.route) {
                        HomeScreen(navController,viewModel)
                    }
                    composable(Screen.Publish.route) {
                        PublishScreen(navController,viewModel)
                    }
                    composable(Screen.Subscribe.route) {
                        SubscribeScreen(navController,viewModel)
                    }
                }
               
            }
        }
    }
}