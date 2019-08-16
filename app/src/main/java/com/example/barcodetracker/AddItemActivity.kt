package com.example.barcodetracker

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.barcodetracker.Model.ItemLocation
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint

class AddItemActivity : AppCompatActivity() {

    private lateinit var db:FirebaseFirestore
    private lateinit var location: ItemLocation

    private lateinit var itemLocationInput: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_item)

        var itemNameInput = findViewById<EditText>(R.id.add_item_nameInput)
        var itemPriceInput = findViewById<EditText>(R.id.add_item_priceInput)
        itemLocationInput = findViewById(R.id.item_location)
        var btnAddLocation = findViewById<Button>(R.id.add_item_location)
        var btnAddItem = findViewById<Button>(R.id.add_item)
        var cancel = findViewById<Button>(R.id.cancel_add_item)

        var barcodeValue = intent.getStringExtra("barcodeValue")
        var itemName = intent.getStringExtra("existingItemName")

        itemNameInput.setText(itemName)

        btnAddLocation.setOnClickListener {
            var intent = Intent(this, MapsActivity::class.java)
            startActivityForResult(intent,1)
        }

        btnAddItem.setOnClickListener{
            if(checkEmptyField(itemNameInput, itemPriceInput, itemLocationInput.text.toString())) {
                addItem(
                    itemNameInput.text.toString(),
                    itemPriceInput.text.toString().toDouble(),
                    location,
                    barcodeValue
                )
            }
        }

        cancel.setOnClickListener {
           finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 1){
            if(resultCode == Activity.RESULT_OK) {
                location = data!!.getParcelableExtra("LOCATION") as ItemLocation
                itemLocationInput.text = location.name
            }
        }
    }

    private fun addItem(itemName: String, itemPrice: Double, location: ItemLocation, barcodeValue: String) {
        db = FirebaseFirestore.getInstance()

        var geoPointValue = GeoPoint(location.latLng.latitude, location.latLng.longitude)

        val item = HashMap<String, Any>()
        item["name"] = itemName
        item["price"] = itemPrice
        item["location"] = location.name
        item["barcodeValue"] = barcodeValue
        item["coordination"] = geoPointValue

        db.collection("items")
            .add(item)
            .addOnSuccessListener {
                val builder = AlertDialog.Builder(this)
                builder.setMessage("$itemName has been added. Thank you for your contribution.")
                builder.setPositiveButton("OK") { _, _ ->
                    finish()
                }

                val dialog: AlertDialog = builder.create()
                dialog.show()
            }
            .addOnFailureListener {
                Toast.makeText(baseContext, "Failed to add item.", Toast.LENGTH_SHORT)
            }
    }

    private fun checkEmptyField(itemName: EditText, itemPrice: EditText, location: String): Boolean{
        var counter = true
        val builder = AlertDialog.Builder(this)
        if(itemName.text.toString() == ""){
            counter = false
            builder.setMessage("Item name field cannot be empty!")
            builder.setPositiveButton("OK") { _, _ ->
            }
            val dialog: AlertDialog = builder.create()
            dialog.show()
        }

        if(itemPrice.text.toString() == ""){
            counter = false
            builder.setMessage("Item price field cannot be empty!")
            builder.setPositiveButton("OK") { _, _ ->
            }
            val dialog: AlertDialog = builder.create()
            dialog.show()
        }

        if(location == "No location selected"){
            counter = false
            builder.setMessage("Please select a location.")
            builder.setPositiveButton("OK") { _, _ ->
            }
            val dialog: AlertDialog = builder.create()
            dialog.show()
        }

        return counter
    }
}
