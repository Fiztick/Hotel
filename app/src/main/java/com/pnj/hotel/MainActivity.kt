package com.pnj.hotel

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.pnj.hotel.auth.SettingsActivity
import com.pnj.hotel.chat.ChatActivity
import com.pnj.hotel.databinding.ActivityMainBinding
import com.pnj.hotel.kamar.AddKamarActivity
import com.pnj.hotel.kamar.Kamar
import com.pnj.hotel.kamar.KamarAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

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

        kamarArrayList = arrayListOf()
        kamarAdapter = KamarAdapter(kamarArrayList)

        kamarRecyclerView.adapter = kamarAdapter

        load_data()

        swipeDelete()

        binding.txtSearchKamar.addTextChangedListener(object  : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val keyword = binding.txtSearchKamar.text.toString()
                if(keyword.isNotEmpty()) {
                    search_data(keyword)
                }
                else {
                    load_data()
                }
            }

            override fun afterTextChanged(p0: Editable?) {}
        })

        binding.btnAddKamar.setOnClickListener {
            val intentMain = Intent(this, AddKamarActivity::class.java)
            startActivity(intentMain)
        }

        binding.bottomNavigation.setOnItemSelectedListener {
            when(it.itemId) {
                R.id.nav_bottom_home -> {
                    val intent = Intent(this, MainActivity::class.java)
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
        kamarArrayList.clear()
        db = FirebaseFirestore.getInstance()
        db.collection("kamar").
                addSnapshotListener(object : EventListener<QuerySnapshot>{
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
                                kamarArrayList.add(dc.document.toObject(Kamar::class.java))
                        }
                        kamarAdapter.notifyDataSetChanged()
                    }
                })
    }

    private fun search_data(keyword : String) {
        kamarArrayList.clear()

        db = FirebaseFirestore.getInstance()

        // bikin query firebase
        val query = db.collection("kamar")
            .orderBy("nama")
            .startAt(keyword)
            .get()
        query.addOnSuccessListener {
            kamarArrayList.clear()
            for (document in it) {
                kamarArrayList.add(document.toObject(Kamar::class.java))
            }
        }
    }

    private fun deleteKamar(kamar : Kamar, doc_id : String) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Apakah ${kamar.no_kamar} ingin dihapus ?")
            .setCancelable(false)
            .setPositiveButton("Yes") {dialog, id ->
                lifecycleScope.launch {
                    db.collection("kamar")
                        .document(doc_id).delete()

                    deleteFoto("img_kamar/${kamar.no_kamar}.jpg")
                    Toast.makeText(
                        applicationContext,
                        kamar.no_kamar.toString() + " is deleted",
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
                    val kamar = kamarArrayList[position]
                    val kamarQuery = db.collection("kamar")
                        .whereEqualTo("no_kamar", kamar.no_kamar)
                        .whereEqualTo("tipe_kamar", kamar.tipe_kamar)
                        .get()
                        .await()
                    if (kamarQuery.documents.isNotEmpty()) {
                        for (document in kamarQuery) {
                            try {
                                deleteKamar(kamar, document.id)
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
                                "Kamar yang ingin di hapus tidak ditemukan",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        }).attachToRecyclerView(kamarRecyclerView)
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