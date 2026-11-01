package com.example.cse489_assignment2.features

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.example.cse489_assignment2.R
import com.example.cse489_assignment2.databinding.FragmentImageScaleBinding
import java.net.URL
import java.util.concurrent.Executors
import javax.net.ssl.HttpsURLConnection

class ImageScaleFragment : Fragment() {

    private var _binding: FragmentImageScaleBinding? = null
    private val binding get() = checkNotNull(_binding)
    private var scaleFactor = 1f
    private val imageExecutor = Executors.newSingleThreadExecutor()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentImageScaleBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        scaleFactor = savedInstanceState?.getFloat(KEY_SCALE, 1f) ?: 1f
        applyScale()

        val scaleDetector = ScaleGestureDetector(
            requireContext(),
            object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                override fun onScale(detector: ScaleGestureDetector): Boolean {
                    scaleFactor = (scaleFactor * detector.scaleFactor).coerceIn(MIN_SCALE, MAX_SCALE)
                    applyScale()
                    binding.scaleValueText.text = getString(R.string.scale_value, scaleFactor)
                    return true
                }
            }
        )

        binding.zoomImage.setOnTouchListener { _, event ->
            scaleDetector.onTouchEvent(event)
            true
        }

        binding.resetZoomButton.setOnClickListener {
            scaleFactor = 1f
            applyScale()
            binding.scaleValueText.text = getString(R.string.scale_value, scaleFactor)
        }

        binding.scaleValueText.text = getString(R.string.scale_value, scaleFactor)
        binding.zoomImage.setImageResource(R.drawable.image_placeholder)
        loadRemoteImage()
    }

    private fun loadRemoteImage() {
        imageExecutor.execute {
            var connection: HttpsURLConnection? = null
            try {
                connection = URL(IMAGE_URL).openConnection() as HttpsURLConnection
                connection.connectTimeout = 10_000
                connection.readTimeout = 15_000
                connection.doInput = true
                connection.connect()
                check(connection.responseCode in 200..299)
                val bitmap = connection.inputStream.use(BitmapFactory::decodeStream)
                    ?: error("The server returned an invalid image")
                activity?.runOnUiThread {
                    _binding?.imageProgress?.isVisible = false
                    _binding?.imageStatusText?.isVisible = false
                    _binding?.zoomImage?.setImageBitmap(bitmap)
                }
            } catch (_: Exception) {
                activity?.runOnUiThread {
                    _binding?.imageProgress?.isVisible = false
                    _binding?.imageStatusText?.isVisible = true
                    _binding?.zoomImage?.setImageResource(R.drawable.image_error)
                }
            } finally {
                connection?.disconnect()
            }
        }
    }

    private fun applyScale() {
        _binding?.zoomImage?.scaleX = scaleFactor
        _binding?.zoomImage?.scaleY = scaleFactor
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putFloat(KEY_SCALE, scaleFactor)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        imageExecutor.shutdownNow()
        super.onDestroy()
    }

    private companion object {
        const val IMAGE_URL = "https://picsum.photos/id/237/1200/800"
        const val KEY_SCALE = "image_scale"
        const val MIN_SCALE = 0.75f
        const val MAX_SCALE = 5f
    }
}
