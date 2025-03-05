package com.example.ratemate

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.preference.PreferenceManager
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.io.IOException

class HomeActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private val permissionRequestCode = 101
    private var userMarker: Marker? = null
    private lateinit var btnLiveLocation: Button
    private val client = OkHttpClient()
    private var currentMarkers = mutableListOf<Marker>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Initialize the Toolbar for menu
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Initialize osmdroid configuration
        Configuration.getInstance().load(applicationContext, PreferenceManager.getDefaultSharedPreferences(applicationContext))

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Initialize MapView
        mapView = findViewById(R.id.map)
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)

        btnLiveLocation = findViewById(R.id.btnLiveLocation)

        // Set up location updates
        setupLocationUpdates()

        // Request user location
        if (checkLocationPermission()) {
            startLocationUpdates()
        }

        // Live Location Button functionality
        btnLiveLocation.setOnClickListener {
            fetchLiveLocation()
        }
    }

    private fun fetchLiveLocation() {
        if (checkLocationPermission()) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val userLocation = GeoPoint(it.latitude, it.longitude)
                    updateMap(userLocation)
                }
            }
        }
    }

    private fun setupLocationUpdates() {
        locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 2000 // Update every 2 seconds
            fastestInterval = 1000 // Fastest update interval
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    if (location != null) {
                        updateMap(GeoPoint(location.latitude, location.longitude))
                    }
                }
            }
        }
    }

    private fun checkLocationPermission(): Boolean {
        return if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                permissionRequestCode
            )
            false
        } else {
            true
        }
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, mainLooper)
        }
    }

    private fun updateMap(userLocation: GeoPoint) {
        // Only set the center when the userMarker is null (initialization)
        if (userMarker == null) {
            val mapController: IMapController = mapView.controller
            mapController.setZoom(15.0) // Set a default zoom level only once
            mapController.setCenter(userLocation)

            userMarker = Marker(mapView).apply {
                position = userLocation
                title = "You are here"
                mapView.overlays.add(this)
            }
        } else {
            // Update the marker's position without changing zoom or center
            userMarker?.position = userLocation
            mapView.invalidate()
        }
    }

    private fun fetchNearbyPlaces(userLocation: GeoPoint, placeType: String) {
        val url = "https://overpass-api.de/api/interpreter?data=[out:json];node[amenity=$placeType](around:1000,${userLocation.latitude},${userLocation.longitude});out;"
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.e("HomeActivity", "Error fetching $placeType: ${e.localizedMessage}")
            }

            override fun onResponse(call: okhttp3.Call, response: Response) {
                response.body?.string()?.let { responseBody ->
                    val json = JSONObject(responseBody)
                    val elements = json.getJSONArray("elements")
                    runOnUiThread {
                        clearMarkers()
                        for (i in 0 until elements.length()) {
                            val element = elements.getJSONObject(i)
                            val lat = element.getDouble("lat")
                            val lon = element.getDouble("lon")
                            val tags = element.optJSONObject("tags")
                            val name = tags?.optString("name") ?: "Unknown $placeType"

                            val marker = Marker(mapView).apply {
                                position = GeoPoint(lat, lon)
                                title = name
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                setOnMarkerClickListener { marker, _ ->
                                    showRatingDialog("place_$lat$lon", marker.title ?: "Unknown")
                                    true
                                }
                            }
                            mapView.overlays.add(marker)
                            currentMarkers.add(marker)
                        }
                        mapView.invalidate()
                    }
                }
            }
        })
    }

    private fun clearMarkers() {
        mapView.overlays.removeAll(currentMarkers)
        currentMarkers.clear()
        mapView.invalidate()
    }

    @SuppressLint("DefaultLocale")
    private fun getMockRating(): String {
        // Mock rating logic (you can replace this with real ratings)
        val rating = (1..5).random() + Math.random()
        return String.format("%.1f", rating) // Format to 1 decimal place
    }

    private fun showInfoDialog(title: String, message: String) {
        // Show a dialog with place info
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun submitRating(placeId: String, placeName: String, rating: Float, review: String) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Log.e("HomeActivity", "User not authenticated")
            showToast("User not authenticated")
            return
        }

        val userId = user.uid
        Log.d("HomeActivity", "User ID: $userId")

        val ratingData = hashMapOf(
            "userId" to userId,
            "placeName" to placeName,
            "rating" to rating,
            "review" to review,
            "timestamp" to System.currentTimeMillis()
        )

        val db = FirebaseFirestore.getInstance()
        db.collection("ratings")
            .document(placeId)
            .collection("user_ratings")
            .document(userId)
            .set(ratingData)
            .addOnSuccessListener {
                Log.d("HomeActivity", "Rating submitted successfully!")
                showToast("Rating submitted successfully!")
            }
            .addOnFailureListener { e ->
                Log.e("HomeActivity", "Error submitting rating: ${e.message}")
                showToast("Error submitting rating: ${e.message}")
            }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showRatingDialog(placeId: String, placeName: String) {
        // Inflate the dialog layout
        val dialogView = layoutInflater.inflate(R.layout.dialog_rating, null) as View

        // Initialize views from the dialog layout
        val ratingBar = dialogView.findViewById<RatingBar>(R.id.ratingBar)
        val reviewText = dialogView.findViewById<EditText>(R.id.reviewText)

        // Create and show the dialog
        AlertDialog.Builder(this)
            .setTitle("Rate $placeName")
            .setView(dialogView)
            .setPositiveButton("Submit") { _, _ ->
                val rating = ratingBar.rating
                val review = reviewText.text.toString()
                submitRating(placeId, placeName, rating, review)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_profile -> {
                startActivity(Intent(this, ProfileActivity::class.java))
                return true
            }
            R.id.nav_restaurants_near_me -> {
                userMarker?.let { fetchNearbyPlaces(it.position, "restaurant") }
                return true
            }
            R.id.nav_colleges_near_me -> {
                userMarker?.let { fetchNearbyPlaces(it.position, "college") }
                return true
            }
            R.id.nav_lodges_near_me -> {
                userMarker?.let { fetchNearbyPlaces(it.position, "lodge") }
                return true
            }
            R.id.nav_gardens_near_me -> {
                userMarker?.let { fetchNearbyPlaces(it.position, "park") }
                return true
            }
            R.id.nav_logout -> {
                showLogoutDialog()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { dialog, which ->
                // Perform logout action
                logoutUser()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun logoutUser() {
        // Example: Firebase logout
        FirebaseAuth.getInstance().signOut()

        // Navigate to the Login screen
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // Close the current activity
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_home, menu)
        return true
    }
}