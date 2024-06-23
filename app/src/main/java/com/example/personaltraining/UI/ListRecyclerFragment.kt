package com.example.personaltraining.UI

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.personaltraining.R
import com.example.personaltraining.databinding.ListRecyclerFragmentBinding

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class ListRecyclerFragment : Fragment() {

    private var _binding: ListRecyclerFragmentBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}