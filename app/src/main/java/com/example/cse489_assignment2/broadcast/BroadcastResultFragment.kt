package com.example.cse489_assignment2.broadcast

import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.example.cse489_assignment2.MainActivity
import com.example.cse489_assignment2.R
import com.example.cse489_assignment2.databinding.FragmentBroadcastResultBinding

class BroadcastResultFragment : Fragment() {

    private var _binding: FragmentBroadcastResultBinding? = null
    private val binding get() = checkNotNull(_binding)
    private lateinit var receiverType: ReceiverType
    private var customReceiver: CustomMessageReceiver? = null
    private var receiverRegistered = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        receiverType = runCatching {
            ReceiverType.valueOf(
                requireArguments().getString(BroadcastContract.ARG_RECEIVER_TYPE).orEmpty()
            )
        }.getOrDefault(ReceiverType.CUSTOM)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBroadcastResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val isCustom = receiverType == ReceiverType.CUSTOM
        binding.customResultGroup.isVisible = isCustom
        binding.batteryResultGroup.isVisible = !isCustom
        binding.restartButton.setOnClickListener {
            (requireActivity() as MainActivity).restartBroadcastFlow()
        }
    }

    override fun onStart() {
        super.onStart()
        if (receiverType != ReceiverType.CUSTOM) return

        val receiver = CustomMessageReceiver { message ->
            _binding?.receivedMessageText?.text = message.ifBlank {
                getString(R.string.empty_message_received)
            }
        }
        customReceiver = receiver
        ContextCompat.registerReceiver(
            requireContext(),
            receiver,
            IntentFilter(BroadcastContract.ACTION_CUSTOM_MESSAGE),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        receiverRegistered = true

        val message = requireArguments().getString(BroadcastContract.ARG_MESSAGE).orEmpty()
        requireContext().sendBroadcast(
            Intent(BroadcastContract.ACTION_CUSTOM_MESSAGE)
                .setPackage(requireContext().packageName)
                .putExtra(BroadcastContract.EXTRA_MESSAGE, message)
        )
    }

    override fun onStop() {
        if (receiverRegistered) {
            customReceiver?.let { requireContext().unregisterReceiver(it) }
            receiverRegistered = false
            customReceiver = null
        }
        super.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(receiverType: ReceiverType, message: String = "") =
            BroadcastResultFragment().apply {
                arguments = Bundle().apply {
                    putString(BroadcastContract.ARG_RECEIVER_TYPE, receiverType.name)
                    putString(BroadcastContract.ARG_MESSAGE, message)
                }
            }
    }

}
