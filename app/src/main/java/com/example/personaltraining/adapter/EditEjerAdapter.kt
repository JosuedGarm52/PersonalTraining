package com.example.personaltraining.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.personaltraining.databinding.EjercicioItemVistapreviaBinding
import com.example.personaltraining.model.Ejercicio

class EditEjerAdapter(
    private val onItemClicked: (Ejercicio) -> Unit
) : ListAdapter<Ejercicio, EditEjerAdapter.EditEjerViewHolder>(EjercicioDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EditEjerViewHolder {
        val binding = EjercicioItemVistapreviaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EditEjerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EditEjerViewHolder, position: Int) {
        val ejercicio = getItem(position)
        holder.bind(ejercicio)
    }

    inner class EditEjerViewHolder(private val binding: EjercicioItemVistapreviaBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(ejercicio: Ejercicio) {
            binding.tvNombre.text = ejercicio.Nombre
            binding.tvDEjercicio.text = "Ejercicio: " + ejercicio.DEjercicio
            binding.tvDDescanso.text = "Descanso: " + ejercicio.DDescanso
            if (ejercicio.isObjetivo) {
                binding.tvObjetivo.visibility = View.VISIBLE
                binding.tvObjetivo.text = "Objetivo: " + ejercicio.Objetivo
            } else {
                binding.tvObjetivo.visibility = View.GONE
            }

            // Detectar clic en el elemento
            binding.root.setOnClickListener {
                onItemClicked(ejercicio)
            }
        }
    }

    private class EjercicioDiffCallback : DiffUtil.ItemCallback<Ejercicio>() {
        override fun areItemsTheSame(oldItem: Ejercicio, newItem: Ejercicio): Boolean {
            return oldItem.ID == newItem.ID
        }

        override fun areContentsTheSame(oldItem: Ejercicio, newItem: Ejercicio): Boolean {
            return oldItem == newItem
        }
    }
}
