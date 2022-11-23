package ito.plf.yossdemo

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

class FireBaseRepo {

    val db = FirebaseFirestore.getInstance()

    fun setUserData() {

    }

    fun getUserData(): String? {
        var nombre: String? = ""
        val docRef = db.collection("rutas").document("ruta")
        docRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    Log.d("TAG","DocumentSnapshot data: ${document.data}")
                    Log.d("TAG","${document.get("first") as String}")
                    nombre = document.getString("nombre")
                } else {
                    Log.d("TAG","document don't found")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("TAG","error al obtener $exception")
            }

        return nombre
    }
}