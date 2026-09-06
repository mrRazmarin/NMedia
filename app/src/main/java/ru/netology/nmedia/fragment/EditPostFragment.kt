package ru.netology.nmedia.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.databinding.FragmentEditPostBinding
import ru.netology.nmedia.viewmodel.PostViewModel

class EditPostFragment : Fragment() {

    private val viewModel: PostViewModel by viewModels(ownerProducer = ::requireParentFragment)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        var clicked = false
        val binding = FragmentEditPostBinding.inflate(
            inflater,
            container,
            false
        )

        viewModel.edited.observe(viewLifecycleOwner) { post ->
            binding.edit.setText(post.content)
        }

        binding.fabMenu.setOnClickListener {
            clicked = !clicked
            if (clicked) {
                binding.groupBtns.visibility = View.VISIBLE
            } else{
                binding.groupBtns.visibility = View.GONE
            }
        }

        binding.fabConfirm.setOnClickListener {
            viewModel.save(binding.edit.text.toString())

        }
        viewModel.saveFinished.observe(viewLifecycleOwner) {
            findNavController().navigateUp()
        }

        binding.fabCancel.setOnClickListener {
            viewModel.editToEmpty()
            findNavController().navigateUp()
        }

        return binding.root
    }
}