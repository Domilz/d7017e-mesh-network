package com.epiroc.ble.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import org.intellij.lang.annotations.JdkConstants.HorizontalAlignment

@Composable
fun HomeScreen(
    navController: NavController
) {

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxSize(),
    ) {
        Box(
            modifier = Modifier
                .size(150.dp)
                .clip(CircleShape)
                .background(Color.Blue, CircleShape)
                .clickable {
                    navController.navigate(Screen.BleListScreen.route){
                        popUpTo(Screen.HomeScreen.route){
                            inclusive = true
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ){
            Text(
                text = "Central Start",
                fontSize = 35.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        Box(
            modifier = Modifier
                .size(150.dp)
                .clip(CircleShape)
                .background(Color.Blue, CircleShape)
                .clickable {
                    navController.navigate(Screen.PeripheralScreen.route){
                        popUpTo(Screen.HomeScreen.route){
                            inclusive = true
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ){
            Text(
                text = "Peripheral Start",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}