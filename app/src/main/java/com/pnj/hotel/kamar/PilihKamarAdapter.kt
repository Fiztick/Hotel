package com.pnj.hotel.kamar

import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.storage.FirebaseStorage
import com.pnj.hotel.R
import com.pnj.hotel.booking.AddBookingActivity
import java.io.File

class PilihKamarAdapter(private val pilihKamarList: ArrayList<Kamar>) :
    RecyclerView.Adapter<PilihKamarAdapter.PilihKamarViewHolder>(){

    private lateinit var activity: AppCompatActivity

    class PilihKamarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val no_kamar: TextView = itemView.findViewById(R.id.TVLNoKamar)
        val tipe_kamar: TextView = itemView.findViewById(R.id.TVLTipeKamar)
        val ketersediaan: TextView = itemView.findViewById(R.id.TVLKetersediaan)
        val img_kamar: ImageView = itemView.findViewById(R.id.IMLGambarKamar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PilihKamarViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.kamar_list_layout, parent, false)
        return PilihKamarViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PilihKamarViewHolder, position: Int) {
        val kamar: Kamar = pilihKamarList[position]
        holder.no_kamar.text = kamar.no_kamar
        holder.tipe_kamar.text = kamar.tipe_kamar
        holder.ketersediaan.text = kamar.ketersediaan

        // item diklik loncat balik ke addbooking activity
        holder.itemView.setOnClickListener {
            activity = it.context as AppCompatActivity
            activity.startActivity(Intent(activity, AddBookingActivity::class.java).apply {
                putExtra("no_kamar", kamar.no_kamar.toString())
                putExtra("tipe_kamar", kamar.tipe_kamar.toString())
            })
        }

        val dir_gambar: String = "img_kamar/${kamar.no_kamar}.jpg"
        Log.e("directory", dir_gambar)

        val storageRef = FirebaseStorage.getInstance().reference.child(dir_gambar)
        val localfile = File.createTempFile("tempImage", "jpg")
        storageRef.getFile(localfile).addOnSuccessListener {
            val bitmap = BitmapFactory.decodeFile(localfile.absolutePath)
            holder.img_kamar.setImageBitmap(bitmap)
        }.addOnFailureListener {
            Log.e("foto ?", "gagal")
        }
    }

    override fun getItemCount(): Int {
        return pilihKamarList.size
    }
}