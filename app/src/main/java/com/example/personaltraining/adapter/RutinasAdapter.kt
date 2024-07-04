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
import android.widget.Button
import android.widget.LinearLayout
import com.chauthai.swipereveallayout.SwipeRevealLayout

class RutinasAdapter(
    private val clickt: (Rutina) -> Unit,
    private val deletet: (Rutina) -> Unit,
    private val editt: (Rutina) -> Unit) :
    ListAdapter<Rutina, RutinasAdapter.ViewHolder>(RutinaComparator()) {

    class ViewHolder(
        item: View,
        val clickt: (Rutina) -> Unit,
        val deletet: (Rutina) -> Unit,
        val editt: (Rutina) -> Unit)
        : RecyclerView.ViewHolder(item) {
        val swipeRevealLayout: SwipeRevealLayout = item.findViewById(R.id.swipeRevealLayout)
        val tvTitulo: TextView = item.findViewById(R.id.tvTitulo)
        val tvDuracionEjer: TextView = item.findViewById(R.id.tvDuracionEjer)
        val tvFecha: TextView = item.findViewById(R.id.tvFechaCreacion)
        val btnDelete: LinearLayout = item.findViewById(R.id.catalogDelete)
        val btnEditar: LinearLayout = item.findViewById(R.id.catalogEdit)

        fun bind(rutina: Rutina) {
            tvTitulo.text = "Rutina N°: " + rutina.ID.toString() + " " + rutina.nombre
            tvDuracionEjer.text = ""
            tvFecha.text = "Fecha de creacion: "+ rutina.fechaCreacion

            itemView.setOnClickListener {
                clickt(rutina)
            }

            btnDelete.setOnClickListener {
                deletet(rutina)
            }

            btnEditar.setOnClickListener {
                editt(rutina)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val item = LayoutInflater.from(parent.context).inflate(R.layout.item_rutina, parent, false)
        return ViewHolder(item, clickt, deletet, editt)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val rutina = getItem(position)
        holder.bind(rutina)
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
