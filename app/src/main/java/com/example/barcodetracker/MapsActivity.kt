package com.example.barcodetracker

import android.Manifest
import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.example.barcodetracker.Model.ItemLocation

import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

import com.google.android.gms.tasks.Task
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse
import com.google.android.libraries.places.api.net.PlacesClient
import java.lang.Exception

import kotlin.collections.List

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private val TAG: String = "MapsActivity"
    private lateinit var lstPlaces: ListView
    private lateinit var mPlacesClient: PlacesClient
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient

    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private val mDefaultLocation: LatLng = LatLng(-33.8523341, 151.2106085)
    private val DEFAULT_ZOOM: Float = 20.0F
    private val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: Int = 1
    private var mLocationPermissionGranted: Boolean = false

    // Used for selecting the current place.
    private val M_MAX_ENTRIES: Int = 5
    private var mLikelyPlaceNames = listOf<String?>()
    private var mLikelyPlaceLatLngs = listOf<LatLng?>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        var toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        lstPlaces = findViewById(R.id.listPlaces)

        val apiKey: String = getString(R.string.google_api_key)
        Places.initialize(applicationContext, apiKey)

        mPlacesClient = Places.createClient(this)
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_geolocate -> {
                pickCurrentPlace()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        mLocationPermissionGranted = false
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mLocationPermissionGranted = true
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
        }
    }

    /**
     * Handles the result of the request for location permissions.
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        mLocationPermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    mLocationPermissionGranted = true
                }
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

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))

        // Enable the zoom controls for the map
        mMap.uiSettings.isZoomControlsEnabled

        // Prompt the user for permission.
        getLocationPermission()

        //Look for user location upon starting map
        pickCurrentPlace()
    }

    private fun getCurrentPlaceLikelihoods() {
        // Use fields to define the data types to return.
        var placeFields: List<Place.Field> =
            arrayOf(Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG).asList()

        // Get the likely places - that is, the businesses and other points of interest that
        // are the best match for the device's current location.
        @SuppressWarnings("MissingPermission") val request: FindCurrentPlaceRequest =
            FindCurrentPlaceRequest.builder(placeFields).build()
        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        var placeResponse: Task<FindCurrentPlaceResponse> = mPlacesClient.findCurrentPlace(request)
        placeResponse.addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                var response: FindCurrentPlaceResponse? = task.result
                // Set the count, handling cases where less than 5 entries are returned.
                var count: Int
                if (response!!.placeLikelihoods.size < M_MAX_ENTRIES) {
                    count = response!!.placeLikelihoods.size
                } else {
                    count = M_MAX_ENTRIES
                }

                var i = 0

                for (placeLikelihood in response.placeLikelihoods) {
                    var currPlace = placeLikelihood.place
                    mLikelyPlaceNames += currPlace.name
                    mLikelyPlaceLatLngs += currPlace.latLng

                    var currLatLng: String = if(mLikelyPlaceLatLngs[i] == null) "" else mLikelyPlaceLatLngs[i].toString()

                    Log.i(TAG, String.format("ItemLocation " + currPlace.name + " has likelihood " + placeLikelihood.likelihood + " at " + currLatLng))

                    i++
                    if(i > (count - 1)){
                        break
                    }
                }
                fillPlacesList()
            } else {
                var exception: Exception? = task.exception
                if(exception is ApiException){
                    var apiException: ApiException = exception
                    Log.e(TAG, "ItemLocation not found: " + apiException.statusCode)
                }
            }

        }
    }

    private fun getDeviceLocation(){
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try{
            if(mLocationPermissionGranted) {
                var locationResult: Task<Location> = mFusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(this){task ->
                    if(task.isSuccessful){
                        // The geographical location where the device is currently located. That is, the last-known
                        // location retrieved by the Fused Location Provider.
                        var mLastKnownLocation: Location? = task.result

                        //Set map's camera position to current location of device
                        Log.d(TAG, "Latitude: " + mLastKnownLocation!!.latitude)
                        Log.d(TAG, "Longitude: " + mLastKnownLocation!!.latitude)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            LatLng(mLastKnownLocation!!.latitude,mLastKnownLocation!!.longitude),DEFAULT_ZOOM
                        ))
                        mMap.addMarker(MarkerOptions()
                            .title("Your position")
                            .position(LatLng(mLastKnownLocation.latitude,mLastKnownLocation.longitude))
                        )
                    } else {
                        Log.d(TAG, "Current location is null. Using defaults. ")
                        Log.e(TAG, "Exception: ${task.exception}")
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation,DEFAULT_ZOOM))
                    }

                    getCurrentPlaceLikelihoods()
                }
            }
        } catch (e: SecurityException){
            Log.e(TAG,"Exception: ${e.message}")
        }
    }

    private fun pickCurrentPlace(){
        if(mMap == null){
            return
        }

        if (mLocationPermissionGranted){
            getDeviceLocation()
        } else{
            //The user has not granted permission
            Log.i(TAG, "The user did not grant location permission.")

            //Add a default marker, because the user hasn't selected a place.
            mMap.addMarker(MarkerOptions()
                .title(R.string.default_info_title.toString())
                .position(mDefaultLocation)
                .snippet(R.string.default_info_snippet.toString())
            )

            //Prompt user for permission
            getLocationPermission()
        }
    }

    private var listClickedHandler: AdapterView.OnItemClickListener = AdapterView.OnItemClickListener{ parent, v, position, id ->
        // position will give us the index of which place was selected in the array
        var markerLatLng: LatLng? = mLikelyPlaceLatLngs[position]
        var markerPlaceName: String? = mLikelyPlaceNames[position]

        // Add a marker for the selected place
        mMap.addMarker(MarkerOptions()
            .title(mLikelyPlaceNames[position])
            .position(markerLatLng!!))

        // ItemLocation the map's camera at the location of the marker.
        mMap.moveCamera(CameraUpdateFactory.newLatLng(markerLatLng))

        val builder = AlertDialog.Builder(this)
        builder.setMessage("$markerPlaceName")
        builder.setPositiveButton("OK") { _, _ ->
            Log.i("ABC",markerLatLng.toString())
            var selectedLocation = ItemLocation(markerPlaceName!!,markerLatLng)
            var returnIntent = Intent()
            returnIntent.putExtra("LOCATION",selectedLocation)
            setResult(Activity.RESULT_OK,returnIntent)
            finish()
        }
//
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun fillPlacesList(){
        lstPlaces.adapter = null
        // Set up an ArrayAdapter to convert likely places into TextViews to populate the ListView
        var placesAdapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mLikelyPlaceNames)
        lstPlaces.adapter = placesAdapter
        lstPlaces.onItemClickListener =listClickedHandler
    }
}

