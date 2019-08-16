package com.example.barcodetracker

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.appcompat.app.AppCompatActivity
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

import java.io.IOException

class ScannedBarcodeActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private val auth = FirebaseAuth.getInstance()

    private lateinit var txtBarcodeValue: TextView
    private lateinit var btnShoppingList: Button
    private lateinit var btnLogout: Button

    private lateinit var surfaceView: SurfaceView
    private lateinit var barcodeDetector: BarcodeDetector
    private lateinit var cameraSource: CameraSource
    private val REQUEST_CAMERA_PERMISSION = 201

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanned_barcode)

        initViews()

        btnShoppingList.setOnClickListener {
            startActivity(Intent(this@ScannedBarcodeActivity, ShoppingListActivity::class.java))
        }

        btnLogout.setOnClickListener {
            auth.signOut()
            finish()
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun initViews() {
        surfaceView = findViewById(R.id.surfaceView)
        txtBarcodeValue = findViewById(R.id.txtBarcodeValue)
        btnShoppingList = findViewById(R.id.view_shopping_list)
        btnLogout = findViewById(R.id.logout_button)

        barcodeDetector  = BarcodeDetector.Builder(this)
            .setBarcodeFormats(Barcode.EAN_13).build()
        cameraSource = CameraSource.Builder(this, barcodeDetector!!)
            .setRequestedPreviewSize(1920, 1080)
            .setAutoFocusEnabled(true) //you should add this feature
            .build()
    }

    private fun initialiseDetectorsAndSources() {

        Toast.makeText(applicationContext, "Barcode scanner started", Toast.LENGTH_SHORT).show()

        surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                try {
                    if (ActivityCompat.checkSelfPermission(
                            this@ScannedBarcodeActivity,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        cameraSource!!.start(surfaceView.holder)
                    } else {
                        ActivityCompat.requestPermissions(
                            this@ScannedBarcodeActivity,
                            arrayOf(Manifest.permission.CAMERA),
                            REQUEST_CAMERA_PERMISSION
                        )
                    }

                } catch (e: IOException) {
                    e.printStackTrace()
                }


            }

            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                cameraSource!!.stop()
            }
        })


        barcodeDetector!!.setProcessor(object : Detector.Processor<Barcode> {
            override fun release() {
                Toast.makeText(
                    applicationContext,
                    "To prevent memory leaks barcode scanner has been stopped",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun receiveDetections(detections: Detector.Detections<Barcode>) {
                val barcodes = detections.detectedItems
                if (barcodes.size() != 0) {
                    txtBarcodeValue.post {
                        if (barcodes.valueAt(0).displayValue != null) {
                            barcodeDetector.release()
                            txtBarcodeValue.removeCallbacks(null)
                            txtBarcodeValue.text  = barcodes.valueAt(0).displayValue.toString()
                            getFromDatabase(barcodes.valueAt(0).displayValue.toString())
                        }
                    }

                }
            }
        })
    }


    override fun onPause() {
        super.onPause()
        cameraSource!!.release()
        initViews()
    }

    override fun onResume() {
        super.onResume()
        initialiseDetectorsAndSources()
    }

    private fun getFromDatabase(barcodeValue: String){
        db = FirebaseFirestore.getInstance()

        db.collection("items").whereEqualTo("barcodeValue",barcodeValue)
            .get().addOnSuccessListener { result ->
                if(!result.isEmpty){
                    var intent = Intent(this, PriceListActivity:: class.java)
                    for(document in result){
                        var name = document.data["name"].toString()
                        intent.putExtra("barcodeValue", barcodeValue)
                        intent.putExtra("itemName", name)
                    }
                    startActivity(intent)
                } else {
                    val builder = AlertDialog.Builder(this)
                    builder.setMessage("No item information found!\nDo you want to add information about this item?")
                    builder.setPositiveButton("YES") { _, _ ->
                        var intent = Intent(this@ScannedBarcodeActivity, AddItemActivity::class.java)
                        intent.putExtra("barcodeValue", barcodeValue)
                        startActivity(intent)
                    }
                    builder.setNegativeButton("NO") { _, _ ->
                        initialiseDetectorsAndSources()
                    }
                    val dialog: AlertDialog = builder.create()
                    dialog.show()
                }
            }
    }

}