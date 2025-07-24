package com.userinterface.realtime

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class LogAbsensiActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AbsensiAdapter
    private val absensiList = ArrayList<Absensi>()

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_absensi)

        recyclerView = findViewById(R.id.logRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = AbsensiAdapter(absensiList)
        recyclerView.adapter = adapter

        val uid = auth.currentUser?.uid
        Log.d("LOG_ACTIVITY", "Current UID: $uid")

        if (uid != null) {
            db.collection("absensi")
                .whereEqualTo("uid", uid)
                .orderBy("waktu", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { result ->
                    absensiList.clear()
                    for (doc in result) {
                        val item = doc.toObject(Absensi::class.java)
                        absensiList.add(item)
                        Log.d("LOG_ACTIVITY", "Ambil: ${doc.data}")
                    }
                    if (absensiList.isEmpty()) {
                        Toast.makeText(this, "Belum ada log absensi", Toast.LENGTH_SHORT).show()
                    }
                    adapter.notifyDataSetChanged()
                }
                .addOnFailureListener { e ->
                    Log.e("LOG_ACTIVITY", "Gagal ambil data: ${e.message}")
                    Toast.makeText(this, "Gagal mengambil data absensi", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
