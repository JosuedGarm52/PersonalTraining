package com.example.personaltraining.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.personaltraining.R
import com.example.personaltraining.model.Media
import com.example.personaltraining.model.MediaTipo

class MediaAddAdapter(private val mediaList: List<Media>) :
    RecyclerView.Adapter<MediaAddAdapter.MediaViewHolder>() {

    inner class MediaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageViewMedia)
        // Agrega más vistas si es necesario
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_media_add, parent, false)
        return MediaViewHolder(view)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        val media = mediaList[position]
        when (media.tipo) {
            MediaTipo.IMAGE, MediaTipo.IMAGE_SEQUENCE, MediaTipo.GIF -> {
                // Cargar la imagen o GIF en el ImageView (usando una biblioteca como Glide o Picasso)
                Glide.with(holder.imageView.context)
                    .load(media.ruta)
                    .into(holder.imageView)
            }
            MediaTipo.VIDEO -> {
                // Aquí puedes configurar una vista de video si lo necesitas
            }
        }
    }

    override fun getItemCount(): Int = mediaList.size
}