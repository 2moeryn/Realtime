package com.userinterface.realtime

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class AbsensiAdapter(private val absensiList: List<Absensi>) :
    RecyclerView.Adapter<AbsensiAdapter.AbsensiViewHolder>() {

    class AbsensiViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val namaTextView: TextView = view.findViewById(R.id.namaTextView)
        val waktuTextView: TextView = view.findViewById(R.id.waktuTextView)
        val tipeTextView: TextView = view.findViewById(R.id.tipeTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AbsensiViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_absensi, parent, false)
        return AbsensiViewHolder(view)
    }

    override fun onBindViewHolder(holder: AbsensiViewHolder, position: Int) {
        val absensi = absensiList[position]
        holder.namaTextView.text = absensi.nama
        holder.tipeTextView.text = absensi.tipe

        val timestamp = absensi.waktu
        holder.waktuTextView.text = if (timestamp != null) {
            val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            sdf.format(timestamp.toDate())
        } else {
            "Tidak ada waktu"
        }
    }

    override fun getItemCount(): Int = absensiList.size
}
