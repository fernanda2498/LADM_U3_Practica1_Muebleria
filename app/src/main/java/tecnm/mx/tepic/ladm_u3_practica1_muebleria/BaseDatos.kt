package tecnm.mx.tepic.ladm_u3_practica1_muebleria

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class BaseDatos(
    context: Context?,
    name: String?,
    factory: SQLiteDatabase.CursorFactory?,
    version: Int
) : SQLiteOpenHelper(context, name, factory, version) {

    override fun onCreate(p0: SQLiteDatabase) {
        p0.execSQL("CREATE TABLE Apartado(IdApartado INTEGER PRIMARY KEY AUTOINCREMENT, nombreCliente VARCHAR(200),Producto VARCHAR(200),Precio FLOAT)")
    }
    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p3: Int) {
        // update y updgrade = update =actualizacion menor    upgrade = cambio mayor
        //Actualizar
        // p1 = version anterior
        //p3 = version nueva
        //SE INVOCA SOLO CUANDO CAMBIAS EL NUMERO DE VERSION
    }



}