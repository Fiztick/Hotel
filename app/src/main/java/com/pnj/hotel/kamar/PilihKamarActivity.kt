package com.pnj.hotel.kamar

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.*
import com.pnj.hotel.databinding.ActivityPilihKamarBinding

class PilihKamarActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPilihKamarBinding

    private lateinit var pilihKamarRecyclerView: RecyclerView
    private lateinit var pilihKamarArrayList: ArrayList<Kamar>
    private lateinit var pilihKamarAdapter: PilihKamarAdapter
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPilihKamarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pilihKamarRecyclerView = binding.pilihKamarListView
        pilihKamarRecyclerView.layoutManager = LinearLayoutManager(this)
        pilihKamarRecyclerView.setHasFixedSize(true)

        pilihKamarArrayList = arrayListOf()
        pilihKamarAdapter = PilihKamarAdapter(pilihKamarArrayList)

        pilihKamarRecyclerView.adapter = pilihKamarAdapter

        load_data()
    }

    private fun load_data() {
        pilihKamarArrayList.clear()
        db = FirebaseFirestore.getInstance()
        db.collection("kamar").
        addSnapshotListener(object : EventListener<QuerySnapshot> {
            override fun onEvent(
                value: QuerySnapshot?,
                error: FirebaseFirestoreException?
            ) {
                if (error != null) {
                    Log.e("Firestore Error", error.message.toString())
                    return
                }
                for (dc: DocumentChange in value?.documentChanges!!) {
                    if (dc.type == DocumentChange.Type.ADDED)
                        pilihKamarArrayList.add(dc.document.toObject(Kamar::class.java))
                }
                pilihKamarAdapter.notifyDataSetChanged()
            }
        })
    }

    private fun search_data(keyword : String) {
        pilihKamarArrayList.clear()

        db = FirebaseFirestore.getInstance()
        // bikin query firebase
        val query = db.collection("kamar")
            .orderBy("no_kamar")
            .startAt(keyword)
            .get()
        query.addOnSuccessListener {
            pilihKamarArrayList.clear()
            for (document in it) {
                pilihKamarArrayList.add(document.toObject(Kamar::class.java))
            }
        }
    }
}