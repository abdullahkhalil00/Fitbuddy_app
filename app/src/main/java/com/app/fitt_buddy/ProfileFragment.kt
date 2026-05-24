package com.app.fitt_buddy

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var tvName: TextView
    private lateinit var tvEmail: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_profile, container, false)
        
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        tvName = view.findViewById(R.id.tv_profile_name)
        tvEmail = view.findViewById(R.id.tv_profile_email)
        val editGoalsBtn = view.findViewById<View>(R.id.cl_edit_goals)
        val notificationsBtn = view.findViewById<View>(R.id.cl_notifications)
        val subscriptionBtn = view.findViewById<View>(R.id.cl_subscription)
        val helpSupportBtn = view.findViewById<View>(R.id.cl_help_support)
        val logoutBtn = view.findViewById<View>(R.id.cl_logout)

        loadProfileData()

        editGoalsBtn.setOnClickListener {
            startActivity(Intent(activity, EditGoalsActivity::class.java))
        }

        notificationsBtn.setOnClickListener {
            showUnavailableDialog()
        }

        subscriptionBtn.setOnClickListener {
            showUnavailableDialog()
        }

        helpSupportBtn.setOnClickListener {
            showHelpSupportDialog()
        }

        logoutBtn.setOnClickListener {
            auth.signOut()
            val intent = Intent(activity, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        return view
    }

    private fun showUnavailableDialog() {
        val context = context ?: return
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true) 
        dialog.setContentView(R.layout.dialog_feature_unavailable)
        
        // Ensure the dialog window background is transparent to respect the rounded corners in the XML
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Set width to 90% of screen
        val width = (resources.displayMetrics.widthPixels * 0.90).toInt()
        dialog.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)

        val btnClose = dialog.findViewById<ImageButton>(R.id.btn_close)
        val btnOk = dialog.findViewById<Button>(R.id.btn_ok)

        btnClose.setOnClickListener { dialog.dismiss() }
        btnOk.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    private fun showHelpSupportDialog() {
        val context = context ?: return
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_help_support)
        
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val width = (resources.displayMetrics.widthPixels * 0.90).toInt()
        dialog.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)

        val btnClose = dialog.findViewById<ImageButton>(R.id.btn_close)
        val btnOk = dialog.findViewById<Button>(R.id.btn_ok)

        btnClose.setOnClickListener { dialog.dismiss() }
        btnOk.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    private fun loadProfileData() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (isAdded && document != null && document.exists()) {
                    val name = document.getString("name") ?: "User Name"
                    val email = document.getString("email") ?: "user@example.com"
                    
                    tvName.text = name
                    tvEmail.text = email
                }
            }
            .addOnFailureListener { e ->
                if (isAdded) {
                    Toast.makeText(context, "Error loading profile: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onResume() {
        super.onResume()
        loadProfileData() // Refresh data when returning from EditGoalsActivity
    }
}
