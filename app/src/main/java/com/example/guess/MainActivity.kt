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
        Toast.makeText(
            this,
            "Loaded file with ${taskFile.size.toString()} tasks.",
            Toast.LENGTH_SHORT
        ).show()

        showRandomTask(taskFile)
        binding.nextButton.setOnClickListener { showRandomTask(taskFile) }
    }

    private fun showRandomTask(tasks: List<GuessTask>) {
        val chosenTask = tasks.randomOrNull()
        if (chosenTask == null) {
            binding.primaryText.text = ""
            binding.secondaryText1.text = ""
            binding.secondaryText2.text = ""
            binding.secondaryText3.text = ""
            binding.secondaryText4.text = ""
            binding.secondaryText5.text = ""
            return
        }
        binding.primaryText.text = chosenTask.guessWord
        binding.secondaryText1.text = chosenTask.blockedWords[0]
        binding.secondaryText2.text = chosenTask.blockedWords[1]
        binding.secondaryText3.text = chosenTask.blockedWords[2]
        binding.secondaryText4.text = chosenTask.blockedWords[3]
        binding.secondaryText5.text = chosenTask.blockedWords[4]
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
                GuessTask(line[0], line.subList(1, line.size))
            }.toList()
    }
}

class GuessTask(val guessWord: String, val blockedWords: List<String>)