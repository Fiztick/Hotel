package com.pnj.hotel

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.pnj.hotel.databinding.ActivityMainBinding
import com.pnj.hotel.kamar.Kamar
import com.pnj.hotel.kamar.KamarAdapter

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var kamarRecyclerView: RecyclerView
    private lateinit var kamarArrayList: ArrayList<Kamar>
    private lateinit var kamarAdapter: KamarAdapter
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        kamarRecyclerView = binding.kamarListView
        kamarRecyclerView.layoutManager = LinearLayoutManager(this)
        kamarRecyclerView.setHasFixedSize(true)
    }
}