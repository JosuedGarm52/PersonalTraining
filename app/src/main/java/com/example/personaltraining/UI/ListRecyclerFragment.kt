package com.example.personaltraining.UI

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.personaltraining.R
import com.example.personaltraining.adapter.RutinasAdapter
import com.example.personaltraining.application.RutinasApplication
import com.example.personaltraining.databinding.ListRecyclerFragmentBinding
import com.example.personaltraining.model.Rutina
import com.example.personaltraining.viewModel.ListRecyclerFragmentViewModelFactory
import com.example.personaltraining.repository.RutinasRepository
import com.example.personaltraining.viewModel.ListRecyclerFragmentViewModel

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class ListRecyclerFragment : Fragment() {

    private var _binding: ListRecyclerFragmentBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    val listRecyclerFragmentViewModel: ListRecyclerFragmentViewModel by viewModels {
        ListRecyclerFragmentViewModelFactory((requireActivity().application as RutinasApplication).repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = ListRecyclerFragmentBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /*
        binding.buttonFirst.setOnClickListener {
            findNavController().navigate(R.id.action_ListRecyclerFragment_to_AddEjerFragment)
        }*/

        binding.btnAddRutina.setOnClickListener{
            findNavController().navigate(R.id.action_ListRecyclerFragment_to_AddEjerFragment)
        }
        val adapter = RutinasAdapter(
            clickt = { rutina -> onItemClick(rutina) },
            deletet = { rutina -> onItemDelete(rutina) },
            editt = { rutina -> onItemEdit(rutina) }
        )

        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())

        try {
            listRecyclerFragmentViewModel.rutinasKardex.observe(viewLifecycleOwner, Observer { rutinas ->
                rutinas?.let {
                    adapter.submitList(it)
                }
            })
        } catch (e: Exception) {
            Log.e("ListRecyclerFragment", "Error al observar rutinasKardex: ${e.message}", e)
        }

    }

    private fun onItemClick(it: Rutina) {
        Log.d("FirstFragment", "onItem clic")
        Toast.makeText(requireContext(), "Clic a ${it.nombre}", Toast.LENGTH_SHORT).show()

        //val action = ListRecyclerFragmentDirections.actionFirstFragmentToSecondFragment(it.ID)
        //findNavController().navigate(action)
    }

    private fun onItemDelete(rutina: Rutina) {
        Log.d("ListRecyclerFragment", "onItem delete")
        Toast.makeText(requireContext(), "Eliminar ${rutina.nombre}", Toast.LENGTH_SHORT).show()

        listRecyclerFragmentViewModel.deleteRutina(rutina.ID)
    }

    private fun onItemEdit(rutina: Rutina) {
        Log.d("ListRecyclerFragment", "onItem edit")
        Toast.makeText(requireContext(), "Editar ${rutina.nombre}", Toast.LENGTH_SHORT).show()
        // Implementa lógica para editar la rutina
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}