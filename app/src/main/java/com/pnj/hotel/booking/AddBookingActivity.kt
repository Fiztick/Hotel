package com.pnj.hotel.booking

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.pnj.hotel.R
import com.pnj.hotel.databinding.ActivityAddBookingBinding
import com.pnj.hotel.databinding.ActivityAddKamarBinding
import com.pnj.hotel.kamar.Kamar
import com.pnj.hotel.kamar.PilihKamarActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AddBookingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddBookingBinding
    private val firestoreDatabase = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddBookingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val new_data_kamar = updateKetersediaan()
        val (curr_kamar) = setDefaultValue()

        binding.BtnAddBooking.setOnClickListener {
            addBooking()

            updateKamar(curr_kamar as Kamar, new_data_kamar)
        }

        binding.TxtAddNoKamar.setOnClickListener {
            val intent = Intent(this, PilihKamarActivity::class.java)
            startActivity(intent)

            val no_kamar = getNoKamar()
            val tipe_kamar = getTipekamar()

            binding.TxtAddNoKamar.setText(no_kamar)
            binding.TxtTipeKamar.setText(tipe_kamar)
        }
    }

    fun getNoKamar(): String {
        val intent = intent
        val no_kamar = intent.getStringExtra("no_kamar").toString()

        return no_kamar
    }
    fun getTipekamar(): String {
        val intent = intent
        val tipe_kamar = intent.getStringExtra("tipe_kamar").toString()

        return tipe_kamar
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

    private fun updateKamar(kamar: Kamar, newKamarMap: Map<String, Any>) =
        CoroutineScope(Dispatchers.IO).launch {
            val kamarQuery = firestoreDatabase.collection("kamar")
                .whereEqualTo("no_kamar", kamar.no_kamar)
                .whereEqualTo("tipe_kamar", kamar.tipe_kamar)
                .get()
                .await()
            if (kamarQuery.documents.isNotEmpty()) {
                for (document in kamarQuery) {
                    try {
                        firestoreDatabase.collection("kamar").document(document.id).set(
                            newKamarMap,
                            SetOptions.merge()
                        )
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@AddBookingActivity,
                                e.message, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }

    fun updateKetersediaan(): Map<String, Any> {
        var no_kamar: String = binding.TxtAddNoKamar.text.toString()
        var tipe_kamar: String = binding.TxtTipeKamar.text.toString()

        val kamar = mutableMapOf<String, Any>()
        kamar["no_kamar"] = no_kamar
        kamar["ketersediaan"] = "Tidak Tersedia"
        kamar["tipe_kamar"] = tipe_kamar

        return kamar
    }

    fun setDefaultValue(): Array<Any> {
        val intent = intent
        val no_kamar = intent.getStringExtra("no_kamar").toString()
        val tipe_kamar = intent.getStringExtra("tipe_kamar").toString()
        val ketersediaan = intent.getStringExtra("ketersediaan").toString()

        val curr_kamar = Kamar(no_kamar, tipe_kamar, ketersediaan)
        return arrayOf(curr_kamar)
    }
}