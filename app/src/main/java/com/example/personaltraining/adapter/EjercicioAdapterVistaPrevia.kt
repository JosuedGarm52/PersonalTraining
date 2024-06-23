package com.example.personaltraining.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.personaltraining.R
import com.example.personaltraining.model.Ejercicio

class EjercicioAdapterVistaPrevia(private val ejercicioList: List<Ejercicio>) : RecyclerView.Adapter<EjercicioAdapterVistaPrevia.EjercicioViewHolder>() {

    class EjercicioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombre: TextView = itemView.findViewById(R.id.tvNombre)
        val tvDEjercicio: TextView = itemView.findViewById(R.id.tvDEjercicio)
        val tvDDescanso: TextView = itemView.findViewById(R.id.tvDDescanso)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EjercicioViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.ejercicio_item_vistaprevia, parent, false)
        return EjercicioViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: EjercicioViewHolder, position: Int) {
        val currentEjercicio = ejercicioList[position]
        holder.tvNombre.text = currentEjercicio.Nombre
        holder.tvDEjercicio.text = currentEjercicio.DEjercicio
        holder.tvDDescanso.text = currentEjercicio.DDescanso
    }

    override fun getItemCount() = ejercicioList.size
}