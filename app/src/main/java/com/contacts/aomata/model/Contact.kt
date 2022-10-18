package com.contacts.aomata.model

import android.graphics.Bitmap
import android.net.Uri

data class Contact(val id: String, val name:String) {
    var numbers = ArrayList<String>()
    var emails = ArrayList<String>()
    var dob = listOf<String>()
    var photo = listOf<Bitmap>()
}