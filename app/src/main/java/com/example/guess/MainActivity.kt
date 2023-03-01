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
            this, getString(R.string.loading_finished, taskFile.size),
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
        val blockedWords = chosenTask.blockedWords.shuffled()
        binding.primaryText.text = chosenTask.guessWord.uppercase()
        binding.secondaryText1.text = blockedWords[0].uppercase()
        binding.secondaryText2.text = blockedWords[1].uppercase()
        binding.secondaryText3.text = blockedWords[2].uppercase()
        binding.secondaryText4.text = blockedWords[3].uppercase()
        binding.secondaryText5.text = blockedWords[4].uppercase()
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