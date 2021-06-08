package tecnm.mx.tepic.ladm_u3_practica1_muebleria

import android.content.ContentValues
import android.database.sqlite.SQLiteException
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main2.*

class MainActivity2 : AppCompatActivity() {
    var baseSQLite = BaseDatos(this,"prueba1",null,1)
    var baseFirestore = FirebaseFirestore.getInstance()
    var id = ""
    var idFire =""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        var extra = intent.extras //Recuperar el id
        var sincro = extra!!.getBoolean("sincronizar")

        if (!sincro){
            id= extra.getString("idElegido")!!
            cargarSQL(extra,id)
        }else{
            idFire = extra.getString("idElegido")!!
            cargarFirestore(extra,idFire)
        }
        button3.setOnClickListener {
            if (!sincro){
                actualizarSQL(id)
            }else{
                actualizarFirestore(idFire)
            }
        }
        button4.setOnClickListener {
            finish()
        }
    }

    private fun actualizarFirestore(idFire: String) {
        baseFirestore.collection("Apartado")
            .document(idFire)
            .update("nombre",nombreCactualizar.text.toString(),
                "producto",productoActualizar.text.toString(),
                "precio",precioActualizar.text.toString())
            .addOnSuccessListener {
                alerta("ACTUALIZADO CON EXITO")
            }
            .addOnFailureListener {
                mensaje("ERROR NO SE PUDO ACTUALIZAR")
            }
    }

    private fun alerta(s: String) {
        Toast.makeText(this,s, Toast.LENGTH_LONG).show()
    }

    private fun cargarFirestore(extra: Bundle, idFire: String) {
        baseFirestore.collection("Apartado")
            .document(idFire)
            .get()
            .addOnSuccessListener {
                nombreCactualizar.setText(it.getString("nombre"))
                productoActualizar.setText(it.getString("producto"))
                precioActualizar.setText(it.get("precio").toString())
            }
            .addOnFailureListener {
                mensaje("ERROR: ${it.message!!}")
            }
    }

    private fun cargarSQL(extra: Bundle, id: String) {
        try{
            var transaccion = baseSQLite.readableDatabase
            var cursor = transaccion.query("Apartado", arrayOf("nombre","producto","precio"),"IdApartado=?",
                arrayOf(this.id),null,null,null)
            if (cursor.moveToFirst()){
                nombreCactualizar.setText(cursor.getString(0))
                productoActualizar.setText(cursor.getString(1))
                precioActualizar.setText(cursor.getString(2))
            }else{
                mensaje("ERROR, NO SE PUDO RECUPERAR LA DATA DE ID ${this.id}")
            }
            transaccion.close()
        }catch (err: SQLiteException){
            mensaje(err.message!!)
        }
    }

    private fun actualizarSQL(id: String) {
        try {
            var transaccion = baseSQLite.writableDatabase
            var valores = ContentValues()

            valores.put("nombre", nombreCactualizar.text.toString())
            valores.put("producto",productoActualizar.text.toString())
            valores.put("precio",precioActualizar.text.toString())
            var resultado = transaccion.update("Apartado",valores,"ID=?", arrayOf(id))

            if (resultado >0) {
                mensaje("SE ACTUALIZO CORRECTAMENTE ID")
                finish()//cierra la ventana
            }else {
                mensaje("ERROR! NO SE ACTUALIZO")
            }
            transaccion.close()
        }catch (err:SQLiteException){
            mensaje(err.message!!)
        }
    }

    fun mensaje(m:String){
        AlertDialog.Builder(this)
            .setTitle("ATENCION")
            .setMessage(m)
            .setPositiveButton("OK"){d,i->}
            .show()
    }
}