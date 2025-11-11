package com.divora.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.divora.data.UserDataStore

class CalculatorViewModelFactory(private val application: Application, private val userDataStore: UserDataStore) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CalculatorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CalculatorViewModel(application, userDataStore) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
