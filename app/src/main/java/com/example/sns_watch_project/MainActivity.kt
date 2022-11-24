package com.example.sns_watch_project

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.sns_watch_project.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : Activity() {
    private lateinit var binding: ActivityMainBinding
    val uid = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val recyclerView: RecyclerView = binding.root.findViewById(R.id.recyclerview)
        setContentView(binding.root)

        val adapter = MyAdapter()
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    @SuppressLint("NotifyDataSetChanged")
    inner class MyAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        private var contentDTOs : ArrayList<ContentDTO> = arrayListOf()
        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

        private val firestore = FirebaseFirestore.getInstance()

        init {
            firestore.collection("following").document(uid!!).addSnapshotListener { documentSnapshot, _ ->
                if (documentSnapshot == null) return@addSnapshotListener
                val followDTO = documentSnapshot.toObject(FollowDTO::class.java)
                for (key in followDTO?.followings!!.keys) {
                    firestore.collection("images").whereEqualTo("uid", key).addSnapshotListener { querySnapshot, _ ->
                        if (querySnapshot == null) return@addSnapshotListener
                        for (snapshot in querySnapshot.documents) {
                            contentDTOs.add(snapshot.toObject(ContentDTO::class.java)!!)
                        }
                        notifyDataSetChanged()
                    }
                }
                notifyDataSetChanged()
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.post_item, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val postings = contentDTOs[position]
            val dpi = resources.displayMetrics.densityDpi
            val imageView = (holder.itemView.findViewById(R.id.post_image) as ImageView)
            imageView.layoutParams = LinearLayout.LayoutParams(dpi, dpi)
            Glide.with(holder.itemView.context).load(contentDTOs[position].imageUrl).apply(
                RequestOptions().centerCrop()).into(imageView)
            holder.itemView.findViewById<TextView>(R.id.username).text = postings.userId
            holder.itemView.findViewById<TextView>(R.id.publisher).text = postings.userId
            holder.itemView.findViewById<TextView>(R.id.description).text = postings.explain
        }

        override fun getItemCount(): Int {
            return contentDTOs.size
        }
    }
}