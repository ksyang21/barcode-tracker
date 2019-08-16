package com.example.barcodetracker.Model

import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.parcel.Parcelize

@Parcelize
class ItemLocation(var name: String, var latLng: LatLng) : Parcelable
