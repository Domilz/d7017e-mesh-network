package com.epiroc.wifiaware.ViewModels
import android.content.Context
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

import kotlin.IllegalArgumentException

class HomeScreenViewModelFactory(private val context: Context, private val packageManager: PackageManager) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeScreenViewModel::class.java)) {
            return HomeScreenViewModel(context,packageManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

