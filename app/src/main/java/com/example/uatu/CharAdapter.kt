package com.example.uatu

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class CharAdapter(private val charList: List<String>) : RecyclerView.Adapter<CharAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val charImage: ImageView

        init {
            // Find our RecyclerView item's ImageView for future use
            charImage = view.findViewById(R.id.char_image)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.char_item, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount() = charList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Glide.with(holder.itemView)
            .load(charList[position])
            .centerCrop()
            .into(holder.charImage)
    }
}