package com.forecasty.view.common

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren

open class BaseViewModel : ViewModel() {

    protected val job = SupervisorJob()
    protected val main: CoroutineDispatcher = Dispatchers.Main

    override fun onCleared() {
        job.cancelChildren()
        super.onCleared()
    }
}