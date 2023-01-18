package com.example.image.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.content.ContextCompat
import androidx.core.view.drawToBitmap
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import coil.load
import com.example.image.R
import com.example.image.databinding.FragmentMainBinding
import com.example.image.models.ImageViewModel
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.lang.Thread.sleep


class MainFragment : Fragment() {

    lateinit var binding:FragmentMainBinding
    private val viewModel:ImageViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =  FragmentMainBinding.inflate(inflater,container, false)
        val savedUrl = viewModel.getImageUrl()
        if(savedUrl!="")
        {
            loadImage()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.goButton.setOnClickListener{
            var url =  binding.textInput.text.toString()
            if(url!=""){
                viewModel.setImageUrl(url)
                loadImage()
            }
        }

        binding.downloadButton.setOnClickListener{
            lifecycleScope.launch(Dispatchers.IO) {
                viewModel.downloadImage(requireContext(), viewModel.getImageUrl(),"image.jpg")
            }
        }

    }
    fun loadImage(){
        binding.image.visibility = View.VISIBLE
        binding.downloadButton.visibility = View.VISIBLE
        binding.image.load(viewModel.getImageUrl()){
            placeholder(R.drawable.loading_img)
            error(R.drawable.ic_connection_error)
        }
    }

}