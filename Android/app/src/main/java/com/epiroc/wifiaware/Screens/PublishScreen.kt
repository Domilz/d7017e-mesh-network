package com.epiroc.wifiaware.Screens

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.epiroc.wifiaware.ViewModels.HomeScreenViewModel


@Composable
fun PublishScreen(navController: NavController,viewModel: HomeScreenViewModel) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Publish Activity")

        Text(
            text = "Publish Status: ${viewModel.publishMessageLiveData.value.toString()}",
            modifier = Modifier.padding(16.dp)
        )

        Button(
            onClick = {
                navController.popBackStack()
            },
            modifier = Modifier.padding(8.dp)
        ) {
            Text("GO BACK")
        }
    }
}
