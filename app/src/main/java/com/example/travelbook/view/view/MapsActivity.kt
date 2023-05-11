package com.example.travelbook.view.view

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.room.Room
import com.example.travelbook.BuildConfig
import com.example.travelbook.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.travelbook.databinding.ActivityMapsBinding
import com.example.travelbook.view.room_database.DAO
import com.example.travelbook.view.room_database.PlaceModel
import com.example.travelbook.view.room_database.TravelBookDataBase
import com.google.android.material.snackbar.Snackbar
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMapLongClickListener {
    //you should initialize your api key in the local.properties like "API_KEY=xxxx"
    val apiKey = BuildConfig.API_KEY

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private lateinit var sharedPreferences: SharedPreferences
    private var locationForFirstOpen : Boolean? = null
    private var selectedLatitude : Double? = null
    private var selectedLongitude : Double? = null
    private lateinit var dataBase: TravelBookDataBase
    private lateinit var dao: DAO
    private val compositeDisposable = CompositeDisposable()
    private var placeFromMain : PlaceModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        //saving last location for first open, then we see last location zoom when app is open
        sharedPreferences = this@MapsActivity.getSharedPreferences("com.example.travelbook", MODE_PRIVATE)
        locationForFirstOpen = false

        dataBase = Room.databaseBuilder(applicationContext,TravelBookDataBase::class.java,"TravelBook").build()
        dao = dataBase.TravelBookDao()

        bottomNavBar()

        registerLauncher()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMapLongClickListener(this@MapsActivity)

        val intent = intent
        val info = intent.getStringExtra("info")
        if (info=="new"){
            binding.deleteButton.isVisible = false

            locationManager = this.getSystemService(LOCATION_SERVICE) as LocationManager

            binding.saveButton.setOnClickListener(){
                valuesIsEmpty()
            }

            locationListener = object : LocationListener {
                override fun onLocationChanged(location: Location) {

                    locationForFirstOpen = sharedPreferences.getBoolean("locationForFirstOpen",false)

                    if (locationForFirstOpen == false){
                        val userLastLocation = LatLng(location.latitude, location.longitude)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLastLocation, 15f))
                        sharedPreferences.edit().putBoolean("locationForFirstOpen",true).apply()
                    }

                }

                override fun onProviderDisabled(provider: String) {
                    super.onProviderDisabled(provider)
                    try {
                        //checking provider is enabled or disabled and if provider is disabled we asking the user he want to change this or not
                        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                            val alert = AlertDialog.Builder(this@MapsActivity)
                            alert.setTitle("Travel Book")
                            alert.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                            alert.setPositiveButton("Turn on"){ dialog, which ->
                                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                                startActivity(intent)

                            }
                            alert.setNegativeButton("Stay off"){ dialog, which ->

                            }
                            alert.show()
                        }
                    }catch (e: Exception){
                        e.printStackTrace()
                    }
                }

                override fun onProviderEnabled(provider: String) {
                    super.onProviderEnabled(provider)
                    Toast.makeText(this@MapsActivity, "Location is now available", Toast.LENGTH_SHORT).show()
                }
            }
            //checking permission for location
            requestPermission()
        }else{
            binding.saveButton.visibility = View.GONE

            placeFromMain = intent.getSerializableExtra("selectedPlace") as? PlaceModel

            placeFromMain?.let { place->
                val latLang = LatLng(place.latitude,place.longitude)

                mMap.addMarker(MarkerOptions().position(latLang).title(place.cityName))
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLang,15f))

                binding.cityNameText.setText(place.cityName)
                binding.countryNameText.setText(place.countryName)

                binding.deleteButton.setOnClickListener(){
                    delete()
                }

            }
        }

    }

    private fun registerLauncher() {

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
                if (result) {
                    //permission granted
                    if (ContextCompat.checkSelfPermission(this@MapsActivity, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 2f, locationListener)

                        val lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                        if (lastLocation != null) {
                            val userLastLocation = LatLng(lastLocation.latitude, lastLocation.longitude)
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLastLocation, 15f))
                        }

                        mMap.isMyLocationEnabled = true

                    } else {
                        //permission denied
                        Toast.makeText(this@MapsActivity, "Permission needed", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    private fun requestPermission() {
        if (ContextCompat.checkSelfPermission(this@MapsActivity, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this@MapsActivity, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                Snackbar.make(binding.root, "Permission needed for location updates", Snackbar.LENGTH_INDEFINITE).setAction("Give Permission") {
                    //request permission
                    permissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
                }.show()
            } else {
                //request permission
                permissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
            }
        } else {
            //permission granted
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 2f, locationListener)

            val lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (lastLocation != null) {
                val userLastLocation = LatLng(lastLocation.latitude, lastLocation.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLastLocation, 15f))
            }
            mMap.isMyLocationEnabled = true

        }
    }

    override fun onMapLongClick(latLang: LatLng) {
        mMap.clear()
            mMap.addMarker(MarkerOptions().position(latLang))
        selectedLatitude = latLang.latitude
        selectedLongitude = latLang.longitude
    }

    private fun bottomNavBar(){
        val intent = Intent(this@MapsActivity, MainActivity::class.java)
        binding.bottomNavBar.setOnItemSelectedListener {
            when(it){
                R.id.nav_home -> startActivity(intent)

            }
        }
    }

    private fun valuesIsEmpty(){
        val errorMsg = StringBuilder()

        val city= binding.cityNameText.text.toString().trim()
        val country= binding.countryNameText.text.toString().trim()

        if (TextUtils.isEmpty(city)){
            binding.cityNameText.error = "City name can not be empty"
            errorMsg.append(" ")
        }
        if (TextUtils.isEmpty(country)){
            binding.countryNameText.error = "Country name can not be empty"
            errorMsg.append(" ")
        }
        if (selectedLatitude == null && selectedLongitude == null){
            errorMsg.append("You should also choose an location with long click.\n")
        }
        if (errorMsg.isNotEmpty()) {
            Toast.makeText(this@MapsActivity, errorMsg.toString().trim(), Toast.LENGTH_SHORT).show()
        }else {
            val placeModel = PlaceModel(city,country,selectedLatitude!!,selectedLongitude!!)
            compositeDisposable.add(
                dao.insert(placeModel)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this@MapsActivity::handleResponse)
            )
        }
    }

    private fun handleResponse(){
        Toast.makeText(this@MapsActivity, "Place are saved.\nYou also save new place or back to the home page", Toast.LENGTH_LONG).show()
        binding.cityNameText.text.clear()
        binding.countryNameText.text.clear()
        mMap.clear()
    }

    private fun delete(){
        placeFromMain?.let {
            compositeDisposable.add(
                dao.delete(it)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this@MapsActivity::handleResponseOldPlace)
            )
        }
    }

    private fun handleResponseOldPlace(){
        Toast.makeText(this@MapsActivity, "Place Deleted.", Toast.LENGTH_SHORT).show()
        val intent = Intent(this@MapsActivity, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }
}