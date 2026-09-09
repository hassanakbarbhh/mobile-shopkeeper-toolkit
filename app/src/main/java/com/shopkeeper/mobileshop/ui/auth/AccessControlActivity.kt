package com.shopkeeper.mobileshop.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.FirebaseFirestore
import com.shopkeeper.mobileshop.R

class AccessControlActivity : AppCompatActivity() {

    private lateinit var rvUsers: RecyclerView
    private lateinit var adapter: UserAdapter
    private val usersList = mutableListOf<Map<String, Any>>()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_access_control)

        rvUsers = findViewById(R.id.rvUsers)
        rvUsers.layoutManager = LinearLayoutManager(this)
        adapter = UserAdapter(usersList)
        rvUsers.adapter = adapter

        // Listen to all users
        db.collection("users").addSnapshotListener { snapshot, e ->
            if (e != null) {
                Toast.makeText(this, "Listen failed: ${e.message}", Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }

            if (snapshot != null) {
                usersList.clear()
                for (doc in snapshot.documents) {
                    val data = doc.data?.toMutableMap() ?: continue
                    data["uid"] = doc.id
                    usersList.add(data)
                }
                adapter.notifyDataSetChanged()
            }
        }
    }

    inner class UserAdapter(private val users: List<Map<String, Any>>) :
        RecyclerView.Adapter<UserAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvEmail: TextView = view.findViewById(R.id.tvEmail)
            val tvRole: TextView = view.findViewById(R.id.tvRole)
            val tvStatus: TextView = view.findViewById(R.id.tvStatus)
            val btnAction: Button = view.findViewById(R.id.btnAction)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_user_access, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val user = users[position]
            val email = user["email"] as? String ?: ""
            val role = user["role"] as? String ?: "BASIC_USER"
            val approved = user["approved"] as? Boolean ?: false
            val uid = user["uid"] as? String ?: ""

            holder.tvEmail.text = email
            holder.tvRole.text = "Role: $role"
            holder.tvStatus.text = if (approved) "Approved" else "Pending"

            holder.btnAction.text = "Manage"
            holder.btnAction.setOnClickListener {
                showManageDialog(uid, role, approved)
            }
        }

        override fun getItemCount() = users.size
    }

    private fun showManageDialog(uid: String, currentRole: String, isApproved: Boolean) {
        val roles = arrayOf("OWNER", "SHOP_OWNER", "RESELLER", "REPAIR_SHOP", "BASIC_USER")
        val checkedItem = roles.indexOf(currentRole).takeIf { it >= 0 } ?: 4
        var selectedRole = currentRole

        MaterialAlertDialogBuilder(this)
            .setTitle("Manage User Access")
            .setSingleChoiceItems(roles, checkedItem) { _, which ->
                selectedRole = roles[which]
            }
            .setPositiveButton("Approve/Update") { _, _ ->
                db.collection("users").document(uid).update(
                    mapOf(
                        "role" to selectedRole,
                        "approved" to true,
                        "updatedAt" to System.currentTimeMillis()
                    )
                )
            }
            .setNegativeButton("Revoke/Kick") { _, _ ->
                db.collection("users").document(uid).update(
                    mapOf(
                        "approved" to false,
                        "role" to "BASIC_USER",
                        "updatedAt" to System.currentTimeMillis()
                    )
                )
            }
            .setNeutralButton("Cancel", null)
            .show()
    }
}
