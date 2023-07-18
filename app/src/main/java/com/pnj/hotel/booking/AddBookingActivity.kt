package com.pnj.hotel.booking

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.firestore.FirebaseFirestore
import com.pnj.hotel.R
import com.pnj.hotel.databinding.ActivityAddBookingBinding

class AddBookingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddBookingBinding
    private val firestoreDatabase = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    fun addBooking() {

    }
}