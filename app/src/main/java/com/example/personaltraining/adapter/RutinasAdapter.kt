package com.example.personaltraining.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.personaltraining.R
import com.example.personaltraining.model.Rutina
import android.view.View
import android.widget.LinearLayout
import com.chauthai.swipereveallayout.SwipeRevealLayout
import com.example.personaltraining.model.Ejercicio

class RutinasAdapter(
    private val clickt: (Rutina) -> Unit,
    private val deletet: (Rutina) -> Unit,
    private val editt: (Rutina) -> Unit,
    private var listEjercicios: List<Ejercicio>?
) : ListAdapter<Rutina, RutinasAdapter.ViewHolder>(RutinaComparator()) {

    fun updateEjercicios(nuevosEjercicios: List<Ejercicio>?) {
        listEjercicios = nuevosEjercicios
        notifyDataSetChanged() // Notificar al adaptador que los datos han cambiado
    }

    class ViewHolder(
        item: View,
        val clickt: (Rutina) -> Unit,
        val deletet: (Rutina) -> Unit,
        val editt: (Rutina) -> Unit)
        : RecyclerView.ViewHolder(item) {
        private lateinit var rutina: Rutina
        //val swipeRevealLayout: SwipeRevealLayout = item.findViewById(R.id.swipeRevealLayout)
        val tvTitulo: TextView = item.findViewById(R.id.tvTitulo)
        val tvDuracionEjer: TextView = item.findViewById(R.id.tvDuracionEjer)
        val tvFecha: TextView = item.findViewById(R.id.tvFechaCreacion)
        val btnIniciar : LinearLayout = item.findViewById(R.id.catalogIniciar)
        val btnDelete: LinearLayout = item.findViewById(R.id.catalogDelete)
        val btnEditar: LinearLayout = item.findViewById(R.id.catalogEdit)

        init {

            btnIniciar.setOnClickListener {
                if (::rutina.isInitialized) {
                    clickt(rutina)
                }
            }

            btnDelete.setOnClickListener {
                if (::rutina.isInitialized) {
                    deletet(rutina)
                }
            }

            btnEditar.setOnClickListener {
                if (::rutina.isInitialized) {
                    editt(rutina)
                }
            }
        }

        fun bind(rutina: Rutina, listEjercicios : List<Ejercicio>?) {
            this.rutina = rutina

            tvTitulo.text = "Rutina N°: ${rutina.ID} ${rutina.nombre}"
            tvFecha.text = "Fecha de creación: ${rutina.fechaCreacion}"

            if (listEjercicios.isNullOrEmpty()) {
                tvDuracionEjer.text = "No hay ejercicios disponibles"
            } else {
                val ejerciciosDeRutina = listEjercicios.filter { it.rutinaId == rutina.ID }
                val tieneObjetivo = ejerciciosDeRutina.any { it.isObjetivo || it.Objetivo != null }

                val totalTimeInSeconds = ejerciciosDeRutina.sumOf {
                    convertToSeconds(it.DEjercicio) + convertToSeconds(it.DDescanso)
                }

                val totalTimeFormatted = String.format("%02d:%02d", totalTimeInSeconds / 60, totalTimeInSeconds % 60)
                val titulo = if (tieneObjetivo) "Tiempo estimado: " else "Tiempo: "
                val text = "$titulo$totalTimeFormatted"

                tvDuracionEjer.text = text
            }
        }
        fun convertToSeconds(time: String): Int {
            val parts = time.split(":")
            val minutes = parts[0].toInt()
            val seconds = parts[1].toInt()
            return minutes * 60 + seconds
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val item = LayoutInflater.from(parent.context).inflate(R.layout.item_rutina, parent, false)
        return ViewHolder(item, clickt, deletet, editt)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val rutina = getItem(position)
        holder.bind(rutina, listEjercicios)
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
