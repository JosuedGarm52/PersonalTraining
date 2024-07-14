package com.example.personaltraining.adapter

import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.VideoView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.personaltraining.R
import com.example.personaltraining.model.Media
import com.example.personaltraining.model.MediaTipo
import java.io.File

class MediaAdapter(
    private val onItemClick: (Media) -> Unit
) : ListAdapter<Media, MediaAdapter.MediaViewHolder>(MediaDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_media_add, parent, false)
        return MediaViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        val media = getItem(position)
        holder.bind(media)
    }

    class MediaViewHolder(
        itemView: View,
        private val onItemClick: (Media) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.imageViewMedia)
        private val videoView: VideoView = itemView.findViewById(R.id.videoViewMedia)

        fun bind(media: Media) {
            val mediaFile = File(media.ruta) // Asume que media.ruta es la ruta completa del archivo
            if (!mediaFile.exists()) {
                Log.e("MediaAdapter", "El archivo no existe: ${mediaFile.absolutePath}")
                imageView.visibility = View.GONE
                videoView.visibility = View.GONE
                return
            }

            itemView.setOnClickListener {
                onItemClick(media)
            }

            when (media.tipo) {
                MediaTipo.IMAGE, MediaTipo.IMAGE_SEQUENCE, MediaTipo.GIF -> {
                    imageView.visibility = View.VISIBLE
                    videoView.visibility = View.GONE

                    Glide.with(imageView.context)
                        .load(mediaFile)
                        .error(R.drawable.ic_broken_image)
                        .into(imageView)
                }
                MediaTipo.VIDEO -> {
                    imageView.visibility = View.GONE
                    videoView.visibility = View.VISIBLE
                    videoView.setVideoURI(Uri.fromFile(mediaFile))
                    videoView.start()
                }
            }
        }
    }
}


class MediaDiffCallback : DiffUtil.ItemCallback<Media>() {
    override fun areItemsTheSame(oldItem: Media, newItem: Media): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Media, newItem: Media): Boolean {
        return oldItem == newItem
    }
}
