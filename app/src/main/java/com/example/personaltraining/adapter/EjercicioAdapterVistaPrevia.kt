package com.example.personaltraining.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.personaltraining.R
import com.example.personaltraining.model.Ejercicio
import com.example.personaltraining.databinding.EjercicioItemVistapreviaBinding

class EjercicioAdapterVistaPrevia (
        private val onItemClicked: (Ejercicio) -> Unit
    ): ListAdapter<Ejercicio, EjercicioAdapterVistaPrevia.EjercicioViewHolder>(EjercicioDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EjercicioViewHolder {
        val binding = EjercicioItemVistapreviaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EjercicioViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EjercicioViewHolder, position: Int) {
        val ejercicio = getItem(position)
        holder.bind(ejercicio)
    }

    inner class EjercicioViewHolder(private val binding: EjercicioItemVistapreviaBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(ejercicio: Ejercicio) {
            binding.tvNombre.text = ejercicio.Nombre
            binding.tvDEjercicio.text = "Ejercicio: "+ejercicio.DEjercicio
            binding.tvDDescanso.text = "Descanso: "+ejercicio.DDescanso
            if (ejercicio.isObjetivo){
                binding.tvObjetivo.visibility = View.VISIBLE
                binding.tvObjetivo.text = "Objetivo: "+ ejercicio.Objetivo
            }else{
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
