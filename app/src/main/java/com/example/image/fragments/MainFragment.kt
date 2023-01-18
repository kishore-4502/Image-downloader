package com.example.image.fragments

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import coil.load
import com.example.image.R
import com.example.image.databinding.FragmentMainBinding
import com.example.image.models.ImageViewModel
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL

private var job: Job? = null

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
                Log.d("main",url)
                viewModel.setImageUrl(url)
                loadImage()
            }
        }

        binding.downloadButton.setOnClickListener{
                if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.Q){
                    lifecycleScope.launch(Dispatchers.IO){
                        viewModel.downloadImage1(requireContext(), viewModel.getImageUrl(),"image.jpg")
                    }
                }else{
                    val imageUrl = viewModel.getImageUrl()
                    lifecycleScope.launch(Dispatchers.IO) {
                       viewModel.downloadImage2(requireContext(),imageUrl,"image.jpg")
                    }

                }
        }

    }
    private fun loadImage(){
        binding.image.visibility = View.VISIBLE
        binding.downloadButton.visibility = View.VISIBLE
        binding.image.load(viewModel.getImageUrl())
        {
            placeholder(R.drawable.loading_img)
            error(R.drawable.ic_connection_error)
        }
    }


}

