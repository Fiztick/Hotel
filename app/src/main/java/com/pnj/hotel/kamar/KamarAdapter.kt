package com.pnj.hotel.kamar

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.pnj.hotel.R

class KamarAdapter(private val kamarList: ArrayList<Kamar>) :
    RecyclerView.Adapter<KamarAdapter.KamarViewHolder>(){

    private lateinit var activity: AppCompatActivity

    class KamarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val no_kamar: TextView = itemView.findViewById(R.id.TVLNoKamar)
        val tipe_kamar: TextView = itemView.findViewById(R.id.TVLTipeKamar)
        val ketersediaan: TextView = itemView.findViewById(R.id.TVLKetersediaan)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KamarViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.kamar_list_layout, parent, false)
        return KamarViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: KamarViewHolder, position: Int) {
        val kamar: Kamar = kamarList[position]
        holder.no_kamar.text = kamar.no_kamar
        holder.tipe_kamar.text = kamar.tipe_kamar
        holder.ketersediaan.text = kamar.ketersediaan

        holder.itemView.setOnClickListener {
            activity = it.context as AppCompatActivity
            activity.startActivity(Intent(activity, EditKamarActivity::class.java).apply {
                putExtra("no_kamar", kamar.no_kamar.toString())
                putExtra("tipe_kamar", kamar.tipe_kamar.toString())
                putExtra("ketersediaan", kamar.ketersediaan.toString())
            })
        }
    }

    override fun getItemCount(): Int {
        return kamarList.size
    }
}