package com.storyteller_f.hu

import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.storyteller_f.hu.databinding.ActivityMainBinding
import com.storyteller_f.hu_library.HuState


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val huState by lazy {
        HuState(
            this,
            this,
            binding.content,
            binding.panelContainer,
            binding.inputBox
        )
    }
    private val selectImage = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) {
        Toast.makeText(this, it.toString(), Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val inflate = ActivityMainBinding.inflate(layoutInflater)
        setContentView(inflate.root)
        binding = inflate
        WindowCompat.setDecorFitsSystemWindows(window, false)
        huState
        setupPanelSwitch()
        binding.selectImage.setOnClickListener {
            selectImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }


    private fun setupPanelSwitch() {
        val list = listOf(binding.more)
        list.forEachIndexed { index, imageView ->
            imageView.setOnClickListener {
                huState.switchPanel(index)
            }
        }

        huState.panelState.observe(this) { currentPanel: com.storyteller_f.hu_library.HuPanel? ->
            list.forEachIndexed { index, imageView ->
                imageView.isSelected =
                    currentPanel is com.storyteller_f.hu_library.HuPanel.Panel && currentPanel.index == index
            }
        }
    }


}

