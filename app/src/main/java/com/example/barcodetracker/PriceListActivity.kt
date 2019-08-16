package com.example.barcodetracker

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ListView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.barcodetracker.Adapter.ItemListAdapter
import com.example.barcodetracker.Model.Item
import com.example.barcodetracker.Model.LocationCallback
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import java.text.DecimalFormat

class PriceListActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient

    private lateinit var lstItem: ListView
    private lateinit var itemListAdapter: ItemListAdapter

    private var items = arrayListOf<Item>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_price_list)

        var barcodeValue = intent.getStringExtra("barcodeValue")
        var itemName = intent.getStringExtra("itemName")

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        lstItem = findViewById(R.id.item_list)
        itemListAdapter = ItemListAdapter(this@PriceListActivity, items)
        lstItem.adapter = itemListAdapter

        var btnConfirm = findViewById<Button>(R.id.confirm_button)
        var btnAddItem = findViewById<Button>(R.id.add_item_button)

        addDataOnDistanceToList(itemName)

        btnAddItem.setOnClickListener {
            var intent = Intent(this@PriceListActivity, AddItemActivity::class.java)
            intent.putExtra("existingItemName", itemName)
            intent.putExtra("barcodeValue", barcodeValue)
            startActivity(intent)
        }

        btnConfirm.setOnClickListener {
            finish()
        }
    }

    private fun addDataOnDistanceToList(itemName: String){
        var callback: LocationCallback = object: LocationCallback {
            override fun onLocation(item: Item,documentGeopoint: LatLng) {
                if(ContextCompat.checkSelfPermission(this@PriceListActivity,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                    var locationResult: Task<Location> = mFusedLocationProviderClient.lastLocation
                    locationResult.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            var mLastKnownLocation: Location? = task.result
                            var userLocation = LatLng(mLastKnownLocation!!.latitude, mLastKnownLocation.longitude)
                            var distance = calculateDistance(userLocation, documentGeopoint)
                            if (distance <= 25000) {
                                itemListAdapter.add(item)
                            }
                        }
                    }
                } else {
                    ActivityCompat.requestPermissions(this@PriceListActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),1)
                }
            }
        }
        getDataOnDistance(itemName, callback)
    }
    private fun getDataOnDistance(itemName: String, callback: LocationCallback) {
        db.collection("items").whereEqualTo("name",itemName)
            .get().addOnSuccessListener { result ->
                for(document in result){
                    var item = Item(
                        document.data["name"].toString(),
                        document.data["price"].toString(),
                        document.data["location"].toString()
                    )
                    var itemLocation = document.getGeoPoint("coordination")
                    callback.onLocation(item, LatLng(itemLocation!!.latitude, itemLocation.longitude))
                }
            }
    }

    private fun calculateDistance(selfLocation: LatLng, placeLikelihood: LatLng): Double {
        var result = FloatArray(1)
        Location.distanceBetween(
            selfLocation.latitude,
            selfLocation.longitude,
            placeLikelihood.latitude,
            placeLikelihood.longitude,
            result
        )
        return result[0].toDouble()
    }
}

