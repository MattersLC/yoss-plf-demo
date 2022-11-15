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

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    // Variables necesarias para crear la instancia de google maps
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    //private lateinit var currentLocation: Location
    //private val permissionCode = 101
    //private val TAG = MapsActivity::class.java.simpleName
    private val REQUEST_LOCATION_PERMISSION = 1
    // Coordenadas para prueba
    private var originLatitude: Double = 17.077831563590298
    private var originLongitude: Double = -96.74437901266337
    private var destinationLatitude: Double = 17.060590930692484
    private var destinationLongitude: Double = -96.72546242802322
    // Coordenadas para la creación de las diferentes rutas
    lateinit var initPoint: LatLng
    lateinit var firsPoint: LatLng
    lateinit var secondPoint: LatLng
    lateinit var thirdPoint: LatLng
    lateinit var fourthPoint: LatLng
    lateinit var fifthPoint: LatLng
    lateinit var endPoint: LatLng
    //var ArrayList<LatLng> arr:
    // Color variables
    // Arreglo para almacenar todos los colores ubicados en values/colors.xml
    private val arrayColor = arrayOf(
        "purple_200",
        "purple_500",
        "purple_700",
        "teal_200",
        "teal_700",
        "black",
        "white",
        "pink",
        "orange",
        "mint",
        "yellow",
    )

    // Variable para crear una instancia de otra clase (recuperar todas las ubicaciones en el archivo json de lugares)
    private val places: List<Place> by lazy {
        PlacesReader(this).read()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState != null) {

        }

        val ai: ApplicationInfo = applicationContext.packageManager.getApplicationInfo(applicationContext.packageName, PackageManager.GET_META_DATA)
        val value = ai.metaData["com.google.android.geo.API_KEY"]
        val key = value.toString()

        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, key)
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        mapFragment.getMapAsync { googleMap ->
            //addMarkers(googleMap)
            addClusteredMarkers(googleMap)
            // Set custom info window adapter.
            googleMap.setInfoWindowAdapter(MarkerInfoWindowAdapter(this))
        }

        //getCurrentLocationUser()

        val gd = findViewById<Button>(R.id.directions)
        gd.setOnClickListener{
            mapFragment.getMapAsync {
                mMap = it
                val originLocation = LatLng(originLatitude, originLongitude)
                //mMap.addMarker(MarkerOptions().position(originLocation))

                val destinationLocation = LatLng(destinationLatitude, destinationLongitude)
                //mMap.addMarker(MarkerOptions().position(destinationLocation))
                //val urll = getDirectionURL(originLocation, destinationLocation, key)
                //GetDirection(urll).execute()
                defineRoute01()
                /*val p1 = getDirectionURL(initPoint, firsPoint, key)
                GetDirection(p1).execute()
                val p2 = getDirectionURL(firsPoint, secondPoint, key)
                GetDirection(p2).execute()
                val p3 = getDirectionURL(secondPoint, thirdPoint, key)
                GetDirection(p3).execute()
                val p4 = getDirectionURL(thirdPoint, fourthPoint, key)
                GetDirection(p4).execute()
                val p5 = getDirectionURL(fourthPoint, fifthPoint, key)
                GetDirection(p5).execute()
                val p6 = getDirectionURL(fifthPoint, endPoint, key)
                GetDirection(p6).execute()*/
                // ruta 1
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

                defineRoute27()
                val r27 = getDirectionURL(initPoint, endPoint, key)
                GetDirection(r27).execute()

                defineRoute29()
                val r29 = getDirectionURL(initPoint, endPoint, key)
                GetDirection(r29).execute()

                defineRoute47()
                val r47 = getDirectionURL(initPoint, endPoint, key)
                GetDirection(r47).execute()

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(originLocation, 14F))
            }
        }
    }

    private fun getDirectionURL(origin:LatLng, dest:LatLng, secret: String) : String{
        return "https://maps.googleapis.com/maps/api/directions/json?origin=${origin.latitude},${origin.longitude}" +
                "&destination=${dest.latitude},${dest.longitude}" +
                "&sensor=false" +
                "&mode=transit" +
                "&key=$secret"
    }

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

        override fun onPostExecute(result: List<List<LatLng>>) {
            val lineoption = PolylineOptions()

            for (i in result.indices){
                lineoption.addAll(result[i])
                lineoption.width(10f)
                lineoption.color(Color.GREEN)
                lineoption.geodesic(true)
            }
            mMap.addPolyline(lineoption)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        // Add a marker in Oaxaca and move the camera
        //val current = LatLng(currentLocation.latitude, currentLocation.longitude)
        val oaxaca = LatLng(17.060590930692484, -96.72546242802322)
        mMap.moveCamera(CameraUpdateFactory.newLatLng(oaxaca))
        // Zoom out to zoom level 10, animating with a duration of 2 seconds.
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15f), 2000, null)

        //setMapLongClick(mMap)
        enableMyLocation()
    }

    // Checks that users have given permission
    private fun isPermissionGranted() : Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    // Checks if users have given their location and sets location enabled if so.
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

    // Callback for the result from requesting permissions.
    // This method is invoked for every call on requestPermissions(android.app.Activity, String[],
    // int).
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Check if location permissions are granted and if so enable the
        // location data layer.
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
                enableMyLocation()
            }
        }
    }

    /**
     * Adds markers to the map with clustering support.
     */
    private fun addClusteredMarkers(googleMap: GoogleMap) {
        // Create the ClusterManager class and set the custom renderer.
        val clusterManager = ClusterManager<Place>(this, googleMap)
        clusterManager.renderer =
            PlaceRenderer(
                this,
                googleMap,
                clusterManager
            )

        // Set custom info window adapter
        clusterManager.markerCollection.setInfoWindowAdapter(MarkerInfoWindowAdapter(this))

        // Add the places to the ClusterManager.
        clusterManager.addItems(places)
        clusterManager.cluster()

        // Set ClusterManager as the OnCameraIdleListener so that it
        // can re-cluster when zooming in and out.
        googleMap.setOnCameraIdleListener {
            clusterManager.onCameraIdle()
        }
    }

    fun defineRoute01() {
        initPoint = LatLng(17.135256862093122, -96.78051826442118)
        firsPoint = LatLng(17.078870343480773, -96.74295633414324)
        secondPoint = LatLng(17.066771358213252, -96.7364238245297)
        thirdPoint = LatLng(17.055757051774304, -96.73030965482059)
        fourthPoint = LatLng(17.054710142014812, -96.71300923674707)
        fifthPoint = LatLng(17.070369018218564, -96.71360897592642)
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
        endPoint = LatLng(117.036950641175952, -96.70726816347653)
    }

    fun defineRoute47() {
        initPoint = LatLng(17.056536913872083, -96.75828620870179)
        endPoint = LatLng(17.05549651768387, -96.68130361631837)
    }

}