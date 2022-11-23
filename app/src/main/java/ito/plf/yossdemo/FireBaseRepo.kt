package ito.plf.yossdemo

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

class FireBaseRepo {

    val db = FirebaseFirestore.getInstance()

    fun setUserData() {

    }

    fun getUserData(){

        val docRef = db.collection("rutas").document("ruta")
        docRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val nombre: String? = document.getString("nombre")
                    val camiones: Int? = document.getLong("num_camiones")?.toInt()
                    Log.d("TAG","nombre ruta = ${nombre} => cantidad de camiones = ${camiones}")
                } else {
                    Log.d("TAG","document don't found")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("TAG","error al obtener $exception")
            }

    }
}