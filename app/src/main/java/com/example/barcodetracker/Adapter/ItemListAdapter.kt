package com.example.barcodetracker.Adapter

import android.app.Activity
import android.content.Intent
import android.net.Uri

import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.example.barcodetracker.Model.Item
import com.example.barcodetracker.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class ItemListAdapter(private val context: Activity, private var itemList: ArrayList<Item>)

    : ArrayAdapter<Item>(context, R.layout.item_info_list_view, itemList) {

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {

        val db = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        val inflater = context.layoutInflater
        val rowView = inflater.inflate(R.layout.item_info_list_view, null, true)

        val nameText = rowView.findViewById(R.id.item_name) as TextView
        val priceText = rowView.findViewById(R.id.item_price) as TextView
        val locationText = rowView.findViewById(R.id.item_location) as TextView
        val addBtn = rowView.findViewById(R.id.addBtn) as Button
        val reportBtn = rowView.findViewById(R.id.reportBtn) as Button

        var item = getItem(position)
        nameText.text = item.name
        priceText.text = "RM " + String.format("%.2f",item.price.toDouble())
        locationText.text = item.location

        addBtn.setOnClickListener {
            db.collection("shoppinglist")
                .document(auth.currentUser!!.uid)
                .update("itemArray", FieldValue.arrayUnion(item))
                .addOnSuccessListener {
                    val builder = AlertDialog.Builder(context)
                    builder.setMessage("${item.name} has been added to your shopping list.")
                    builder.setPositiveButton("OK") { _, _ ->
                    }

                    val dialog: AlertDialog = builder.create()
                    dialog.show()
                }
                .addOnFailureListener{
                    db.collection("shoppinglist")
                        .document(auth.currentUser!!.uid)
                        .set(item)
                }
        }

        reportBtn.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setMessage("Are you sure you want to lodge a complaint about ${item.name} in ${item.location}?")
            builder.setPositiveButton("Yes") { _, _ ->
                var intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://e-aduan.kpdnhep.gov.my"))
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
            }
            builder.setNegativeButton("No") { _, _ -> }

            val dialog: AlertDialog = builder.create()
            dialog.show()
        }
        return rowView
    }
}