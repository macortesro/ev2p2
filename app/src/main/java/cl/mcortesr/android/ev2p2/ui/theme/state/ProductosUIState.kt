package cl.mcortesr.android.ev2p2.ui.theme.state

import cl.mcortesr.android.ev2p2.data.modelo.Producto

data class ProductosUIState (
    val mensaje:String = "",
    val productos:List<Producto> = emptyList(),
    val ordenAlfabetico: Boolean = false,
    val mostrarPrimeroNoEncontrados: Boolean = false
)

