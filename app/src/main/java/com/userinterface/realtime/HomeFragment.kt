package com.userinterface.realtime

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.userinterface.realtime.databinding.FragmentHomeBinding
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        db = Firebase.firestore
        auth = FirebaseAuth.getInstance()

        setGreetingBasedOnTime()
        setupUserInfo()
        setupWorkingSchedule()
        checkAbsensiStatus()
        setupAttendanceHistory()

        binding.btnAbsen.setOnClickListener {
            handleAbsen()
        }

        binding.llAttendanceList.setOnClickListener {
            val intent = Intent(activity, LogAbsensiActivity::class.java)
            startActivity(intent)
        }

        binding.tvViewAll.setOnClickListener {
            val intent = Intent(activity, LogAbsensiActivity::class.java)
            startActivity(intent)
        }

        binding.llAttendanceCorrection.setOnClickListener {
            Toast.makeText(context, "Stay Tune!!", Toast.LENGTH_SHORT).show()
        }
        binding.llOnDuty.setOnClickListener {
            Toast.makeText(context, "Stay Tune!!", Toast.LENGTH_SHORT).show()
        }
        binding.llLeave.setOnClickListener {
            Toast.makeText(context, "Stay Tune!!", Toast.LENGTH_SHORT).show()
        }

        return binding.root
    }

    private fun setGreetingBasedOnTime() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        val greeting: String = when (hour) {
            in 0..11 -> "Selamat Pagi,"
            in 12..17 -> "Selamat Siang,"
            in 18..23 -> "Selamat Malam,"
            else -> "Halo,"
        }
        binding.tvGreeting.text = greeting
    }

    private fun setupUserInfo() {
        val uid = auth.currentUser?.uid ?: run {
            Toast.makeText(context, "User tidak login.", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                val name = document.getString("nama") ?: "User Name"
                val position = document.getString("position") ?: "Posisi Tidak Diketahui"

                binding.tvUserName.text = name
                binding.tvUserPosition.text = position
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Gagal memuat info pengguna: ${e.message}", Toast.LENGTH_SHORT).show()
                binding.tvUserName.text = "Error"
                binding.tvUserPosition.text = "Error"
            }
    }


    private fun setupWorkingSchedule() {
        val dateFormat = SimpleDateFormat("EEE, dd MMM yyyy", Locale("id", "ID"))
        val currentDate = dateFormat.format(Calendar.getInstance().time)
        binding.tvWorkingScheduleDate.text = currentDate
        binding.tvWorkingHours.text = "09:00 - 18:00"
    }

    private fun checkAbsensiStatus() {
        val uid = auth.currentUser?.uid ?: return
        val todayStart = getStartOfDay()
        val todayEnd = getEndOfDay()

        db.collection("absensi")
            .whereEqualTo("uid", uid)
            .whereGreaterThanOrEqualTo("waktu", todayStart)
            .whereLessThanOrEqualTo("waktu", todayEnd)
            .get()
            .addOnSuccessListener { result ->
                val masuk = result.any { it.getString("tipe") == "masuk" }
                val pulang = result.any { it.getString("tipe") == "pulang" }

                when {
                    !masuk -> {
                        binding.btnAbsen.text = "Check In"
                        binding.btnAbsen.isEnabled = true
                    }
                    masuk && !pulang -> {
                        binding.btnAbsen.text = "Check Out"
                        binding.btnAbsen.isEnabled = true
                    }
                    else -> {
                        binding.btnAbsen.text = "Sudah Absen Hari Ini"
                        binding.btnAbsen.isEnabled = false
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Gagal cek absensi", Toast.LENGTH_SHORT).show()
            }
    }

    private fun handleAbsen() {
        val uid = auth.currentUser?.uid ?: return
        val waktu = Timestamp.now()

        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                val nama = document.getString("nama") ?: "Tidak diketahui"
                val tipe = if (binding.btnAbsen.text.toString().contains("Check Out", ignoreCase = true)) {
                    "pulang"
                } else {
                    "masuk"
                }

                val data = hashMapOf(
                    "uid" to uid,
                    "nama" to nama,
                    "tipe" to tipe,
                    "waktu" to waktu
                )

                db.collection("absensi")
                    .add(data)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Berhasil absen $tipe", Toast.LENGTH_SHORT).show()
                        checkAbsensiStatus()
                        setupAttendanceHistory() // Perbarui history setelah absen
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Gagal absen", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Gagal mengambil data user", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupAttendanceHistory() {
        val uid = auth.currentUser?.uid ?: return
        val dateFormat = SimpleDateFormat("EEE, dd MMM yyyy", Locale("id", "ID"))
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        db.collection("absensi")
            .whereEqualTo("uid", uid)
            .orderBy("waktu", Query.Direction.DESCENDING)
            .limit(100)
            .get()
            .addOnSuccessListener { result ->
                val dailyAttendanceMap = mutableMapOf<String, Pair<Timestamp?, Timestamp?>>() // DateString -> (MasukTimestamp, PulangTimestamp)

                for (document in result.documents) {
                    val timestamp = document.getTimestamp("waktu") ?: continue
                    val type = document.getString("tipe") ?: continue

                    val date = Calendar.getInstance().apply { time = timestamp.toDate() }
                    val dateString = dateFormat.format(date.time)

                    val currentEntry = dailyAttendanceMap[dateString] ?: (null to null)

                    when (type) {
                        "masuk" -> {
                            // Ambil yang paling awal untuk masuk
                            if (currentEntry.first == null || timestamp.compareTo(currentEntry.first!!) < 0) {
                                dailyAttendanceMap[dateString] = timestamp to currentEntry.second
                            }
                        }
                        "pulang" -> {
                            // Ambil yang paling akhir untuk pulang
                            if (currentEntry.second == null || timestamp.compareTo(currentEntry.second!!) > 0) {
                                dailyAttendanceMap[dateString] = currentEntry.first to timestamp
                            }
                        }
                    }
                }

                val sortedDates = dailyAttendanceMap.keys.sortedByDescending {
                    SimpleDateFormat("EEE, dd MMM yyyy", Locale("id", "ID")).parse(it)
                }.take(2)


                if (sortedDates.isNotEmpty()) {
                    val firstDate = sortedDates[0]
                    val (masukTimestamp, pulangTimestamp) = dailyAttendanceMap[firstDate]!!
                    binding.cardAttendanceWed.visibility = View.VISIBLE // Pastikan card terlihat
                    binding.tvDateWed.text = firstDate
                    binding.tvStartTimeWed.text = masukTimestamp?.toDate()?.let { timeFormat.format(it) } ?: "N/A"
                    binding.tvEndTimeWed.text = pulangTimestamp?.toDate()?.let { timeFormat.format(it) } ?: "N/A"
                } else {
                    binding.cardAttendanceWed.visibility = View.GONE
                }

                if (sortedDates.size > 1) {
                    val secondDate = sortedDates[1]
                    val (masukTimestamp, pulangTimestamp) = dailyAttendanceMap[secondDate]!!
                    binding.cardAttendanceTue.visibility = View.VISIBLE
                    binding.tvDateTue.text = secondDate
                    binding.tvStartTimeTue.text = masukTimestamp?.toDate()?.let { timeFormat.format(it) } ?: "N/A"
                    binding.tvEndTimeTue.text = pulangTimestamp?.toDate()?.let { timeFormat.format(it) } ?: "N/A"
                } else {
                    binding.cardAttendanceTue.visibility = View.GONE
                }

                if (sortedDates.isEmpty()) {
                    binding.cardAttendanceWed.visibility = View.GONE
                    binding.cardAttendanceTue.visibility = View.GONE
                }

            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Gagal memuat riwayat absensi: ${e.message}", Toast.LENGTH_SHORT).show()
                binding.tvStartTimeWed.text = "Error"
                binding.tvEndTimeWed.text = "Error"
                binding.tvStartTimeTue.text = "Error"
                binding.tvEndTimeTue.text = "Error"
            }
    }


    private fun getStartOfDay(calendar: Calendar = Calendar.getInstance()): Timestamp {
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return Timestamp(calendar.time)
    }

    private fun getEndOfDay(calendar: Calendar = Calendar.getInstance()): Timestamp {
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return Timestamp(calendar.time)
    }
}