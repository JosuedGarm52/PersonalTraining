package com.example.personaltraining.adapter

import android.view.View
import com.example.personaltraining.R
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.VideoView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.personaltraining.model.Media
import com.example.personaltraining.model.MediaTipo
import java.io.File

class MediaPagerAdapter(private var mediaItems: List<MediaItem>) : RecyclerView.Adapter<MediaPagerAdapter.MediaViewHolder>() {

    // Actualiza la lista de MediaItems
    fun updateMediaItems(newMediaItems: List<MediaItem>) {
        mediaItems = newMediaItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_media, parent, false)
        return MediaViewHolder(view)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        val mediaItem = mediaItems[position]
        holder.bind(mediaItem)
    }

    override fun getItemCount(): Int = mediaItems.size

    class MediaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.imageViewMedia)
        private val videoView: VideoView = itemView.findViewById(R.id.videoViewMedia)

        fun bind(mediaItem: MediaItem) {
            when (mediaItem) {
                is MediaItem.Image -> {
                    imageView.visibility = View.VISIBLE
                    videoView.visibility = View.GONE
                    Glide.with(imageView.context)
                        .load(mediaItem.ruta)
                        .into(imageView)
                }
                is MediaItem.Video -> {
                    imageView.visibility = View.GONE
                    videoView.visibility = View.VISIBLE
                    videoView.setVideoURI(Uri.parse(mediaItem.ruta))
                    videoView.start()
                }
            }
        }
    }
}

sealed class MediaItem {
    data class Image(val ruta: String) : MediaItem()
    data class Video(val ruta: String) : MediaItem()
}

