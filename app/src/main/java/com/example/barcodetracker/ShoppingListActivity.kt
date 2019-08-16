package com.example.barcodetracker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import com.example.barcodetracker.Adapter.ShoppingListAdapter
import com.example.barcodetracker.Model.Item
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ShoppingListActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private lateinit var subtotalTextView: TextView
    private lateinit var lstItem: ListView
    private lateinit var shoppingListAdapter: ShoppingListAdapter

    private var items = arrayListOf<Item>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shopping_list)

        lstItem = findViewById(R.id.item_list)
        shoppingListAdapter = ShoppingListAdapter(this@ShoppingListActivity, items)
        lstItem.adapter = shoppingListAdapter

        var btnConfirm = findViewById<Button>(R.id.confirm_button)
        subtotalTextView = findViewById(R.id.subtotal)

        addData()

        btnConfirm.setOnClickListener {
            finish()
        }
    }

    private fun addData(){
        db.collection("shoppinglist")
            .document(auth.currentUser!!.uid)
            .get().addOnSuccessListener { document ->
                if(document != null){
                    var subtotal = 0.00
                    var itemArray = document.get("itemArray") as? ArrayList<HashMap<String, String>>

                    for(i in itemArray.orEmpty()) {
                        var item = Item(i["name"]!!,i["price"]!!,i["location"]!!)
                        shoppingListAdapter.add(item)
                        subtotal = countSubtotal(i["price"]!!.toDouble(), subtotal)
                        subtotalTextView.text = String.format("%.2f",subtotal)
                    }
                }
            }
    }

    private fun countSubtotal(addedPrice: Double, itemsPrice: Double): Double{
        return addedPrice + itemsPrice
    }
}
