package ito.plf.yossdemo

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.libraries.places.api.Places
import com.google.gson.Gson
import com.google.maps.android.clustering.ClusterManager
import ito.plf.yossdemo.databinding.ActivityMapsBinding
import ito.plf.yossdemo.place.Place
import ito.plf.yossdemo.place.PlaceRenderer
import ito.plf.yossdemo.place.PlacesReader
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlin.random.Random

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    // Variables necesarias para crear la instancia de google maps
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val REQUEST_LOCATION_PERMISSION = 1
    // Coordenadas para la creación de las diferentes rutas
    lateinit var initPoint: LatLng
    lateinit var endPoint: LatLng
    // Arreglo para almacenar todos los colores ubicados en values/colors.xml
    private val arrayColor = arrayOf(
        Color.GREEN,
        Color.BLUE,
        Color.GRAY,
        Color.BLACK,
        Color.CYAN,
        Color.MAGENTA,
        Color.RED,
        Color.YELLOW,
        Color.DKGRAY,
        Color.LTGRAY
    )
    private var randomColor : Int = 1

    // Variable para crear una instancia de otra clase (recuperar todas las ubicaciones en el archivo json de lugares)
    private val places: List<Place> by lazy {
        PlacesReader(this).read()
    }

    // Función principal de nuestro programa, se define al lanzar la aplicación
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val ai: ApplicationInfo = applicationContext.packageManager.getApplicationInfo(applicationContext.packageName, PackageManager.GET_META_DATA)
        val value = ai.metaData["com.google.android.geo.API_KEY"]
        val key = value.toString()

        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, key)
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        // Obtiene el SupportMapFragment y notifica cuando el mapa esté listo para ser usado
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        mapFragment.getMapAsync { googleMap ->
            addClusteredMarkers(googleMap)
            // Definimos el InfoWindowAdapter personalizado
            googleMap.setInfoWindowAdapter(MarkerInfoWindowAdapter(this))
        }

        val gd = findViewById<Button>(R.id.directions)
        gd.setOnClickListener{
            mapFragment.getMapAsync {
                mMap = it
                // ruta 1
                defineRoute01()
                val p0 = getDirectionURL(initPoint, endPoint, key)
                GetDirection(p0).execute()
                // ruta 2
                defineRoute02()
                val r2 = getDirectionURL(initPoint, endPoint, key)
                GetDirection(r2).execute()
                // ruta 3
                defineRoute03()
                val r3 = getDirectionURL(initPoint, endPoint, key)
                GetDirection(r3).execute()
                // ruta 10
                defineRoute10()
                val r10 = getDirectionURL(initPoint, endPoint, key)
                GetDirection(r10).execute()
                // ruta 27
                defineRoute27()
                val r27 = getDirectionURL(initPoint, endPoint, key)
                GetDirection(r27).execute()
                // ruta 29
                defineRoute29()
                val r29 = getDirectionURL(initPoint, endPoint, key)
                GetDirection(r29).execute()
                // ruta 47
                defineRoute47()
                val r47 = getDirectionURL(initPoint, endPoint, key)
                GetDirection(r47).execute()
                // ruta 62
                defineRoute62()
                val r62 = getDirectionURL(initPoint, endPoint, key)
                GetDirection(r62).execute()
                // ruta 71
                defineRoute71()
                val r71 = getDirectionURL(initPoint, endPoint, key)
                GetDirection(r71).execute()
            }
        }
    }

    // Función para escribir apropiadamente la sentencia de consulta de direcciones en el servidor
    private fun getDirectionURL(origin:LatLng, dest:LatLng, secret: String) : String{
        return "https://maps.googleapis.com/maps/api/directions/json?origin=${origin.latitude},${origin.longitude}" +
                "&destination=${dest.latitude},${dest.longitude}" +
                "&sensor=false" +
                "&mode=transit" +
                "&key=$secret"
    }

    // Función para trazar los polylines (líneas que representan las rutas), se trata de obtener
    // los puntos y cambios de dirección necesarios para trazar la ruta.
    fun decodePolyline(encoded: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0
        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat
            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng
            val latLng = LatLng((lat.toDouble() / 1E5),(lng.toDouble() / 1E5))
            poly.add(latLng)
        }
        return poly
    }

    // Clase para obtener las direcciones necesarias para trazar una ruta entre dos puntos
    @SuppressLint("StaticFieldLeak")
    private inner class GetDirection(val url : String) : AsyncTask<Void, Void, List<List<LatLng>>>(){
        override fun doInBackground(vararg params: Void?): List<List<LatLng>> {
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val data = response.body!!.string()

            val result =  ArrayList<List<LatLng>>()
            try{
                val respObj = Gson().fromJson(data,MapData::class.java)
                val path =  ArrayList<LatLng>()
                for (i in 0 until respObj.routes[0].legs[0].steps.size){
                    path.addAll(decodePolyline(respObj.routes[0].legs[0].steps[i].polyline.points))
                }
                result.add(path)
            }catch (e:Exception){
                e.printStackTrace()
            }
            return result
        }

        // En este método es dónde se declaran las instrucciones para graficar un polyline
        override fun onPostExecute(result: List<List<LatLng>>) {
            val lineoption = PolylineOptions()

            randomColor = Random.nextInt(0,9)

            for (i in result.indices){
                lineoption.addAll(result[i])
                lineoption.width(12f)
                lineoption.color(arrayColor[randomColor])
                lineoption.geodesic(true)
            }
            mMap.addPolyline(lineoption)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        // Creamos una variable ubicada en el centro de la ciudad de Oaxaca
        val oaxaca = LatLng(17.060590930692484, -96.72546242802322)
        // Movemos la cámara hacia la dirección de nuestra variable
        mMap.moveCamera(CameraUpdateFactory.newLatLng(oaxaca))
        // Le indicamos a la app que realice un zoom de 15 puntos en dos segundos
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15f), 2000, null)

        // Habilitamos la captura de localización actual del usuario
        enableMyLocation()
        //setMapLongClick(mMap)
    }

    // Función para verificar que el usuario haya concedido los permisos necesarios
    private fun isPermissionGranted() : Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    // Función para verificar que el usuario haya aceptado proporcionar su ubicación actual
    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (isPermissionGranted()) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            mMap.isMyLocationEnabled = true
        }
        else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }

    // Callback para el resultado de la consulta de permisos
    // Este método es invocado para cada llamada en requestPermissions(android.app.Activity, String[], int)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Check if location permissions are granted and if so enable the location data layer.
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
                enableMyLocation()
            }
        }
    }

    // Agregar marcadores al mapa con soporte de clusters
    private fun addClusteredMarkers(googleMap: GoogleMap) {
        // Crea una clase ClusterManager y define el renderizador personalizado
        val clusterManager = ClusterManager<Place>(this, googleMap)
        clusterManager.renderer =
            PlaceRenderer(
                this,
                googleMap,
                clusterManager
            )

        // Define un info window adapter personalizado
        clusterManager.markerCollection.setInfoWindowAdapter(MarkerInfoWindowAdapter(this))

        // Agrega las ubicaciones (obtenidas del archivo JSON) al ClusterManager
        clusterManager.addItems(places)
        clusterManager.cluster()

        // Definimos el ClusterManager como el OnCameraIdListener, para que pueda ser
        // re-clusteado cuando se haga zoom in y zoom out
        googleMap.setOnCameraIdleListener {
            clusterManager.onCameraIdle()
        }
    }

    // Definimos las direcciones para todas las rutas que usaremos en el programa
    fun defineRoute01() {
        initPoint = LatLng(17.135256862093122, -96.78051826442118)
        endPoint = LatLng(17.09452923524003, -96.70664722097415)
    }
    fun defineRoute02() {
        initPoint = LatLng(17.1029627094166, -96.74622724928918)
        endPoint = LatLng(17.081782591386094, -96.69656797897142)
    }
    fun defineRoute03() {
        initPoint = LatLng(16.996928707520496, -96.75258144125172)
        endPoint = LatLng(17.08279434217865, -96.72372154609354)
    }
    fun defineRoute10() {
        initPoint = LatLng(17.103285701601795, -96.7351856977866)
        endPoint = LatLng(17.05549299988845, -96.68130462824716)
    }
    fun defineRoute27() {
        initPoint = LatLng(16.99787860973189, -96.75826847357983)
        endPoint = LatLng(17.09393498938193, -96.69787315669852)
    }
    fun defineRoute29() {
        initPoint = LatLng(17.13508596500535, -96.78071377286666)
        endPoint = LatLng(17.036950641175952, -96.70726816347653)
    }
    fun defineRoute47() {
        initPoint = LatLng(17.056536913872083, -96.75828620870179)
        endPoint = LatLng(17.05549651768387, -96.68130361631837)
    }
    fun defineRoute62() {
        initPoint = LatLng(17.100964699971502, -96.76073536539256)
        endPoint = LatLng(17.068067735865863, -96.6889075848271)
    }
    fun defineRoute71() {
        initPoint = LatLng(17.064434263538747, -96.76110420063237)
        endPoint = LatLng(17.037706362863844, -96.69302667681531)
    }

}