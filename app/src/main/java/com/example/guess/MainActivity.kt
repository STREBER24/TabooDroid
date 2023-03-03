package com.example.guess

import android.os.Bundle
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

        val files = FileManager(assets)
        val allTaskFiles = files.getAllFilesInfo()
        val (tasks, fileInfo) = files.getTaskFile(allTaskFiles[0].filename)
        Toast.makeText(
            this,
            getString(R.string.loading_finished, fileInfo.title, fileInfo.language, tasks.size),
            Toast.LENGTH_LONG
        ).show()

        @Suppress("DEPRECATION")
        val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        val timer = object : StatefulTimer(60) {
            override fun onTick(secondsUntilFinished: Int) {
                binding.timerText.text = secondsUntilFinished.toString()
                if (secondsUntilFinished < 4) {
                    @Suppress("DEPRECATION") vibrator.vibrate(200)
                }
            }

            override fun onFinished() {
                binding.nextButton.text = getString(R.string.start_timer)
                binding.timerText.text = ""
                binding.primaryText.text = ""
                showSecondaryTexts(emptyList())
                @Suppress("DEPRECATION") vibrator.vibrate(750)
            }
        }

        binding.nextButton.setOnClickListener {
            if (timer.getState() == StatefulTimer.States.STOPPED) {
                binding.nextButton.text = getString(R.string.next_button)
                timer.start()
            }
            showRandomTask(tasks)
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
}