package cl.mcortesr.android.ev2p2.ui.theme.vm

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.mcortesr.android.ev2p2.ProductosRepository
import cl.mcortesr.android.ev2p2.data.modelo.Producto
import cl.mcortesr.android.ev2p2.ui.theme.state.ProductosUIState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.UUID

class ProductosViewModel (
    private val productosRepository: ProductosRepository = ProductosRepository(),
    private val configuracionViewModel: ConfiguracionViewModel = ConfiguracionViewModel()
) : ViewModel() {

    companion object {
        const val FILE_NAME = "productos.data"
    }
    private var job: Job? = null

    private val _uiState = MutableStateFlow(ProductosUIState())

    val uiState: StateFlow<ProductosUIState> = _uiState.asStateFlow()

    init {
        obtenerProductos()
    }

    fun obtenerProductosGuardadosEnDisco(fileInputStream: FileInputStream) {
        productosRepository.getProductosEnDisco(fileInputStream)
    }

    fun guardarProductosEnDisco(fileOutputStream: FileOutputStream) {
        productosRepository.guardarProductosEnDisco(fileOutputStream)
    }

    private fun obtenerProductos() {
        job?.cancel()
        job = viewModelScope.launch {
            val productosStream = productosRepository.getProdcutosStream()
            productosStream.collect{ productosActualizados ->
                Log.v("ProductosViewModel", "obtenerProductos() update{]")
                _uiState.update { currentState ->
                    currentState.copy(
                        productos = productosActualizados
                    )
                }
            }
        }
    }

    fun agregarProducto(producto:String) {
        job = viewModelScope.launch {
            val p = Producto(UUID.randomUUID().toString(), producto)
            productosRepository.insertar(p)
            _uiState.update {
                it.copy(mensaje = "Producto agregado: ${p.nombre}")
            }
            obtenerProductos()
        }
    }

    fun eliminarProductos(producto: Producto) {
        job = viewModelScope.launch {
            productosRepository.eliminar(producto)
            _uiState.update {
                it.copy(mensaje = "Producto eliminado: ${producto.nombre}")
            }
            obtenerProductos()
        }
    }

    fun marcarProductoEncontrado(productoId: String, encontrado: Boolean) {
        val producto = _uiState.value.productos.find { it.id == productoId }
        producto?.let {
            it.encontrado = encontrado
            _uiState.update { currentState ->
                currentState.copy(productos = currentState.productos.toMutableList())
            }
        }
    }

    fun ordenarProductos() {
        val listaOrdenada = if (_uiState.value.ordenAlfabetico) {
            _uiState.value.productos.sortedBy { it.nombre }
        } else {
            _uiState.value.productos
        }

        val listaFiltrada = if (_uiState.value.mostrarPrimeroNoEncontrados) {
            listaOrdenada.filter { !it.encontrado }
        } else {
            listaOrdenada
        }

        _uiState.value = _uiState.value.copy(productos = listaFiltrada)
    }

    fun activarOrdenAlfabetico() {
        _uiState.update {
            it.copy(
                ordenAlfabetico = true,
                mensaje = "La lista de productos se ha ordenado alfabeticamente"
            )
        }
        ordenarProductos()
    }

    fun desactivarOrdenAlfabetico() {
        _uiState.update {
            it.copy(ordenAlfabetico = false)
        }
        ordenarProductos()
    }

    fun activarMostrarPrimeroNoEncontrados() {
        _uiState.update {
            it.copy(mostrarPrimeroNoEncontrados = true)
        }
        ordenarProductos()
    }

    fun desactivarMostrarPrimeroNoEncontrados() {
        _uiState.update {
            it.copy(mostrarPrimeroNoEncontrados = false)
        }
        ordenarProductos()
    }
}
