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

class MediaAdapter : ListAdapter<Media, MediaAdapter.MediaViewHolder>(MediaDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_media_add, parent, false)
        return MediaViewHolder(view)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        val media = getItem(position)
        holder.bind(media)
    }

    class MediaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.imageViewMedia)
        private val videoView: VideoView = itemView.findViewById(R.id.videoViewMedia)

        fun bind(media: Media) {
            val mediaFile = File(media.ruta) // Asume que media.ruta es la ruta completa del archivo
            if (!mediaFile.exists()) {
                // Maneja el caso donde el archivo no existe
                Log.e("MediaAdapter", "El archivo no existe: ${mediaFile.absolutePath}")
                imageView.visibility = View.GONE
                videoView.visibility = View.GONE
                return
            }

            when (media.tipo) {
                MediaTipo.IMAGE, MediaTipo.IMAGE_SEQUENCE, MediaTipo.GIF -> {
                    imageView.visibility = View.VISIBLE
                    videoView.visibility = View.GONE

                    Log.d("MediaAdapter", "Cargando imagen desde: ${mediaFile.absolutePath}")
                    Glide.with(imageView.context)
                        .load(mediaFile)
                        .error(R.drawable.ic_broken_image) // Opcional: agregar un placeholder de error
                        .into(imageView)
                }
                MediaTipo.VIDEO -> {
                    imageView.visibility = View.GONE
                    videoView.visibility = View.VISIBLE

                    Log.d("MediaAdapter", "Cargando video desde: ${mediaFile.absolutePath}")
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
