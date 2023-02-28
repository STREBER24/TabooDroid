package com.example.guess

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.guess.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val taskFile = getTaskFile()
        Toast.makeText(this, "Loaded file with ${taskFile.size.toString()} tasks.", Toast.LENGTH_SHORT).show()
    }

    private fun getTaskFile(): List<GuessTask> {
        val allFiles = assets.list("taskFiles")
        if (allFiles.isNullOrEmpty()) {
            return emptyList()
        }
        val reader = assets.open("taskFiles/${allFiles[0]}").bufferedReader()
        return reader.lineSequence()
            .filter { it.isNotBlank() }
            .map {
                val line = it.split(',', ignoreCase = false)
                GuessTask(line[0], line.slice(1..5))
            }.toList()
    }
}

class GuessTask(val guessWord: String, val blockedWords: List<String>)