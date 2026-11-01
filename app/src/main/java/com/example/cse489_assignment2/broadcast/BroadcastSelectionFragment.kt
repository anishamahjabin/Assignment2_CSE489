package com.example.cse489_assignment2.broadcast

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.example.cse489_assignment2.MainActivity
import com.example.cse489_assignment2.R
import com.example.cse489_assignment2.databinding.FragmentBroadcastSelectionBinding

class BroadcastSelectionFragment : Fragment() {

    private var _binding: FragmentBroadcastSelectionBinding? = null
    private val binding get() = checkNotNull(_binding)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBroadcastSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val choices = resources.getStringArray(R.array.broadcast_receiver_options)
        binding.receiverSpinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            choices
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        binding.proceedButton.setOnClickListener {
            val receiverType = if (binding.receiverSpinner.selectedItemPosition == 0) {
                ReceiverType.CUSTOM
            } else {
                ReceiverType.BATTERY
            }
            (requireActivity() as MainActivity).navigateTo(
                BroadcastDataFragment.newInstance(receiverType)
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
