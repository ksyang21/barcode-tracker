package com.example.barcodetracker.Model

import com.example.barcodetracker.Model.Item
import com.google.android.gms.maps.model.LatLng

interface LocationCallback{
    fun onLocation(item: Item, documentGeopoint: LatLng)
}