package com.example.cse489_assignment2.features

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.example.cse489_assignment2.R
import com.example.cse489_assignment2.databinding.FragmentVideoBinding

class VideoFragment : Fragment() {

    private var _binding: FragmentVideoBinding? = null
    private val binding get() = checkNotNull(_binding)
    private var playbackPosition = 0
    private var resumePlayback = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        playbackPosition = savedInstanceState?.getInt(KEY_POSITION) ?: 0
        resumePlayback = savedInstanceState?.getBoolean(KEY_WAS_PLAYING) ?: true
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVideoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val controls = MediaController(requireContext()).apply {
            setAnchorView(binding.videoView)
        }
        binding.videoView.setMediaController(controls)
        val bundledVideo = Uri.parse(
            "android.resource://${requireContext().packageName}/${R.raw.demo_video}"
        )
        binding.videoView.setVideoURI(bundledVideo)
        binding.videoView.setOnPreparedListener { player ->
            _binding?.videoProgress?.isVisible = false
            player.isLooping = false
            if (playbackPosition > 0) binding.videoView.seekTo(playbackPosition)
            if (resumePlayback) binding.videoView.start()
        }
        binding.videoView.setOnErrorListener { _, _, _ ->
            _binding?.videoProgress?.isVisible = false
            _binding?.videoErrorText?.isVisible = true
            true
        }
        binding.videoView.setOnCompletionListener {
            resumePlayback = false
            playbackPosition = 0
        }
        binding.videoView.requestFocus()
    }

    override fun onPause() {
        _binding?.videoView?.let {
            playbackPosition = it.currentPosition
            resumePlayback = it.isPlaying
            it.pause()
        }
        super.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        _binding?.videoView?.let {
            playbackPosition = it.currentPosition
            resumePlayback = it.isPlaying
        }
        outState.putInt(KEY_POSITION, playbackPosition)
        outState.putBoolean(KEY_WAS_PLAYING, resumePlayback)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        _binding?.videoView?.stopPlayback()
        super.onDestroyView()
        _binding = null
    }

    private companion object {
        const val KEY_POSITION = "video_position"
        const val KEY_WAS_PLAYING = "video_was_playing"
    }
}
