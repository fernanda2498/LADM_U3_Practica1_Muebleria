package tecnm.mx.tepic.ladm_u3_practica1_muebleria

import android.content.ContentValues
import android.content.Intent
import android.database.sqlite.SQLiteException
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*
import java.util.ArrayList

class MainActivity : AppCompatActivity() {
    var baseSQLite = BaseDatos(this, "prueba1", null, 1)
    var baseFirebase = FirebaseFirestore.getInstance()
    var listaID = ArrayList<String>()
    var listaIDFirebase = ArrayList<String>()
    var dataLista = ArrayList<String>()
    var dataFirestore = ArrayList<String>()
    var sincro = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cargarSQL()

        if (listaID.isEmpty()){
            button2.isEnabled =false
        }

        button.setOnClickListener {
            insertar()
        }
        button2.setOnClickListener {
            sincronizar()
        }
    }

    private fun sincronizar() {
        try {
            var Fcliente = ""
            var Fproducto = ""
            var Fprecio = 0f

            var seleccion = baseSQLite.readableDatabase
            var SQL = "SELECT * FROM Apartado"

            var cursor = seleccion.rawQuery(SQL, null)

            if (cursor.moveToFirst()) {
                do {
                    Fcliente = cursor.getString(1)
                    Fproducto = cursor.getString(2)
                    Fprecio = cursor.getString(3).toFloat()

                    var datosInsertar = hashMapOf(
                            "nombre" to Fcliente,
                            "producto" to Fproducto,
                            "precio" to Fprecio
                    )
                    //Insertar en Firebase
                    baseFirebase.collection("Apartado")
                            .add(datosInsertar)
                            .addOnSuccessListener {
                                alerta("SE INSERTO CORRECTAMENTE EN LA NUBE")
                                limpiarCampos()
                            }
                            .addOnFailureListener {
                                mensaje("ERROR: ${it.message!!}")
                            }
                } while (cursor.moveToNext())
            } else {
                mensaje("NO HAY INFORMACION A SINCRONIZAR")
            }
            eliminarTSQL()
            sincro = true
            cargarFirestore()
            button2.isEnabled = false
            seleccion.close()
        } catch (err: SQLiteException) {
            mensaje(err.message!!)
        }
    }

    private fun cargarFirestore() {
        baseFirebase.collection("Apartado").addSnapshotListener{ querySnapshot, error ->

            if(error != null){
                mensaje(error.message!!)
                return@addSnapshotListener
            }

            dataFirestore.clear()
            listaIDFirebase.clear()

            for (document in querySnapshot!!){
                var cadena = "[${document.getString("nombre")}] / ${document.get("producto")} / ${document.get("precio")}"
                dataFirestore.add(cadena)
                listaIDFirebase.add(document.id.toString())
            }
            listaapartado2.adapter = ArrayAdapter<String>(this,android.R.layout.simple_expandable_list_item_1,dataFirestore)
            listaapartado2.setOnItemClickListener { parent, view, position, id ->
                dialogoEliminaActualiza(position)
            }
        }
    }

    private fun insertar() {
        try {
            var transaccion = baseSQLite.writableDatabase
            var SQL = "INSERT INTO Apartado VALUES(NULL,'${nombreCliente.text.toString()}','${producto.text.toString()}','${precio.text.toString().toFloat()}')"
            transaccion.execSQL(SQL)
            cargarSQL()
            limpiarCampos()
            transaccion.close()
        } catch (err: SQLiteException) {
            mensaje(err.message.toString())
        }
        button2.isEnabled = true
    }

    private fun cargarSQL() {
        try {
            var transaccion = baseSQLite.readableDatabase
            var objetosA = ArrayList<String>() // arreglo dinamico de cadenas

            var SQL = "SELECT * FROM Apartado"

            var cursor = transaccion.rawQuery(SQL,null)
            listaID.clear()

            if (cursor.moveToFirst()) {
                do {
                    var data = "[ "+cursor.getString(1)+"] - "+cursor.getString(2)+" -"+cursor.getString(3)
                    objetosA.add(data)
                    listaID.add(cursor.getInt(0).toString())
                } while (cursor.moveToNext())
            } else {
                objetosA.add("NO HAY DATOS CAPTURDOS AUN")
            }
            transaccion.close()
            listaapartado.adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, objetosA)
            listaapartado.setOnItemClickListener { adapterView, view, posicionItemSeleccionado, l ->
                var idABorrar = listaID.get(posicionItemSeleccionado)
                //mensaje("ID RECUPERADO"+ listaID.get(posicionItemSeleccionado))
                AlertDialog.Builder(this).setMessage("QUE DESEAS HACER CON ID: " + idABorrar + "?")
                        .setTitle("ATENCION")
                        .setPositiveButton("ELIMINAR") { d, i ->
                            eliminarSQL(idABorrar)
                        }
                        .setNeutralButton("ACTUALIZAR") { d, i ->
                            var intent = Intent(this, MainActivity2::class.java)
                            intent.putExtra("idElegido", idABorrar)
                            intent.putExtra("sincronizar",sincro)
                            startActivity(intent)
                        }
                        .setNegativeButton("CANCELAR") { d, i ->
                            d.dismiss()
                        }
                        .show()
            }
            transaccion.close()
        } catch (err: SQLiteException) {
            mensaje(err.message!!)
        }
    }

    fun eliminarSQL(idABorrar:String) {
        try {
            var transaccion = baseSQLite.writableDatabase
            var SQL = "DELETE  FROM Apartado where idApartado = ${idABorrar}"
            transaccion.execSQL(SQL)
            cargarSQL()
            transaccion.close()
        } catch (err: SQLiteException) {
            mensaje(err.message!!)
        }
    }
    private fun eliminarTSQL() {
        try {
            var eliminar = baseSQLite.writableDatabase
            var SQL = "DELETE FROM APARTADO"
            eliminar.execSQL(SQL)
            cargarSQL()
            eliminar.close()
        }catch (err:SQLiteException){
            mensaje(err.message!!)
        }
    }

    private fun dialogoEliminaActualiza(position: Int) {
        var idElegido = listaIDFirebase.get(position)
        AlertDialog.Builder(this).setTitle("ATENCION!!")
                .setMessage("QUE DESEAS HACER CON \n ${dataFirestore.get(position)}?")
                .setPositiveButton("ELIMINAR"){d, i->
                    eliminarFirestore(idElegido)
                }
                .setNeutralButton("ACTUALIZAR"){d,i->
                    var intent = Intent(this,MainActivity2::class.java)
                    intent.putExtra("idElegido",idElegido)
                    intent.putExtra("sincronizar",sincro)
                    startActivity(intent)
                }
                .setNegativeButton("CANCELAR"){d,i->}
                .show()
    }

    private fun eliminarFirestore(idElegido: String) {
        baseFirebase.collection("Apartado")
                .document(idElegido)
                .delete()
                .addOnFailureListener {
                    mensaje("ERROR! ${it.message!!}")
                }
                .addOnSuccessListener {
                    mensaje("SE ELIMINO CON EXITO")
                }
    }

    fun limpiarCampos() {
        nombreCliente.setText("")
        producto.setText("")
        precio.setText("")
    }


    fun mensaje(m: String) {
        AlertDialog.Builder(this)
                .setTitle("ATENCION")
                .setMessage(m)
                .setPositiveButton("OK") { d, i -> }
                .show()
    }

    private fun alerta(s: String) {
        Toast.makeText(this, s, Toast.LENGTH_LONG).show()
    }
}

