package com.pnj.hotel.booking

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.pnj.hotel.R

class BookingAdapter(private val bookingList: ArrayList<Booking>) :
    RecyclerView.Adapter<BookingAdapter.BookingViewHolder>(){

    private lateinit var activity: AppCompatActivity

    class BookingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val no_kamar: TextView = itemView.findViewById(R.id.TVLNoKamar)
        val nik_penyewa: TextView = itemView.findViewById(R.id.TVLNikPenyewa)
        val nama_penyewa: TextView = itemView.findViewById(R.id.TVLNamaPenyewa)
        val no_telp_penyewa: TextView = itemView.findViewById(R.id.TVLNoTelpPenyewa)
        val alamat_penyewa: TextView = itemView.findViewById(R.id.TVLAlamatPenyewa)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.booking_list_layout, parent, false)
        return BookingViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        val booking: Booking = bookingList[position]
        holder.no_kamar.text = booking.no_kamar
        holder.nik_penyewa.text = booking.nik_penyewa
        holder.nama_penyewa.text = booking.nama_penyewa
        holder.no_telp_penyewa.text = booking.no_telp_penyewa
        holder.alamat_penyewa.text = booking.alamat_penyewa

        holder.itemView.setOnClickListener {
            activity = it.context as AppCompatActivity
            activity.startActivity(Intent(activity, EditBookingActivity::class.java).apply {
                putExtra("no_kamar", booking.no_kamar.toString())
                putExtra("nik_penyewa", booking.nik_penyewa.toString())
                putExtra("nama_penyewa", booking.nama_penyewa.toString())
                putExtra("no_telp_penyewa", booking.no_telp_penyewa.toString())
                putExtra("alamat_penyewa", booking.alamat_penyewa.toString())
            })
        }
    }

    override fun getItemCount(): Int {
        return bookingList.size
    }
}