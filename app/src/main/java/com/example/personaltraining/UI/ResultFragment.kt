package com.example.personaltraining.UI

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.personaltraining.R
import com.example.personaltraining.databinding.ResultFragmentBinding
import com.example.personaltraining.viewModel.NavigationListener
import com.example.personaltraining.viewModel.ResultViewModel


/**
 * A simple [Fragment] subclass.
 * Use the [ResultFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ResultFragment : Fragment() {

    private var _binding: ResultFragmentBinding? = null
    private val binding get() = _binding!!

    private val args: ResultFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ResultFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val elapsedTimeInMillis = args.elapsedTimeMillis
        val formattedTime = formatTime(elapsedTimeInMillis)
        binding.textViewElapsedTime.text = formattedTime

        binding.btnReturn.setOnClickListener {
            val action = ResultFragmentDirections.actionResultFragmentToListRecyclerFragment()
            findNavController().navigate(action)
        }
    }

    private fun formatTime(milliseconds: Long): String {
        val seconds = (milliseconds / 1000) % 60
        val minutes = (milliseconds / (1000 * 60)) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}