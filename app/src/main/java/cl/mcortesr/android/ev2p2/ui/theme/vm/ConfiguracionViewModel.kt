package cl.mcortesr.android.ev2p2.ui.theme.vm

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import cl.mcortesr.android.ev2p2.data.modelo.Producto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ConfiguracionViewModel : ViewModel() {

    var ordenAlfabetico by mutableStateOf(false)
        private set

    var mostrarPrimeroNoEncontrados by mutableStateOf(false)
        private set

}
