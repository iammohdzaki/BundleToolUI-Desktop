package ui.screens

import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState
import com.rickclephas.kmp.observableviewmodel.MutableStateFlow
import com.rickclephas.kmp.observableviewmodel.ViewModel
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.component.KoinComponent

class HomeViewModel : ViewModel(), KoinComponent {

    private val _logs = MutableStateFlow(viewModelScope, "")
    @NativeCoroutinesState
    val logs = _logs.asStateFlow()

    fun updateLogs(log: String, isAppend: Boolean = true) {
        if (isAppend) _logs.value += log
        else _logs.value = log
    }
}