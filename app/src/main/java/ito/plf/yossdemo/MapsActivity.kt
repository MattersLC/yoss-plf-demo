package ito.plf.yossdemo

//import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.gson.Gson
import ito.plf.yossdemo.databinding.ActivityMapsBinding
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.*
import kotlin.random.Random

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val REQUEST_LOCATION_PERMISSION = 1
    private val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    private val DEFAULT_ZOOM = 15
    private var locationPermissionGranted: Boolean = false
    private var cameraPosition: CameraPosition? = null
    private val TAG: String = ito.plf.yossdemo.MapsActivity::class.java.getSimpleName()
    private val KEY_CAMERA_POSITION = "camera_position"
    private val KEY_LOCATION = "location"
    // Coordenadas para la creación de las diferentes rutas
    lateinit var initPoint: LatLng
    lateinit var endPoint: LatLng
    val route: MutableList<LatLng> = mutableListOf()
    private var lastKnownLocation: Location? = null
    val defaultLocation = LatLng(17.060590930692484, -96.72546242802322)
    val markers: MutableList<Marker> = mutableListOf()
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
    val polylines: MutableList<Polyline> = mutableListOf()
    val markers_animations: MutableList<Marker> = mutableListOf()
    //private val markers_animations: List<Marker> = java.util.ArrayList()
    /*private var polyline1: Polyline? = null
    private var polyline2: Polyline? = null
    private var polyline3: Polyline? = null
    private var polyline10: Polyline? = null
    private var polyline27: Polyline? = null
    private var polyline29: Polyline? = null
    private var polyline62: Polyline? = null
    private var polyline71: Polyline? = null*/
    // Arrays con las coordenadas que irán dentro de los polylines
    private var ruta01: List<LatLng> = java.util.ArrayList()
    private var ruta02: List<LatLng> = java.util.ArrayList()
    private var ruta03: List<LatLng> = java.util.ArrayList()
    private var ruta10: List<LatLng> = java.util.ArrayList()
    private var ruta27: List<LatLng> = java.util.ArrayList()
    private var ruta29: List<LatLng> = java.util.ArrayList()
    private var ruta47: List<LatLng> = java.util.ArrayList()
    private var ruta62: List<LatLng> = java.util.ArrayList()
    private var ruta71: List<LatLng> = java.util.ArrayList()
    private var clicks = 0
    // Nombres de rutas
    private val nombreRuta01 = "HACIENDA BLANCA - VOLCANES"
    private val nombreRuta02 = "U.H. RICARDO FLORE MAGÓN 1a ETAPA - COLONIA DEL MAESTRO"
    private val nombreRuta03 = "CENTRO DE REHABILITACIÓN INFANTIL - ISSSTE"
    private val nombreRuta10 = "SANTA CRUZ AMILPAS - 2da. SECCION DE GUADALUPE VICTORIA (La loma)"
    private val nombreRuta27 = "CRIT - COLONIA AMPLIACIÓN 7 REGIONES"
    private val nombreRuta29 = "HACIENDA BLANCA - PLAZA DEL VALLE"
    private val nombreRuta47 = "MONTE ALBÁN - SANTA CRUZ AMILPAS"
    private val nombreRuta62 = "SAN JACINTO AMILPAS - MACROPLAZA"
    private val nombreRuta71 = "ROSARIO 5ta ETAPA - FRACCIONAMIENTO LOS ÁLAMOS"
    // Variables para acceder a la api key de google maps
    private lateinit var ai: ApplicationInfo
    private lateinit var key: String

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.omenu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition())
            outState.putParcelable(KEY_LOCATION, lastKnownLocation)
        }
        super.onSaveInstanceState(outState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Retrieve location and camera position from saved instance state.

        // Retrieve location and camera position from saved instance state.
        if (savedInstanceState != null) {
            lastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION)!!
            cameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION)
        }

        ai = applicationContext.packageManager.getApplicationInfo(applicationContext.packageName, PackageManager.GET_META_DATA)
        val value = ai.metaData["com.google.android.geo.API_KEY"]
        key = value.toString()

        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, key)
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Llenar las rutas con sus respectivas coordenadas
        val rutas = Rutas()
        rutas.crearRutas()
        ruta01 = rutas.ruta01
        ruta02 = rutas.ruta02
        ruta03 = rutas.ruta03
        ruta10 = rutas.ruta10
        ruta27 = rutas.ruta27
        ruta29 = rutas.ruta29
        ruta47 = rutas.ruta47
        ruta62 = rutas.ruta62
        ruta71 = rutas.ruta71
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Use a custom info window adapter to handle multiple lines of text in the info window contents.
        this.mMap.setInfoWindowAdapter(object : InfoWindowAdapter {
            // Return null here, so that getInfoContents() is called next.
            override fun getInfoWindow(arg0: Marker): View? {
                return null
            }

            override fun getInfoContents(marker: Marker): View? {
                // Inflate the layouts for the info window, title and snippet.
                val infoWindow = layoutInflater.inflate(
                    R.layout.custom_info_contents,
                    findViewById<View>(R.id.map) as FrameLayout,
                    false
                )
                val title = infoWindow.findViewById<TextView>(R.id.title)
                title.text = marker.title
                val snippet = infoWindow.findViewById<TextView>(R.id.snippet)
                snippet.text = marker.snippet
                return infoWindow
            }
        })

        val locationResult = fusedLocationProviderClient.lastLocation
        locationResult.addOnCompleteListener(
            this
        ) { task ->
            if (task.isSuccessful) {
                // Set the map's camera position to the current location of the device.
                lastKnownLocation = task.result
                if (lastKnownLocation != null) {
                    mMap.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(lastKnownLocation!!.latitude, lastKnownLocation!!.longitude),
                            DEFAULT_ZOOM.toFloat()
                        )
                    )
                }
            } else {
                Log.d(TAG,"Current location is null. Using defaults.")
                Log.e(TAG,"Exception: %s", task.exception)
                mMap.moveCamera(
                    CameraUpdateFactory
                        .newLatLngZoom(
                            defaultLocation,
                            DEFAULT_ZOOM.toFloat()
                        )
                )
                mMap.getUiSettings().setMyLocationButtonEnabled(false)
            }
        }

        enableMyLocation()
        //getLocationPermission()
        //updateLocationUI()
        //getDeviceLocation()

        // Set a listener for marker click.
        mMap.setOnMarkerClickListener(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.option_get_plazas) {
            deleteAll()
            showPlazasMarkers()
        } else if (item.itemId == R.id.option_get_universities) {
            deleteAll()
            showUniversitiesMarkers()
        } else if (item.itemId == R.id.option_get_hospitals) {
            deleteAll()
            showHospitalesMarkers()
        } else if (item.itemId == R.id.option_get_mercados) {
            deleteAll()
            showMercadosMarkers()
        } else if (item.itemId == R.id.option_get_tourism) {
            deleteAll()
            showTourismMarkers()
        } else if (item.itemId == R.id.option_get_others) {
            deleteAll()
            showOthersMarkers()
        }
        return true
    }

    private fun showUniversitiesMarkers() {
        val ITO = LatLng(17.077831563590298, -96.74437901266337)
        val markerITO: Marker? = mMap.addMarker(
            MarkerOptions()
                .position(ITO)
                .title("ITO")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.education_48))
        )
        markerITO?.tag = "ITO"

        val UABJO = LatLng(17.049872183365846, -96.71283873471712)
        val markerUABJO: Marker? = mMap.addMarker(
            MarkerOptions()
                .position(UABJO)
                .title("UABJO")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.education_48))
        )
        markerUABJO?.tag = "UABJO"
        val UABJOMedicina = LatLng(17.082816925948777, -96.71852379850247)
        val markerUABJOMedicina: Marker? = mMap.addMarker(
            MarkerOptions()
                .position(UABJOMedicina)
                .title("UABJO Facultad de Medicina")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.education_48))
        )
        markerUABJOMedicina?.tag = "UABJOMedicina"
        val Anahuac = LatLng(16.995618003606356, -96.75274957418532)
        // Controlar las polylines en el mapa
        val markerAnahuac: Marker? = mMap.addMarker(
            MarkerOptions()
                .position(Anahuac)
                .title("Universidad Anáhuac")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.education_48))
        )
        markerAnahuac?.tag = "Anahuac"
        val URSE = LatLng(17.047871316849644, -96.69302808478277)
        val markerURSE: Marker? = mMap.addMarker(
            MarkerOptions()
                .position(URSE)
                .title("URSE")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.education_48))
        )
        markerURSE?.tag = "URSE"
        if (markerITO != null) {
            markers.add(markerITO)
        }
        if (markerUABJO != null) {
            markers.add(markerUABJO)
        }
        if (markerUABJOMedicina != null) {
            markers.add(markerUABJOMedicina)
        }
        if (markerAnahuac != null) {
            markers.add(markerAnahuac)
        }
        if (markerURSE != null) {
            markers.add(markerURSE)
        }
    }

    private fun showPlazasMarkers() {
        val MacroPlaza = LatLng(17.068019243865443, -96.69451813050182)
        val markerMacroPlaza: Marker? = mMap.addMarker(
            MarkerOptions()
                .position(MacroPlaza)
                .title("Macro Plaza")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.cart_48))
        )
        markerMacroPlaza?.tag = "MacroPlaza"
        val PlazaDelValle = LatLng(17.039779108913287, -96.7122114844547)
        val markerPlazaDelValle: Marker? = mMap.addMarker(
            MarkerOptions()
                .position(PlazaDelValle)
                .title("Plaza Del Valle")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.cart_48))
        )
        markerPlazaDelValle?.tag = "PlazaDelValle"
        val PlazaBella = LatLng(17.0755874389814, -96.75797439138461)
        val markerPlazaBella: Marker? = mMap.addMarker(
            MarkerOptions()
                .position(PlazaBella)
                .title("Plaza Bella")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.cart_48))
        )
        markerPlazaBella?.tag = "PlazaBella"
        val PlazaOaxaca = LatLng(17.04308424082672, -96.71534942510232)
        val markerPlazaOaxaca: Marker? = mMap.addMarker(
            MarkerOptions()
                .position(PlazaOaxaca)
                .title("Plaza Oaxaca")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.cart_48))
        )
        markerPlazaOaxaca?.tag = "PlazaOaxaca"
        val PlazaMazari = LatLng(17.078262655320255, -96.71929647693146)
        val markerPlazaMazari: Marker? = mMap.addMarker(
            MarkerOptions()
                .position(PlazaMazari)
                .title("Plaza Mazari")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.cart_48))
        )
        markerPlazaMazari?.tag = "PlazaMazari"
        if (markerMacroPlaza != null) {
            markers.add(markerMacroPlaza)
        }
        if (markerPlazaDelValle != null) {
            markers.add(markerPlazaDelValle)
        }
        if (markerPlazaBella != null) {
            markers.add(markerPlazaBella)
        }
        if (markerPlazaOaxaca != null) {
            markers.add(markerPlazaOaxaca)
        }
        if (markerPlazaMazari != null) {
            markers.add(markerPlazaMazari)
        }
    }

    private fun showHospitalesMarkers() {
        val CRIT = LatLng(16.996109486545016, -96.75557262929814)
        val markerCRIT: Marker? = mMap.addMarker(
            MarkerOptions()
                .position(CRIT)
                .title("CRIT")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.add_48))
        )
        markerCRIT?.tag = "CRIT"
        val ISSSTE = LatLng(17.084208648897775, -96.72287984610173)
        val markerISSSTE: Marker? = mMap.addMarker(
            MarkerOptions()
                .position(ISSSTE)
                .title("ISSSTE")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.add_48))
        )
        markerISSSTE?.tag = "ISSSTE"
        val HGralAurelio = LatLng(17.08205263432568, -96.71851352405474)
        val markerHGralAurelio: Marker? = mMap.addMarker(
            MarkerOptions()
                .position(HGralAurelio)
                .title("Hospital General Dr. Aurelio Valdivieso")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.add_48))
        )
        markerHGralAurelio?.tag = "HGralAurelio"
        val HSanLucas = LatLng(17.07541935456676, -96.71821708946949)
        val markerHSanLucas: Marker? = mMap.addMarker(
            MarkerOptions()
                .position(HSanLucas)
                .title("Hospital San Lucas")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.add_48))
        )
        markerHSanLucas?.tag = "HSanLucas"
        val HDValle = LatLng(17.07426994472622, -96.71496079535737)
        val markerHDValle: Marker? = mMap.addMarker(
            MarkerOptions()
                .position(HDValle)
                .title("Hospital del Valle")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.add_48))
        )
        markerHDValle?.tag = "HDValle"
        val HReforma = LatLng(17.06803045120984, -96.72167728221555)
        val markerHReforma: Marker? = mMap.addMarker(
            MarkerOptions()
                .position(HReforma)
                .title("Hospital Reforma")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.add_48))
        )
        markerHReforma?.tag = "HReforma"
        val IMSS = LatLng(17.072108416831444, -96.72126238403851)
        val markerIMSS: Marker? = mMap.addMarker(
            MarkerOptions()
                .position(IMSS)
                .title("IMSS Hospital General de Zona 1")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.add_48))
        )
        markerIMSS?.tag = "IMSS"
        if (markerCRIT != null) {
            markers.add(markerCRIT)
        }
        if (markerISSSTE != null) {
            markers.add(markerISSSTE)
        }
        if (markerHGralAurelio != null) {
            markers.add(markerHGralAurelio)
        }
        if (markerHSanLucas != null) {
            markers.add(markerHSanLucas)
        }
        if (markerHDValle != null) {
            markers.add(markerHDValle)
        }
        if (markerHReforma != null) {
            markers.add(markerHReforma)
        }
        if (markerIMSS != null) {
            markers.add(markerIMSS)
        }
    }

    private fun showMercadosMarkers() {
        val MercadoBJ = LatLng(17.05961293749345, -96.72667773499269)
        val markerMercadoBJ: Marker? = mMap.addMarker(
            MarkerOptions()
                .position(MercadoBJ)
                .title("Mercado Benito Juárez")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.shop_48))
        )
        markerMercadoBJ?.tag = "MercadoBJ"
        val Mercado20N = LatLng(17.05854515379785, -96.72727561311778)
        val markerMercado20N: Marker? = mMap.addMarker(
            MarkerOptions()
                .position(Mercado20N)
                .title("Mercado 20 de Noviembre")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.shop_48))
        )
        markerMercado20N?.tag = "Mercado20N"
        val Mercado21M = LatLng(17.055407267539508, -96.68270064460347)
        val markerMercado21M: Marker? = mMap.addMarker(
            MarkerOptions()
                .position(Mercado21M)
                .title("Mercado 21 de Marzo")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.shop_48))
        )
        markerMercado21M?.tag = "Mercado21M"
        val MercadoZonalSR = LatLng(17.093997190921428, -96.74771968324255)
        val markerMercadoZonalSR: Marker? = mMap.addMarker(
            MarkerOptions()
                .position(MercadoZonalSR)
                .title("Mercado Zonal Santa Rosa")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.shop_48))
        )
        markerMercadoZonalSR?.tag = "MercadoZonalSR"
        val Mercado5S = LatLng(17.053737670970374, -96.70962613477542)
        val markerMercado5S: Marker? = mMap.addMarker(
            MarkerOptions()
                .position(Mercado5S)
                .title("Mercado 5 Señores")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.shop_48))
        )
        markerMercado5S?.tag = "Mercado5S"
        if (markerMercadoBJ != null) {
            markers.add(markerMercadoBJ)
        }
        if (markerMercado20N != null) {
            markers.add(markerMercado20N)
        }
        if (markerMercado21M != null) {
            markers.add(markerMercado21M)
        }
        if (markerMercadoZonalSR != null) {
            markers.add(markerMercadoZonalSR)
        }
        if (markerMercado5S != null) {
            markers.add(markerMercado5S)
        }
    }

    private fun showTourismMarkers() {
        val Zocalo = LatLng(17.060883894880014, -96.72532611394887)
        val markerZocalo: Marker? = mMap.addMarker(
            MarkerOptions()
                .position(Zocalo)
                .title("Zócalo")
                .snippet("Ruta")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.camera_48))
        )
        markerZocalo?.tag = "Zocalo"
        val Tule = LatLng(17.046622093083524, -96.63621469255428)
        val markerTule: Marker? = mMap.addMarker(
            MarkerOptions()
                .position(Tule)
                .title("El Tule")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.camera_48))
        )
        markerTule?.tag = "Tule"
        val MonteAlban = LatLng(17.045480083068096, -96.76746884061338)
        val markerMonteAlban: Marker? = mMap.addMarker(
            MarkerOptions()
                .position(MonteAlban)
                .title("Monte Albán")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.camera_48))
        )
        markerMonteAlban?.tag = "MonteAlban"
        if (markerZocalo != null) {
            markers.add(markerZocalo)
        }
        if (markerTule != null) {
            markers.add(markerTule)
        }
        if (markerMonteAlban != null) {
            markers.add(markerMonteAlban)
        }
    }

    private fun showOthersMarkers() {
        val IEEPO = LatLng(17.069119098740533, -96.69616199440958)
        val markerIEEPO: Marker? = mMap.addMarker(
            MarkerOptions()
                .position(IEEPO)
                .title("IEEPO")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marcador_t32))
        )
        markerIEEPO?.tag = "IEEPO"
        val Baraimas = LatLng(17.066212678335933, -96.68030570874876)
        val markerBaraimas: Marker? = mMap.addMarker(
            MarkerOptions()
                .position(Baraimas)
                .title("Baraimas")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.heart_48))
        )
        markerBaraimas?.tag = "Baraimas"
        if (markerIEEPO != null) {
            markers.add(markerIEEPO)
        }
        if (markerBaraimas != null) {
            markers.add(markerBaraimas)
        }
    }

    private fun getRandomColor(): Int {
        val rnd = java.util.Random()
        return Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))
    }

    private val POLYLINE_STROKE_WIDTH_PX = 12
    private var ruta = ""

    /**
     * Styles the polyline, based on type.
     * @param polyline The polyline object that needs styling.
     */
    private fun stylePolyline(polyline: Polyline) {
        var type = ""
        var color = 0
        // Get the data object stored with the polyline.
        if (polyline.tag != null) {
            type = polyline.tag.toString()
            polyline.startCap = RoundCap()
            color = getRandomColor()
        } else {
            Toast.makeText(this, getString(R.string.unexpected_error), Toast.LENGTH_LONG).show()
        }

        /*switch (type) {
            // If no type is given, allow the API to use the default.
            case "R01":
            case "R02":
            case "R03":
            case "R10":
            case "R27":
            case "R29":
            case "R47":
            case "R62":
            case "R71":
                // Use a round cap at the start of the line.
                polyline.setStartCap(new RoundCap());
                color = getRandomColor();
                break;
            default:
                Toast.makeText(this, getString(R.string.unexpected_error), Toast.LENGTH_LONG).show();
                break;*
        }*/polyline.endCap = RoundCap()
        polyline.width = POLYLINE_STROKE_WIDTH_PX.toFloat()
        polyline.color = color
        polyline.jointType = JointType.ROUND
    }

    private val PATTERN_GAP_LENGTH_PX = 20
    private val DOT: PatternItem = Dot()
    private val GAP: PatternItem = Gap(PATTERN_GAP_LENGTH_PX.toFloat())

    // Create a stroke pattern of a gap followed by a dot.
    private val PATTERN_POLYLINE_DOTTED = Arrays.asList(GAP, DOT)

    /**
     * Listens for clicks on a polyline.
     * @param polyline The polyline object that the user has clicked.
     */
    fun onPolylineClick(polyline: Polyline) {
        // Flip from solid stroke to dotted stroke pattern.
        if (polyline.pattern == null || !polyline.pattern!!.contains(DOT)) {
            polyline.pattern = PATTERN_POLYLINE_DOTTED
        } else {
            // The default pattern is a solid stroke.
            polyline.pattern = null
        }
        val polylineTag = polyline.tag.toString()
        if (polyline.tag.toString() == "R01") {
            ruta = nombreRuta01
        } else if (polylineTag == "R02") {
            ruta = nombreRuta02
        } else if (polylineTag == "R03") {
            ruta = nombreRuta03
        } else if (polylineTag == "R10") {
            ruta = nombreRuta10
        } else if (polylineTag == "R27") {
            ruta = nombreRuta27
        } else if (polylineTag == "R29") {
            ruta = nombreRuta29
        } else if (polylineTag == "R47") {
            ruta = nombreRuta47
        } else if (polylineTag == "R62") {
            ruta = nombreRuta62
        } else if (polylineTag == "R71") {
            ruta = nombreRuta71
        }
        Toast.makeText(this, polyline.tag.toString() + ": " + ruta, Toast.LENGTH_LONG).show()
    }

    private fun showRuta01() {
        var polyline1 = mMap.addPolyline(
            PolylineOptions()
                .clickable(true)
                .addAll(ruta01)
        )
        polyline1.setTag("R01")
        stylePolyline(polyline1)
        polylines.add(polyline1)
    }

    private fun showRuta02() {
        var polyline2 = mMap.addPolyline(
            PolylineOptions()
                .clickable(true)
                .addAll(ruta02)
        )
        polyline2.setTag("R02")
        stylePolyline(polyline2)
        polylines.add(polyline2)
    }

    private fun showRuta03() {
        var polyline3 = mMap.addPolyline(
            PolylineOptions()
                .clickable(true)
                .addAll(ruta03)
        )
        polyline3.setTag("R03")
        stylePolyline(polyline3)
        polylines.add(polyline3)
    }

    private fun showRuta10() {
        var polyline10 = mMap.addPolyline(
            PolylineOptions()
                .clickable(true)
                .addAll(ruta10)
        )
        polyline10.setTag("R10")
        stylePolyline(polyline10)
        polylines.add(polyline10)
    }

    private fun showRuta27() {
        val polyline27 = mMap.addPolyline(
            PolylineOptions()
                .clickable(true)
                .addAll(ruta27)
        )
        polyline27.setTag("R27")
        stylePolyline(polyline27)
        polylines.add(polyline27)
    }

    private fun showRuta29() {
        val polyline29 = mMap.addPolyline(
            PolylineOptions()
                .clickable(true)
                .addAll(ruta29)
        )
        polyline29.setTag("R29")
        stylePolyline(polyline29)
        polylines.add(polyline29)
    }

    private fun showRuta47() {
        val polyline47: Polyline = mMap.addPolyline(
            PolylineOptions()
                .clickable(true)
                .addAll(ruta47)
        )
        polyline47.tag = "R47"
        stylePolyline(polyline47)
        polylines.add(polyline47)
    }

    private fun showRuta62() {
        var polyline62 = mMap.addPolyline(
            PolylineOptions()
                .clickable(true)
                .addAll(ruta62)
        )
        polyline62.setTag("R62")
        stylePolyline(polyline62)
        polylines.add(polyline62)
    }

    private fun showRuta71() {
        var polyline71 = mMap.addPolyline(
            PolylineOptions()
                .clickable(true)
                .addAll(ruta71)
        )
        polyline71.setTag("R71")
        stylePolyline(polyline71)
        polylines.add(polyline71)
    }

    var handler: Handler? = null
    var runnable: Runnable? = null

    @Synchronized
    fun animateMarker(
        pts: List<LatLng>,
        hideMarker: Boolean,
        ruta: Int,
        camiones: Int,
        nombre: String
    ) {
        // Simple check to make sure there are enough points in the list.
        if (pts.size <= 1) {
            // need at least two points.
            return
        }

        //final Handler handler = new Handler();
        handler = Handler()

        // Use first point in list as start.
        val m: Marker? = mMap.addMarker(
            MarkerOptions()
                .position(pts[0])
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.autobus_48))
                .title("Ruta: $ruta")
                .snippet(
                    nombre + "\n" +
                            "Número de camiones: " + camiones
                )
        )
        val proj: Projection = mMap.getProjection()
        //Point startPoint = proj.toScreenLocation(m.getPosition());
        //final LatLng startLatLng = proj.fromScreenLocation(startPoint);
        val duration: Long = 1500
        val interpolator: Interpolator = LinearInterpolator()
        handler!!.post(object : Runnable {
            // start at first segment
            private var segment = 0

            // initial start time
            var start = SystemClock.uptimeMillis()
            override fun run() {
                val elapsed = SystemClock.uptimeMillis() - start
                val t = interpolator.getInterpolation((elapsed.toFloat() / duration))
                // Use next point in list as destination
                val lng = t * pts[segment + 1].longitude + (1 - t) * pts[segment].longitude
                val lat = t * pts[segment + 1].latitude + (1 - t) * pts[segment].latitude
                m?.position = LatLng(lat, lng)
                if (t < 1.0) {
                    // Post again 16ms later.
                    handler!!.postDelayed(this, 16)
                } else if (segment < (pts.size - 2)) {
                    // move to next segment
                    segment++
                    start = SystemClock.uptimeMillis()
                    handler!!.postDelayed(this, 16)
                } else {
                    m?.isVisible = !hideMarker
                }
            }
        }.also { runnable = it })
        if (m != null) {
            markers_animations.add(m)
        }
    }

    var handler2: Handler? = null
    var runnable2: Runnable? = null
    private fun controlAnimations(
        milisegundos: Int,
        pts: List<LatLng>,
        hideMarker: Boolean,
        ruta: Int,
        camiones: Int,
        nombre: String
    ) {
        handler2 = Handler()
        handler2!!.postDelayed(object : Runnable {
            override fun run() {
                animateMarker(pts, hideMarker, ruta, camiones, nombre)
                handler2!!.postDelayed(this, milisegundos.toLong())
            }
        }.also { runnable2 = it }, milisegundos.toLong())
    }

    private fun deleteMarkersAnimations() {
        for (mark in markers_animations) {
            mark.remove()
        }
        markers_animations.clear()
        if (clicks > 0) {
            handler2?.removeCallbacksAndMessages(null)
        }
    }

    // Called when the user clicks a marker.
    override fun onMarkerClick(marker: Marker): Boolean {
        var tagName = ""

        // Retrieve the data from the marker it's not null.

        // Retrieve the data from the marker it's not null.
        if (marker.tag != null) {
            tagName = marker.tag.toString()
        }

        when (tagName) {
            "MacroPlaza", "IEEPO", "Baraimas" -> {
                deletePolylines()
                deleteMarkersAnimations()
                showRuta62()
                animateMarker(ruta62, true, 62, 30, nombreRuta62)
                controlAnimations(10000, ruta62, true, 62, 20, nombreRuta62)
                clicks++
            }
            "PlazaDelValle", "PlazaOaxaca", "UABJO" -> {
                deletePolylines()
                deleteMarkersAnimations()
                showRuta29()
                animateMarker(ruta29, true, 29, 30, nombreRuta29)
                controlAnimations(10000, ruta29, true, 29, 20, nombreRuta29)
                clicks++
            }
            "PlazaBella", "Mercado5S" -> {
                deletePolylines()
                deleteMarkersAnimations()
                showRuta71()
                animateMarker(ruta71, true, 71, 30, nombreRuta71)
                controlAnimations(10000, ruta71, true, 71, 20, nombreRuta71)
                clicks++
            }
            "PlazaMazari" -> {
                deletePolylines()
                deleteMarkersAnimations()
                showRuta10()
                animateMarker(ruta10, true, 10, 20, nombreRuta10)
                controlAnimations(10000, ruta10, true, 10, 20, nombreRuta10)
                clicks++
            }
            "MercadoBJ", "Mercado20N" -> {
                deletePolylines()
                deleteMarkersAnimations()
                showRuta03()
                animateMarker(ruta03, true, 3, 20, nombreRuta03)
                controlAnimations(10000, ruta03, true, 3, 20, nombreRuta03)
                clicks++
            }
            "Zocalo" -> {
                deletePolylines()
                deleteMarkersAnimations()
                showRuta02()
                //animateMarker(ruta02, true, db.getIdRuta(2), db.getCamiones(2), db.getNombreRuta(2));
                //controlAnimations(5000, ruta02, true, db.getIdRuta(2), db.getCamiones(2), db.getNombreRuta(2));
                animateMarker(ruta02, true, 2, 20, nombreRuta02)
                controlAnimations(10000, ruta02, true, 2, 20, nombreRuta02)
                clicks++
            }
            "ITO", "MercadoZonalSR" -> {
                deletePolylines()
                deleteMarkersAnimations()
                showRuta01()
                showRuta02()
                showRuta29()
                showRuta62()
                animateMarker(ruta01, true, 1, 20, nombreRuta01)
                animateMarker(ruta02, true, 2, 20, nombreRuta02)
                animateMarker(ruta29, true, 29, 20, nombreRuta29)
                animateMarker(ruta62, true, 62, 20, nombreRuta62)
                controlAnimations(9500, ruta01, true, 1, 20, nombreRuta01)
                controlAnimations(12000, ruta02, true, 2, 20, nombreRuta02)
                controlAnimations(11500, ruta29, true, 29, 20, nombreRuta29)
                controlAnimations(10000, ruta62, true, 62, 20, nombreRuta62)
                clicks++
            }
            "ISSSTE", "UABJOMedicina", "HGralAurelio", "IMSS" -> {
                deletePolylines()
                deleteMarkersAnimations()
                showRuta03()
                showRuta10()
                animateMarker(ruta03, true, 3, 20, nombreRuta03)
                animateMarker(ruta10, true, 10, 20, nombreRuta10)
                controlAnimations(7500, ruta03, true, 3, 20, nombreRuta03)
                controlAnimations(7500, ruta10, true, 10, 20, nombreRuta10)
                clicks++
            }
            "Anahuac", "CRIT" -> {
                deletePolylines()
                deleteMarkersAnimations()
                showRuta27()
                showRuta03()
                animateMarker(ruta27, true, 27, 20, nombreRuta27)
                animateMarker(ruta03, true, 3, 20, nombreRuta03)
                controlAnimations(7000, ruta27, true, 27, 20, nombreRuta27)
                controlAnimations(7500, ruta03, true, 3, 20, nombreRuta03)
                clicks++
            }
            "Mercado21M", "MonteAlban" -> {
                deletePolylines()
                deleteMarkersAnimations()
                // ruta 47
                defineRoute47()
                val r47 = getDirectionURL(initPoint, endPoint, key)
                GetDirection(r47).execute()
                animateMarker(route, true, 47, 20, nombreRuta47)
                controlAnimations(7500, route, true, 47, 20, nombreRuta47)
                clicks++
                /*deletePolylines()
                deleteMarkersAnimations()
                showRuta47()
                animateMarker(ruta47, true, 47, 20, nombreRuta47)
                controlAnimations(7500, ruta47, true, 47, 20, nombreRuta47)
                clicks++*/
            }
            "Tule", "URSE" -> {
                deletePolylines()
                deleteMarkersAnimations()
                Toast.makeText(this, getString(R.string.not_available_yet), Toast.LENGTH_SHORT).show()
                clicks++
            }
            else -> {}
        }

        // Return false to indicate that we have not consumed the event and that we wish
        // for the default behavior to occur (which is for the camera to move such that the
        // marker is centered and for the marker's info window to open, if it has one).
        return false
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
                val locationResult = fusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(
                    this
                ) { task ->
                    if (task.isSuccessful) {
                        // Set the map's camera position to the current location of the device.
                        lastKnownLocation = task.result
                        if (lastKnownLocation != null) {
                            mMap.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(lastKnownLocation!!.latitude, lastKnownLocation!!.longitude),
                                    DEFAULT_ZOOM.toFloat()
                                )
                            )
                        }
                    } else {
                        Log.d(TAG,"Current location is null. Using defaults.")
                        Log.e(TAG,"Exception: %s", task.exception)
                        mMap.moveCamera(
                            CameraUpdateFactory
                                .newLatLngZoom(
                                    defaultLocation,
                                    DEFAULT_ZOOM.toFloat()
                                )
                        )
                        mMap.getUiSettings().setMyLocationButtonEnabled(false)
                    }
                }
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

        val polyline = mMap.addPolyline(
            PolylineOptions()
                .clickable(true)
                .addAll(poly)
        )
        polyline.setTag("POLY")
        stylePolyline(polyline)
        polylines.add(polyline)
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
                lineoption.clickable(true)
            }
            mMap.addPolyline(lineoption)
        }
    }

    private fun deletePolylines() {
        for (line in polylines) {
            line.remove()
        }
        polylines.clear()
    }

    private fun deleteMarkers() {
        for (mark in markers) {
            mark.remove()
        }
        markers.clear()
    }

    private fun deleteAll() {
        deletePolylines()
        deleteMarkers()
        deleteMarkersAnimations()
    }

    fun defineRoute47() {
        initPoint = LatLng(17.056536913872083, -96.75828620870179)
        endPoint = LatLng(17.05549651768387, -96.68130361631837)
        route.add(initPoint)
        route.add(endPoint)
    }

}


// ------------------------------------------------------------------------------------------- //
    // ------------------------------------------------------------------------------------------- //
    /* FIRST ATTEMPT
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
                lineoption.clickable(true)
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
 */

