package com.pnj.hotel.kamar

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.pnj.hotel.MainActivity
import com.pnj.hotel.databinding.ActivityAddKamarBinding
import java.io.ByteArrayOutputStream
import java.util.Calendar

class AddKamarActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddKamarBinding
    private val firestoreDatabase = FirebaseFirestore.getInstance()

    private val REQ_CAM = 101
    private lateinit var imgUri: Uri
    private var dataGambar: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddKamarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.BtnAddKamar.setOnClickListener {
            addKamar()
        }

        binding.BtnAddImgKamar.setOnClickListener {
            openCamera()
        }
    }

    fun addKamar() {
        var no_kamar: String = binding.TxtAddNoKamar.text.toString()
        var ketersediaan: String = "Tersedia"

        var tipe_kamar: String = ""
        if (binding.RdnAddSingleBed.isChecked) {
            tipe_kamar = "Single Bed"
        } else if (binding.RdnAddDoubleBed.isChecked) {
            tipe_kamar = "Double Bed"
        } else if (binding.RdnAddTwinBed.isChecked) {
            tipe_kamar = "Twin Bed"
        }

        // sesuain sama data class
        val kamar: MutableMap<String, Any> = HashMap()
        kamar["no_kamar"] = no_kamar
        kamar["tipe_kamar"] = tipe_kamar
        kamar["ketersediaan"] = ketersediaan

        if (dataGambar != null) {
            // upload gambar ke storage
            uploadPictFirebase(dataGambar!!, "${no_kamar}")

            // upload datanya ke firebase
            firestoreDatabase.collection("kamar").add(kamar)
                .addOnSuccessListener {
                    val intentMain = Intent(this, MainActivity::class.java)
                    startActivity(intentMain)
                }
        }
    }

    private fun openCamera() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { intent ->
            this.packageManager?.let {
                intent?.resolveActivity(it).also {
                    startActivityForResult(intent, REQ_CAM)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_CAM && resultCode == RESULT_OK) {
            dataGambar = data?.extras?.get("data") as Bitmap
            binding.BtnAddImgKamar.setImageBitmap(dataGambar)
        }
    }

    private fun uploadPictFirebase(img_bitmap: Bitmap, file_name: String) {
        val baos = ByteArrayOutputStream()
        val ref = FirebaseStorage.getInstance().reference.child("img_kamar/${file_name}.jpg")
        img_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)

        val img = baos.toByteArray()
        ref.putBytes(img)
            .addOnCompleteListener {
                ref.downloadUrl.addOnCompleteListener { Task ->
                    Task.result.let { Uri ->
                        imgUri = Uri
                        binding.BtnAddImgKamar.setImageBitmap(img_bitmap)
                    }
                }
            }
    }
}