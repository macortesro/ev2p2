package cl.mcortesr.android.ev2p2.data.modelo

import java.io.Serializable

data class Producto(
    val id:String,
    val nombre:String,
    var encontrado: Boolean = false
) : Serializable