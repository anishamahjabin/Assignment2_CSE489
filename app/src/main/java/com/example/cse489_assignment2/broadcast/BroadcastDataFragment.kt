package com.example.cse489_assignment2.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.example.cse489_assignment2.MainActivity
import com.example.cse489_assignment2.R
import com.example.cse489_assignment2.databinding.FragmentBroadcastDataBinding
import com.google.android.material.snackbar.Snackbar

class BroadcastDataFragment : Fragment() {

    private var _binding: FragmentBroadcastDataBinding? = null
    private val binding get() = checkNotNull(_binding)
    private lateinit var receiverType: ReceiverType
    private var batteryReceiverRegistered = false

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action != Intent.ACTION_BATTERY_CHANGED) return
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val percentage = if (level >= 0 && scale > 0) level * 100 / scale else null
            _binding?.batteryPercentageText?.text = percentage?.let { "$it%" }
                ?: getString(R.string.battery_unavailable)
        }
    }

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
        _binding = FragmentBroadcastDataBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val isCustom = receiverType == ReceiverType.CUSTOM
        binding.customInputGroup.isVisible = isCustom
        binding.batteryGroup.isVisible = !isCustom

        binding.sendMessageButton.setOnClickListener {
            val message = binding.messageInput.text?.toString()?.trim().orEmpty()
            if (message.isBlank()) {
                binding.messageInputLayout.error = getString(R.string.message_required)
                return@setOnClickListener
            }
            binding.messageInputLayout.error = null
            (requireActivity() as MainActivity).navigateTo(
                BroadcastResultFragment.newInstance(ReceiverType.CUSTOM, message)
            )
        }

        binding.continueBatteryButton.setOnClickListener {
            if (!batteryReceiverRegistered) {
                Snackbar.make(binding.root, R.string.waiting_for_battery, Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            (requireActivity() as MainActivity).navigateTo(
                BroadcastResultFragment.newInstance(ReceiverType.BATTERY)
            )
        }
    }

    override fun onStart() {
        super.onStart()
        if (receiverType == ReceiverType.BATTERY && !batteryReceiverRegistered) {
            ContextCompat.registerReceiver(
                requireContext(),
                batteryReceiver,
                IntentFilter(Intent.ACTION_BATTERY_CHANGED),
                ContextCompat.RECEIVER_EXPORTED
            )
            batteryReceiverRegistered = true
        }
    }

    override fun onStop() {
        if (batteryReceiverRegistered) {
            requireContext().unregisterReceiver(batteryReceiver)
            batteryReceiverRegistered = false
        }
        super.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(receiverType: ReceiverType) = BroadcastDataFragment().apply {
            arguments = Bundle().apply {
                putString(BroadcastContract.ARG_RECEIVER_TYPE, receiverType.name)
            }
        }
    }
}
