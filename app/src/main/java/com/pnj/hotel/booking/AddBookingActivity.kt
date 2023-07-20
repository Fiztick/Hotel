package com.pnj.hotel.booking

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.firestore.FirebaseFirestore
import com.pnj.hotel.R
import com.pnj.hotel.databinding.ActivityAddBookingBinding
import com.pnj.hotel.databinding.ActivityAddKamarBinding
import com.pnj.hotel.kamar.PilihKamarActivity

class AddBookingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddBookingBinding
    private val firestoreDatabase = FirebaseFirestore.getInstance()

    private lateinit var activity: AppCompatActivity


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddBookingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.BtnAddBooking.setOnClickListener {
            addBooking()
        }

        binding.TxtAddNoKamar.setOnClickListener {
            activity = it.context as AppCompatActivity
            activity.startActivity(Intent(activity, PilihKamarActivity::class.java))

            val no_kamar = getNoKamar()

            binding.TxtAddNoKamar.setText(no_kamar)
        }
    }

    fun getNoKamar(): String {
        val intent = intent
        val no_kamar = intent.getStringExtra("no_kamar").toString()

        return no_kamar
    }

    fun addBooking() {
        var no_kamar: String = binding.TxtAddNoKamar.text.toString()
        var nik_penyewa: String = binding.TxtAddNikPenyewa.text.toString()
        var nama_penyewa: String = binding.TxtAddNamaPenyewa.text.toString()
        var no_telp_penyewa: String = binding.TxtAddNoTelpPenyewa.text.toString()
        var alamat_penyewa: String = binding.TxtAddAlamatPenyewa.text.toString()

        val booking: MutableMap<String, Any> = HashMap()
        booking["no_kamar"] = no_kamar
        booking["nik_penyewa"] = nik_penyewa
        booking["nama_penyewa"] = nama_penyewa
        booking["no_telp_penyewa"] = no_telp_penyewa
        booking["alamat_penyewa"] = alamat_penyewa

        if (!booking.isEmpty()) {
            // upload ke firebase
            firestoreDatabase.collection("booking").add(booking)
                .addOnSuccessListener {
                    val intentMain = Intent(this, BookingActivity::class.java)
                    startActivity(intentMain)
                }
        }
    }
}