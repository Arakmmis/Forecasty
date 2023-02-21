package com.forecasty.view.common

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.forecasty.util.SingleLiveEvent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren

open class BaseViewModel : ViewModel() {

    protected val job = SupervisorJob()
    protected val main: CoroutineDispatcher = Dispatchers.Main

    protected val _progressState: SingleLiveEvent<Boolean> = SingleLiveEvent()
    val progressState: LiveData<Boolean> = _progressState

    override fun onCleared() {
        job.cancelChildren()
        super.onCleared()
    }
}