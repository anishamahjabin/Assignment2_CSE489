package com.example.cse489_assignment2.features

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.cse489_assignment2.R
import com.example.cse489_assignment2.databinding.FragmentAudioBinding
import java.io.IOException

class AudioFragment : Fragment() {

    private var _binding: FragmentAudioBinding? = null
    private val binding get() = checkNotNull(_binding)
    private var player: MediaPlayer? = null
    private var prepared = false
    private var stoppedByUser = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAudioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.playButton.setOnClickListener {
            if (prepared) {
                player?.start()
                binding.audioStatusText.setText(R.string.audio_playing)
                updateButtons()
            }
        }
        binding.pauseButton.setOnClickListener { pauseAudio() }
        binding.stopButton.setOnClickListener { stopAudio() }
        createAndPreparePlayer()
    }

    private fun createAndPreparePlayer() {
        releasePlayer()
        prepared = false
        updateButtons()
        binding.audioStatusText.setText(R.string.audio_loading)

        player = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            setOnPreparedListener {
                prepared = true
                binding.audioStatusText.setText(
                    if (stoppedByUser) R.string.audio_stopped else R.string.audio_ready
                )
                stoppedByUser = false
                updateButtons()
            }
            setOnCompletionListener {
                binding.audioStatusText.setText(R.string.audio_finished)
                updateButtons()
            }
            setOnErrorListener { _, _, _ ->
                prepared = false
                _binding?.audioStatusText?.setText(R.string.audio_error)
                updateButtons()
                true
            }
            try {
                // Plays the file bundled in app/src/main/res/raw/demo_audio.mp3
                requireContext().resources.openRawResourceFd(R.raw.demo_audio).use { afd ->
                    setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                }
                prepareAsync()
            } catch (e: IOException) {
                prepared = false
                binding.audioStatusText.setText(R.string.audio_error)
            }
        }
    }

    private fun pauseAudio() {
        val currentPlayer = player ?: return
        if (prepared && currentPlayer.isPlaying) {
            currentPlayer.pause()
            binding.audioStatusText.setText(R.string.audio_paused)
            updateButtons()
        }
    }

    private fun stopAudio() {
        val currentPlayer = player ?: return
        if (!prepared) return
        currentPlayer.stop()
        prepared = false
        stoppedByUser = true
        binding.audioStatusText.setText(R.string.audio_stopped)
        updateButtons()
        currentPlayer.prepareAsync()
    }

    private fun updateButtons() {
        val isPlaying = prepared && runCatching { player?.isPlaying == true }.getOrDefault(false)
        _binding?.playButton?.isEnabled = prepared && !isPlaying
        _binding?.pauseButton?.isEnabled = prepared && isPlaying
        _binding?.stopButton?.isEnabled = prepared
    }

    override fun onPause() {
        pauseAudio()
        super.onPause()
    }

    private fun releasePlayer() {
        player?.release()
        player = null
        prepared = false
    }

    override fun onDestroyView() {
        releasePlayer()
        super.onDestroyView()
        _binding = null
    }
}