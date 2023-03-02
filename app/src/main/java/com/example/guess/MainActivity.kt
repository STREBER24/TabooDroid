package com.example.guess

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
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

        val timer = getTimer()

        showRandomTask(taskFile)
        binding.nextButton.setOnClickListener {
            showRandomTask(taskFile)
        }
    }

    private fun getTimer(): CountDownTimer {
        return object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.timerText.text = (millisUntilFinished / 1000).toString()
            }

            override fun onFinish() {
                binding.timerText.text = 0.toString()
            }
        }
    }

    private fun showRandomTask(tasks: List<GuessTask>) {
        val chosenTask = tasks.randomOrNull()
        if (chosenTask == null) {
            binding.primaryText.text = ""
            showSecondaryTexts(emptyList())
        } else {
            binding.primaryText.text = chosenTask.guessWord.uppercase()
            showSecondaryTexts(chosenTask.blockedWords.shuffled().subList(0, 5))
        }
    }

    private fun showSecondaryTexts(list: List<String>) {
        binding.secondaryText1.text = list.elementAtOrNull(0)
        binding.secondaryText2.text = list.elementAtOrNull(1)
        binding.secondaryText3.text = list.elementAtOrNull(2)
        binding.secondaryText4.text = list.elementAtOrNull(3)
        binding.secondaryText5.text = list.elementAtOrNull(4)
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

class GuessTask(val guessWord: String, blockedWords: List<String>) {
    val blockedWords = blockedWords.map { a -> a.uppercase() }
}