package com.example.guess

import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Vibrator
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.guess.databinding.ActivityMainBinding

private const val TAG = "MainActivity"

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

        @Suppress("DEPRECATION")
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val timer = object : StatefulTimer() {
            override fun onTick(secondsUntilFinished: Int) {
                binding.timerText.text = secondsUntilFinished.toString()
            }

            override fun onFinished() {
                binding.nextButton.text = getString(R.string.start_timer)
                binding.timerText.text = ""
                binding.primaryText.text = ""
                showSecondaryTexts(emptyList())
                @Suppress("DEPRECATION")
                vibrator.vibrate(200)
            }
        }

        binding.nextButton.setOnClickListener {
            if (timer.getState() == StatefulTimer.States.STOPPED) {
                binding.nextButton.text = getString(R.string.next_button)
                timer.start()
            }
            showRandomTask(taskFile)
        }
    }

    abstract class StatefulTimer {
        enum class States { STOPPED, RUNNING }

        private var state = States.STOPPED
        abstract fun onTick(secondsUntilFinished: Int)
        abstract fun onFinished()
        private val timer = object : CountDownTimer(60000, 1000) {
            override fun onTick(p0: Long) {
                onTick((p0 / 1000).toInt())
            }

            override fun onFinish() {
                Log.i(TAG, "StatefulTimer finished")
                onFinished()
                state = States.STOPPED
            }
        }

        fun getState(): States {
            return state
        }

        fun start() {
            timer.start()
            state = States.RUNNING
            Log.i(TAG, "StatefulTimer started")
        }
    }

    private fun showRandomTask(tasks: List<GuessTask>) {
        val chosenTask = tasks.randomOrNull()
        if (chosenTask == null) {
            binding.primaryText.text = ""
            showSecondaryTexts(emptyList())
            Log.w(TAG, "failed to show next random task")
        } else {
            binding.primaryText.text = chosenTask.guessWord
            showSecondaryTexts(chosenTask.blockedWords.shuffled().subList(0, 5))
            Log.i(TAG, "show random word '${chosenTask.guessWord}'")
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

class GuessTask(guessWord: String, blockedWords: List<String>) {
    val guessWord = guessWord.uppercase()
    val blockedWords = blockedWords.map { a -> a.uppercase() }
}