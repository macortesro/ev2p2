package cl.mcortesr.android.ev2p2.evaluacion2_p2.data

import android.util.Log
import cl.mcortesr.android.ev2p2.data.modelo.Producto
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

class ProductoDiskDataSource () {

    fun obtener(fileInputStream: FileInputStream):List<Producto> {
        return try {
            fileInputStream.use { fis ->
                ObjectInputStream(fis).use { ois ->
                    ois.readObject() as? List<Producto> ?: emptyList()
                }
            }
        } catch (fnfex:FileNotFoundException) {
            emptyList<Producto>()
        } catch (ex:Exception) {
            Log.e("ProductoDiskDataSource", "obtener ex:Exception ${ex.toString()}")
            emptyList<Producto>()
        }
    }

    fun guardar(fileOutputStream: FileOutputStream, productos:List<Producto>) {
        fileOutputStream.use { fos ->
            ObjectOutputStream(fos).use { oos ->
                oos.writeObject(productos)
            }
        }
    }
}