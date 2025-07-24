package com.userinterface.realtime

import com.google.firebase.Timestamp

data class Absensi(
    val nama: String = "",
    val waktu: Timestamp? = null,
    val tipe: String = "",
    val uid: String = ""
)
