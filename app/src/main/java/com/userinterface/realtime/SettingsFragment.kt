package com.userinterface.realtime

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment


class SettingsFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        val logAbsensiMenu = view.findViewById<LinearLayout>(R.id.menu_log_absensi)
        logAbsensiMenu.setOnClickListener {
            val intent = Intent(requireContext(), LogAbsensiActivity::class.java)
            startActivity(intent)
        }

        val ubahPasswordMenu = view.findViewById<LinearLayout>(R.id.menu_ubah_password)
        ubahPasswordMenu.setOnClickListener {
            val intent = Intent(requireContext(), ChangePasswordActivity::class.java)
            startActivity(intent)
        }

        return view
    }
}