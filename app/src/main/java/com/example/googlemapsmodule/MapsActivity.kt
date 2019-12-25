package com.example.googlemapsmodule

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import java.util.*


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnPolylineClickListener,
    GoogleMap.OnPolygonClickListener {

    private lateinit var mMap: GoogleMap
    private var mLocationPermissionGranted: Boolean = false
    private val LOCATION_REQUEST_CODE = 100
    private val GOOGLE_PLAY_SERVICES_ERROR_CODE = 101
    private lateinit var mapFragment: SupportMapFragment

    private val POLYLINE_STROKE_WIDTH_PX = 12f
    private val COLOR_BLACK_ARGB = -0x1000000
    private val COLOR_WHITE_ARGB = -0x1
    private val COLOR_GREEN_ARGB = -0xc771c4
    private val COLOR_PURPLE_ARGB = -0x7e387c
    private val COLOR_ORANGE_ARGB = -0xa80e9
    private val COLOR_BLUE_ARGB = -0x657db

    private val POLYGON_STROKE_WIDTH_PX = 8
    private val PATTERN_DASH_LENGTH_PX = 20
    private val PATTERN_GAP_LENGTH_PX = 12
    private val DOT: PatternItem = Dot()
    private val DASH: PatternItem = Dash(PATTERN_DASH_LENGTH_PX.toFloat())
    private val GAP: PatternItem = Gap(PATTERN_GAP_LENGTH_PX.toFloat())

    private val transparentBlue = 0x300000FF

    // Create a stroke pattern of a gap followed by a dash.
    private val PATTERN_POLYGON_ALPHA: List<PatternItem> = Arrays.asList(GAP, DOT)

    // Create a stroke pattern of a dot followed by a gap, a dash, and another gap.
    private val PATTERN_POLYGON_BETA: List<PatternItem> = Arrays.asList(DOT, GAP, DASH, GAP)

    private val peeragarhiLatLng = LatLng(28.655219, 77.189181)
    private val peeragarhiMarker = MarkerOptions().position(peeragarhiLatLng).title("Peeragrahi")

    private val BihariJuiceCornerLatLng by lazy { LatLng(28.741727, 77.196156) } //bottom-left fisrt
    private val BurariLatLng by lazy { LatLng(28.748867, 77.200262) } // top-right secound

    private lateinit  var mFusedLocationProviderClient: FusedLocationProviderClient

    private lateinit var goTolocEt: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)


        // mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)


        initGoogleMap()


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.


