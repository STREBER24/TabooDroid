package com.example.guess

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.guess.databinding.ActivityAboutBinding

class AboutActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAboutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val htmlText = resources.openRawResource(R.raw.about_page).bufferedReader().readText()
        binding.htmlViewer.loadData(htmlText, "text/html", "utf8")
    }
}