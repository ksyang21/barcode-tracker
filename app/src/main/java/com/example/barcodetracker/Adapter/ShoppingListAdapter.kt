package com.example.barcodetracker.Adapter

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import com.example.barcodetracker.Model.Item
import com.example.barcodetracker.R
import com.example.barcodetracker.ShoppingListActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_shopping_list.*

class ShoppingListAdapter(private val context: Activity, private var itemList: ArrayList<Item>)

    : ArrayAdapter<Item>(context, R.layout.shopping_list_list_view, itemList) {

    private val activity: ShoppingListActivity = context as ShoppingListActivity

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {

        val db = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        val inflater = context.layoutInflater
        val rowView = inflater.inflate(R.layout.shopping_list_list_view, null, true)

        val nameText = rowView.findViewById(R.id.item_name) as TextView
        val priceText = rowView.findViewById(R.id.item_price) as TextView
        val locationText = rowView.findViewById(R.id.item_location) as TextView
        val removeBtn = rowView.findViewById(R.id.removeBtn) as Button

        var item = getItem(position)
        nameText.text = item.name
        priceText.text = "RM " + String.format("%.2f",item.price.toDouble())
        locationText.text = item.location

        removeBtn.setOnClickListener {
            db.collection("shoppinglist").document(auth.currentUser!!.uid)
                .update("itemArray",FieldValue.arrayRemove(item))
                .addOnSuccessListener {
                    var itemPrice = itemList[position].price
                    var subtotal = activity.subtotal.text.toString().toDouble() - itemPrice.toDouble()
                    activity.subtotal.text = String.format("%.2f", subtotal)
                    itemList.removeAt(position)
                    notifyDataSetChanged()
                }
        }
        return rowView
    }
}