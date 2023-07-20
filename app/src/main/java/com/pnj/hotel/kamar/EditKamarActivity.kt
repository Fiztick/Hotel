package com.pnj.hotel.kamar

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.pnj.hotel.MainActivity
import com.pnj.hotel.databinding.ActivityEditKamarBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File

class EditKamarActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditKamarBinding
    private val db = FirebaseFirestore.getInstance()

    private val REQ_CAM = 101
    private lateinit var imgUri: Uri
    private var dataGambar: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEditKamarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // set default value dari item yang dipilih
        val (curr_kamar) = setDefaultValue()
        showFoto()

        binding.BtnEditKamar.setOnClickListener {
            val new_data_kamar = newKamar()
            updateKamar(curr_kamar as Kamar, new_data_kamar)

            val intentMain = Intent(this, MainActivity::class.java)
            startActivity(intentMain)
            finish()
        }

        binding.BtnEditImgKamar.setOnClickListener {
            openCamera()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQ_CAM && resultCode == RESULT_OK) {
            dataGambar = data?.extras?.get("data") as Bitmap
            binding.BtnEditImgKamar.setImageBitmap(dataGambar)
        }
    }

    fun setDefaultValue(): Array<Any> {
        val intent = intent
        val no_kamar = intent.getStringExtra("no_kamar").toString()
        val tipe_kamar = intent.getStringExtra("tipe_kamar").toString()

        binding.TxtEditNoKamar.setText(no_kamar)

        if (tipe_kamar == "Single Bed") {
            binding.RdnEditSingleBed.isChecked = true
        } else if (tipe_kamar == "Double Bed") {
            binding.RdnEditDoubleBed.isChecked = true
        } else if (tipe_kamar == "Twin Bed") {
            binding.RdnEditTwinBed.isChecked = true
        }
        val curr_kamar = Kamar(no_kamar, tipe_kamar)
        return arrayOf(curr_kamar)
    }

    fun newKamar(): Map<String, Any> {
        var no_kamar: String = binding.TxtEditNoKamar.text.toString()

        var tipe_kamar: String = ""
        if (binding.RdnEditSingleBed.isChecked) {
            tipe_kamar = "Single Bed"
        } else if (binding.RdnEditDoubleBed.isChecked) {
            tipe_kamar = "Double Bed"
        } else if (binding.RdnEditTwinBed.isChecked) {
            tipe_kamar = "Twin Bed"
        }

        val kamar = mutableMapOf<String, Any>()
        kamar["no_kamar"] = no_kamar
        kamar["tipe_kamar"] = tipe_kamar

        if (dataGambar != null) {
            uploadPictFirebase(dataGambar!!, "${no_kamar}")
        } else {
            Toast.makeText(this@EditKamarActivity,
                "${no_kamar}", Toast.LENGTH_LONG).show()
        }

        return kamar
    }

    private fun uploadPictFirebase(img_bitmap: Bitmap, file_name: String) {
        val baos = ByteArrayOutputStream()
        val ref = FirebaseStorage.getInstance().reference.child("img_kamar/${file_name}.jpg")

        img_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)

        val img = baos.toByteArray()
        ref.putBytes(img)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    ref.downloadUrl.addOnCompleteListener { Task ->
                       Task.result.let { Uri ->
                           imgUri = Uri
                           binding.BtnEditImgKamar.setImageBitmap(img_bitmap)
                       }
                    }
                }
            }
    }

    private fun updateKamar(kamar: Kamar, newKamarMap: Map<String, Any>) =
        CoroutineScope(Dispatchers.IO).launch {
            val kamarQuery = db.collection("kamar")
                .whereEqualTo("no_kamar", kamar.no_kamar)
                .whereEqualTo("tipe_kamar", kamar.tipe_kamar)
                .get()
                .await()
            if (kamarQuery.documents.isNotEmpty()) {
                for (document in kamarQuery) {
                    try {
                        db.collection("kamar").document(document.id).set(
                            newKamarMap,
                            SetOptions.merge()
                        )
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@EditKamarActivity,
                            e.message, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }

            deleteFoto("img_kamar/${kamar.no_kamar}.jpg")
        }

    fun showFoto() {
        val intent = intent
        val no_kamar = intent.getStringExtra("no_kamar").toString()

        val storageRef = FirebaseStorage.getInstance().reference.child("img_kamar/${no_kamar}.jpg")
        val localfile = File.createTempFile("tempImage", "jpg")
        storageRef.getFile(localfile).addOnSuccessListener {
            val bitmap = BitmapFactory.decodeFile(localfile.absolutePath)
            binding.BtnEditImgKamar.setImageBitmap(bitmap)
        }.addOnFailureListener {
            Log.e("foto ?", "gagal")
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

    private fun deleteFoto(file_name : String) {
        val storage = Firebase.storage
        val storageRef = storage.reference
        val deleteFileRef = storage.reference
        if (deleteFileRef != null) {
            deleteFileRef.delete().addOnSuccessListener {
                Log.e("deleted", "success")
            }.addOnFailureListener {
                Log.e("deleted", "failed")
            }
        }
    }
}