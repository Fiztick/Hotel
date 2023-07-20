package com.pnj.hotel.booking

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.pnj.hotel.MainActivity
import com.pnj.hotel.R
import com.pnj.hotel.auth.SettingsActivity
import com.pnj.hotel.chat.ChatActivity
import com.pnj.hotel.databinding.ActivityBookingBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class BookingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBookingBinding

    private lateinit var bookingRecyclerView: RecyclerView
    private lateinit var bookingArrayList: ArrayList<Booking>
    private lateinit var bookingAdapter: BookingAdapter
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBookingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bookingRecyclerView = binding.bookingListView
        bookingRecyclerView.layoutManager = LinearLayoutManager(this)
        bookingRecyclerView.setHasFixedSize(true)

        bookingArrayList = arrayListOf()
        bookingAdapter = BookingAdapter(bookingArrayList)

        bookingRecyclerView.adapter = bookingAdapter

        load_data()

        swipeDelete()

        binding.txtSearchBooking.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val keyword = binding.txtSearchBooking.text.toString()
                if(keyword.isNotEmpty()) {
                    search_data(keyword)
                }
                else {
                    load_data()
                }
            }

            override fun afterTextChanged(p0: Editable?) {}
        })

        binding.btnAddBooking.setOnClickListener {
            val intentMain = Intent(this, AddBookingActivity::class.java)
            startActivity(intentMain)
        }

        binding.bottomNavigation.setOnItemSelectedListener {
            when(it.itemId) {
                R.id.nav_bottom_home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_bottom_booking -> {
                    val intent = Intent(this, BookingActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_bottom_setting -> {
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_bottom_chat -> {
                    val intent = Intent(this, ChatActivity::class.java)
                    startActivity(intent)
                }
            }
            true
        }
    }

    private fun load_data() {
        bookingArrayList.clear()
        db = FirebaseFirestore.getInstance()
        db.collection("booking")
            .addSnapshotListener(object : EventListener<QuerySnapshot> {
                override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                    if (error != null) {
                        Log.e("Firestore Error", error.message.toString())
                        return
                    }
                    for (dc: DocumentChange in value?.documentChanges!!) {
                        if (dc.type == DocumentChange.Type.ADDED)
                            bookingArrayList.add(dc.document.toObject(Booking::class.java))
                    }
                    bookingAdapter.notifyDataSetChanged()
                }

            })
    }

    private fun search_data(keyword : String) {
        bookingArrayList.clear()

        db = FirebaseFirestore.getInstance()

        //query untuk firebase
        val query = db.collection("booking")
            .orderBy("no_kamar")
            .startAt(keyword)
            .get()
        query.addOnSuccessListener {
            bookingArrayList.clear()
            for (document in it) {
                bookingArrayList.add(document.toObject(Booking::class.java))
            }
        }
    }

    private fun deleteBooking(booking: Booking, doc_id: String) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Apakah ${booking.no_kamar} ingin dihapus ?")
            .setCancelable(false)
            .setPositiveButton("Yes") {dialog, id ->
                lifecycleScope.launch {
                    db.collection("booking")
                        .document(doc_id).delete()

                    Toast.makeText(
                        applicationContext,
                        booking.no_kamar.toString() + " is deleted",
                        Toast.LENGTH_LONG
                    ).show()
                    load_data()
                }
            }
            .setNegativeButton("No") {dialog, id ->
                dialog.dismiss()
                load_data()
            }
        val alert = builder.create()
        alert.show()
    }

    private fun swipeDelete() {
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0,
        ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                lifecycleScope.launch {
                    val booking = bookingArrayList[position]
                    val bookingQuery = db.collection("booking")
                        .whereEqualTo("no_kamar", booking.no_kamar)
                        .whereEqualTo("nik_penyewa", booking.nik_penyewa)
                        .get()
                        .await()
                    if(bookingQuery.documents.isNotEmpty()) {
                        for (document in bookingQuery) {
                            try {
                                deleteBooking(booking, document.id)
                                load_data()
                            } catch (e : Exception) {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(
                                        applicationContext,
                                        e.message.toString(),
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                    }
                    else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                applicationContext,
                                "Booking yang ingin di hapus tidak ditemukan",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        }).attachToRecyclerView(bookingRecyclerView)
    }
}