//        //adding fragment through code
//        var supportMapFragment = SupportMapFragment.newInstance()
//        supportFragmentManager.beginTransaction().add(R.id.map,supportMapFragment).commit()
//        supportMapFragment.getMapAsync(this)


        var goToloc = findViewById<Button>(R.id.btn_go_to_location)
        goToloc.setOnClickListener({

            var bottomBoundry = 28.741727
            var leftBoundry = 77.196156

            var topBoundry = 28.748867
            var rightBoundry = 77.200262


            var SANT_NAGAR_BOUNDS = LatLngBounds(BihariJuiceCornerLatLng, BurariLatLng)
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(SANT_NAGAR_BOUNDS, 1))
            mMap.addMarker(MarkerOptions().position(SANT_NAGAR_BOUNDS.center))

        })


        findViewById<Button>(R.id.btn_go_loc).setOnClickListener({

            geoLocate()

        })

        goTolocEt = findViewById(R.id.et_search)


    }

    private fun isGPSEnabled(): Boolean {

        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val providerEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

        if (providerEnabled) {
            return true
        } else {

            var alertDialog = AlertDialog.Builder(this@MapsActivity)
            alertDialog.setTitle("GPS Permission required")
            alertDialog.setMessage("GPS permission required for the module to operate")
            alertDialog.setCancelable(false)
            alertDialog.setPositiveButton("yes", object : DialogInterface.OnClickListener {
                override fun onClick(p0: DialogInterface?, p1: Int) {

                    var intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivityForResult(intent, 1001)


                }
            })
            alertDialog.create().show()
        }

        return false;

    }

    private fun geoLocate() {

        val locationName = goTolocEt.text.toString()
        val geoCoder = Geocoder(this@MapsActivity, Locale.getDefault())
        val addrlist = geoCoder.getFromLocation(28.748867, 77.200262, 5)

        // val addrlist = geoCoder.getFromLocationName(locationName,1)

        if (addrlist.size > 0) {

            val address: Address = addrlist.get(0)
            mMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        address.latitude,
                        address.longitude
                    ), 18.0f
                )
            );
            mMap.addMarker(
                MarkerOptions().position(
                    LatLng(
                        address.latitude,
                        address.longitude
                    )
                ).title(address.locality)
            )

        }

        for (add in addrlist) {

            Log.e("Address", add.getAddressLine(add.maxAddressLineIndex))

        }


    }

    private fun initGoogleMap() {

        if (isPlayServicesOk()) {

            if (isGPSEnabled()) {

                requestAllPermission()

                //method when fragment is hardcoded in fragment in xml
                mapFragment =
                    supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
                mapFragment.getMapAsync(this)
            }

        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {

        mMap = googleMap
        mMap.isMyLocationEnabled = true
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(peeragarhiLatLng, 18.0f));
        mMap.addMarker(peeragarhiMarker)


//        // Turn on the My Location layer and the related control on the map.
//        updateLocationUI()
//
//        // Get the current location of the device and set the position of the map.
//        getDeviceLocation()
//
//        addCircle()
//
//        googleMap.setMapStyle(
//            MapStyleOptions.loadRawResourceStyle(
//                this, R.raw.map_style))


//        val polyline1 = mMap.addPolyline(
//            PolylineOptions()
//                .clickable(true)
//                .add(
//                    LatLng(-35.016, 143.321),
//                    LatLng(-34.747, 145.592),
//                    LatLng(-34.364, 147.891),
//                    LatLng(-33.501, 150.217),
//                    LatLng(-32.306, 149.248),
//                    LatLng(-32.491, 147.309)
//                )
//        )
//
//        stylePolyline(polyline1)
//
//
//        val polygon1 = googleMap.addPolygon(
//            PolygonOptions()
//                .clickable(true)
//                .add(
//                    LatLng(-27.457, 153.040),
//                    LatLng(-33.852, 151.211),
//                    LatLng(-37.813, 144.962),
//                    LatLng(-34.928, 138.599)
//                ) .fillColor(0x33000000)
//                .strokeColor(0XFF259DC6.toInt())
//                .strokePattern(PATTERN_POLYGON_ALPHA)
//                //.strokeWidth(POLYGON_STROKE_WIDTH_PX.toFloat())
//
//        )
//
//// Store a data object with the polygon, used here to indicate an arbitrary type.
//        // Store a data object with the polygon, used here to indicate an arbitrary type.
//        polygon1.tag = "alpha"
//
//
//
//       // stylePolygon(polygon1)
//
////        var polylineOptions = PolylineOptions().clickable(true).add( LatLng(-35.016, 143.321),
////         LatLng(-34.747, 145.592),
////         LatLng(-34.364, 147.891),
////         LatLng(-33.501, 150.217),
////         LatLng(-32.306, 149.248),
////         LatLng(-32.491, 147.309))
//
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(-23.684, 133.903), 4f))
//
//        // Set listeners for click events.
//        mMap.setOnPolylineClickListener(this);
//        mMap.setOnPolygonClickListener(this);
//
//         polyline1.tag = "A"
//        // Store a data object with the polyline, used here to indicate an arbitrary type.
////        val sydney = LatLng(-34.0, 151.0)
////        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
////        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

    private fun getDeviceLocation() {

//        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
          mFusedLocationProviderClient = FusedLocationProviderClient(this)
          mFusedLocationProviderClient.lastLocation.addOnCompleteListener { task ->

              if(task.isSuccessful){

                  var location = task.result
                  getLocation(LatLng(location?.latitude!!,location?.longitude),"Current Location")


              }

          }

    }

    private fun getLocation(latLng: LatLng,name : String){

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18.0f));
        mMap.addMarker(MarkerOptions().position(latLng).title(name))

    }

    private fun updateLocationUI() {

    }

    private fun addCircle() {

        val circle = CircleOptions()
            .center(peeragarhiLatLng)
            .radius(30.0)
            .strokeColor(0XFF259DC6.toInt())
            .fillColor(0x33000000)
            .clickable(true)
            .strokePattern(PATTERN_POLYGON_ALPHA)
            .strokeWidth(POLYGON_STROKE_WIDTH_PX.toFloat())
        mMap.addCircle(circle)
    }

    private fun stylePolyline(polyline: Polyline) {
        var type = ""
        // Get the data object stored with the polyline.
        if (polyline.tag != null) {
            type = polyline.tag.toString()
        }
        when (type) {
            "A" ->  // Use a custom bitmap as the cap at the start of the line.
                polyline.startCap = CustomCap(
                    BitmapDescriptorFactory.fromResource(R.drawable.ic_arrow), 10f
                )
            "B" ->  // Use a round cap at the start of the line.
                polyline.startCap = RoundCap()
        }
        polyline.endCap = RoundCap()
        polyline.width = POLYLINE_STROKE_WIDTH_PX
        polyline.color = COLOR_BLACK_ARGB
        polyline.jointType = JointType.ROUND
    }

    private fun stylePolygon(polygon: Polygon) {
        var type = ""
        // Get the data object stored with the polygon.
        if (polygon.tag != null) {
            type = polygon.tag.toString()
        }
        var pattern: List<PatternItem?>? = null
        var strokeColor = COLOR_BLACK_ARGB
        var fillColor = COLOR_WHITE_ARGB
        when (type) {
            "alpha" -> {
                // Apply a stroke pattern to render a dashed line, and define colors.
                pattern = PATTERN_POLYGON_ALPHA
                strokeColor = COLOR_GREEN_ARGB
                fillColor = COLOR_ORANGE_ARGB
            }
            "beta" -> {
                // Apply a stroke pattern to render a line of dots and dashes, and define colors.
                pattern = PATTERN_POLYGON_BETA
                strokeColor = COLOR_ORANGE_ARGB
                fillColor = COLOR_BLUE_ARGB
            }
        }
        polygon.strokePattern = pattern
        polygon.strokeWidth = POLYGON_STROKE_WIDTH_PX.toFloat()
        polygon.strokeColor = strokeColor
        polygon.fillColor = fillColor
    }



    private fun requestAllPermission() {

        if (checkLocationPermission()) {

            Toast.makeText(this, "Location permission granted", Toast.LENGTH_LONG).show()

        } else {

            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                    requestPermissions(
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        LOCATION_REQUEST_CODE
                    )
                }
            }
        }

    }

    private fun checkLocationPermission(): Boolean {

        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun isPlayServicesOk(): Boolean {

        val googleApi = GoogleApiAvailability.getInstance()
        val result = googleApi.isGooglePlayServicesAvailable(this)
        if (result == ConnectionResult.SUCCESS) {

            return true

        } else if (googleApi.isUserResolvableError(result)) {

            googleApi.getErrorDialog(this, result, GOOGLE_PLAY_SERVICES_ERROR_CODE, object :
                DialogInterface.OnCancelListener {
                override fun onCancel(p0: DialogInterface?) {

                    Toast.makeText(
                        this@MapsActivity,
                        "Dialog is cancelled by user",
                        Toast.LENGTH_LONG
                    ).show()

                }
            }).show()
        } else {

            Toast.makeText(
                this@MapsActivity,
                "Play services are required for this app",
                Toast.LENGTH_LONG
            ).show()
        }

        return false;
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            mLocationPermissionGranted = true

            Toast.makeText(this, "Location permission granted", Toast.LENGTH_LONG).show()
        } else {

            Toast.makeText(this, "Location permission not granted", Toast.LENGTH_LONG).show()
        }

    }

    override fun onPolylineClick(p0: Polyline?) {


    }

    override fun onPolygonClick(p0: Polygon?) {


    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.main_menu, menu)
        return true

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        var id = item.itemId

        when (id) {

            R.id.map_default -> {

                mMap.mapType = GoogleMap.MAP_TYPE_NONE
            }

            R.id.map_normal -> {

                mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
            }
            R.id.map_satellite -> {

                mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
            }


            R.id.map_terrain -> {

                mMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
            }

            R.id.map_hybrid -> {

                mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
            }

            R.id.map_current_loc -> {

                getDeviceLocation()
            }

        }


        return super.onOptionsItemSelected(item)


    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1001) {

            val locationManager: LocationManager =
                getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val providerEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

            if (providerEnabled) {

                Toast.makeText(this@MapsActivity, "Gps is enabled", Toast.LENGTH_LONG).show()
            } else {

                Toast.makeText(this@MapsActivity, "Gps is disabled", Toast.LENGTH_LONG).show()
            }

        }

    }


    //if dnt use fragment only for actvity
//    override fun onStart() {
//        super.onStart()
//        mapFragment.onStart()
//
//    }
//
//    override fun onResume() {
//        super.onResume()
//        mapFragment.onResume()
//    }
//
//    override fun onPause() {
//        super.onPause()
//        mapFragment.onPause()
//    }
//
//    override fun onStop() {
//        super.onStop()
//        mapFragment.onStop()
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        mapFragment.onDestroy()
//    }
//
//    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
//        super.onSaveInstanceState(outState, outPersistentState)
//        mapFragment.onSaveInstanceState(outState)
//    }
//
//    override fun onLowMemory() {
//        super.onLowMemory()
//        mapFragment.onLowMemory()
//    }

}
