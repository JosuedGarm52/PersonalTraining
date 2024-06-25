package com.example.personaltraining.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.personaltraining.R
import com.example.personaltraining.model.Rutina
import android.view.View

class RutinasAdapter(private val xyz: (Rutina) -> Unit) : ListAdapter<Rutina, RutinasAdapter.ViewHolder>(RutinaComparator()){
    class ViewHolder(item: View, val xyz: (Rutina) -> Unit) : RecyclerView.ViewHolder(item) {
        val tvTitulo = item.findViewById<TextView>(R.id.tvTitulo)
        val tvDuracionEjer = item.findViewById<TextView>(R.id.tvDuracionEjer)

        fun bind(rutina: Rutina){
            tvTitulo.text = "Rutina N°: "+rutina.ID.toString() + " " + rutina.nombre
            tvDuracionEjer.text = ""

            itemView.setOnClickListener{
                xyz(rutina)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val item = LayoutInflater.from(parent.context).inflate(R.layout.ejercicio_item,parent,false)
        return ViewHolder(item,xyz)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val camion = getItem(position)
        holder.bind(camion)
    }
    class RutinaComparator : DiffUtil.ItemCallback<Rutina>() {
        override fun areItemsTheSame(oldItem: Rutina, newItem: Rutina): Boolean {
            return oldItem === newItem
        }
        override fun areContentsTheSame(oldItem: Rutina, newItem: Rutina): Boolean {
            return oldItem.ID == newItem.ID
        }
    }
}