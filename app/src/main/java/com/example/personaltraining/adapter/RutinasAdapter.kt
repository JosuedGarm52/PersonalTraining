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
        private lateinit var rutina: Rutina
        val swipeRevealLayout: SwipeRevealLayout = item.findViewById(R.id.swipeRevealLayout)
        val tvTitulo: TextView = item.findViewById(R.id.tvTitulo)
        val tvDuracionEjer: TextView = item.findViewById(R.id.tvDuracionEjer)
        val tvFecha: TextView = item.findViewById(R.id.tvFechaCreacion)
        val btnIniciar : LinearLayout = item.findViewById(R.id.catalogIniciar)
        val btnDelete: LinearLayout = item.findViewById(R.id.catalogDelete)
        val btnEditar: LinearLayout = item.findViewById(R.id.catalogEdit)

        init {
            /*
             // Configurar clic en el área extra
            btnClickArea.setOnClickListener {
                if (::rutina.isInitialized) {
                    clickt(rutina)
                }
            }

            // Configurar clic en el SwipeRevealLayout
            swipeRevealLayout.setSwipeListener(object : SwipeListener {
                override fun onClosed(view: SwipeRevealLayout?) {
                    // Manejar eventos al cerrar el SwipeRevealLayout si es necesario
                }

                override fun onOpened(view: SwipeRevealLayout?) {
                    // Manejar eventos al abrir el SwipeRevealLayout si es necesario
                }

                override fun onSlide(view: SwipeRevealLayout?, slideOffset: Float) {
                    // Manejar eventos de deslizamiento si es necesario
                }
            })



            swipeRevealLayout.setOnClickListener {
                if (::rutina.isInitialized) {
                    clickt(rutina) // Llamar al callback con la rutina actual
                }
            }

            itemView.setOnClickListener {
                if (::rutina.isInitialized) {
                    clickt(rutina)
                }
            }
            */

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

        fun bind(rutina: Rutina) {
            this.rutina = rutina

            tvTitulo.text = "Rutina N°: " + rutina.ID.toString() + " " + rutina.nombre
            tvDuracionEjer.text = ""
            tvFecha.text = "Fecha de creacion: "+ rutina.fechaCreacion

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
