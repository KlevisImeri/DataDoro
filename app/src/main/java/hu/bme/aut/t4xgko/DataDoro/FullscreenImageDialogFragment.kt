package hu.bme.aut.t4xgko.DataDoro

import android.app.Dialog
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import hu.bme.aut.t4xgko.DataDoro.databinding.DialogFullscreenImageBinding

class FullscreenImageDialogFragment : DialogFragment() {

    companion object {
        private const val ARG_IMAGE_PATH = "image_path"

        fun newInstance(imagePath: String): FullscreenImageDialogFragment {
            val args = Bundle().apply {
                putString(ARG_IMAGE_PATH, imagePath)
            }
            return FullscreenImageDialogFragment().apply {
                arguments = args
            }
        }
    }

    private var _binding: DialogFullscreenImageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DialogFullscreenImageBinding.inflate(inflater, container, false)
      return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val imagePath = arguments?.getString(ARG_IMAGE_PATH)
        imagePath?.let {
            val bitmap = BitmapFactory.decodeFile(it)
            binding.fullscreenImageView.setImageBitmap(bitmap)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return Dialog(requireContext(), android.R.style.Theme_Translucent_NoTitleBar).apply {
            window?.setBackgroundDrawableResource(R.drawable.semi_transparent_black)
        }
    }

}
