package com.example.guess

import android.content.Intent
import android.os.Bundle
import android.os.Vibrator
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.preference.PreferenceManager
import com.example.guess.databinding.ActivityMainBinding

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var timer: StatefulTimer
    private var score = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val numberOfBlockedWords = preferences.getString("number_of_blocked_words", "5")!!.toInt()
        val fillBlockedWords = preferences.getBoolean("fill_blocked_words", true)
        val timerDuration = preferences.getInt("timer_duration", 60)
        val timerEnabled = preferences.getBoolean("enable_timer", true)
        var chosenTaskFile = preferences.getString("choose_task_file", null)
        Log.i(TAG, "loading preferences finished")

        val files = FileManager(assets)
        if (chosenTaskFile == null) {
            chosenTaskFile = files.getAllFilesInfo().random().filename
        }
        val (tasks, fileInfo) = files.getTaskFile(chosenTaskFile)
        Toast.makeText(
            this,
            getString(R.string.loading_finished, fileInfo.title, fileInfo.language, tasks.size),
            Toast.LENGTH_LONG
        ).show()

        hideSecondaryTexts(numberOfBlockedWords)

        @Suppress("DEPRECATION")
        val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        timer = object : StatefulTimer(timerDuration) {
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

        if (!timerEnabled) {
            binding.nextButton.text = getString(R.string.next_button)
            binding.scoreText.text = "0"
            showRandomTask(tasks, numberOfBlockedWords, fillBlockedWords)
        }

        binding.nextButton.setOnClickListener {
            binding.nextButton.text = getString(R.string.next_button)
            if (timer.getState() == StatefulTimer.States.STOPPED && timerEnabled) {
                resetScore()
                timer.start()
            } else {
                score += 1
                binding.scoreText.text = score.toString()
            }
            showRandomTask(tasks, numberOfBlockedWords, fillBlockedWords)
        }
    }

    override fun onDestroy() {
        timer.cancel()
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.settings_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings -> startActivity(Intent(this, SettingsActivity::class.java))
            R.id.action_reset_score -> resetScore()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun resetScore() {
        score = 0
        binding.scoreText.text = "0"
    }

    private fun showRandomTask(
        tasks: List<GuessTask>,
        numberOfBlockedWords: Int,
        fillBlockedWords: Boolean
    ) {
        var chosenTask = tasks.randomOrNull()
        if (chosenTask == null) {
            binding.primaryText.text = ""
            showSecondaryTexts(emptyList())
            Log.w(TAG, "failed to show next random task")
        } else {
            while (chosenTask!!.blockedWords.size < numberOfBlockedWords && fillBlockedWords) {
                chosenTask = addSimilarWord(tasks, chosenTask)
            }
            binding.primaryText.text = chosenTask.guessWord
            showSecondaryTexts(chosenTask.blockedWords.shuffled())
            Log.i(TAG, "show random word '${chosenTask.guessWord}'")
        }
    }

    private fun addSimilarWord(tasks: List<GuessTask>, chosen: GuessTask): GuessTask {
        var additional = getSimilarWords(tasks, chosen, chosen.guessWord).randomOrNull()
        if (additional == null) {
            additional =
                chosen.blockedWords.flatMap { getSimilarWords(tasks, chosen, it) }.randomOrNull()
        }
        if (additional == null) {
            additional = getRandomWord(tasks, chosen)
        }
        return GuessTask(chosen.guessWord, listOf(additional) + chosen.blockedWords)
    }

    private fun getRandomWord(tasks: List<GuessTask>, blocked: GuessTask): String {
        return tasks.flatMap { listOf(it.guessWord) + it.blockedWords }
            .filter { !blocked.toList().contains(it) }.random()
    }

    private fun getSimilarWords(
        tasks: List<GuessTask>,
        blocked: GuessTask,
        word: String,
    ): List<String> {
        return tasks.flatMap {
            if (it.toList().contains(word)) {
                it.toList()
            } else {
                emptyList()
            }
        }.filter { !blocked.toList().contains(it) }
    }

    private fun showSecondaryTexts(textList: List<String>) {
        binding.secondaryText1.text = textList.elementAtOrNull(0)
        binding.secondaryText2.text = textList.elementAtOrNull(1)
        binding.secondaryText3.text = textList.elementAtOrNull(2)
        binding.secondaryText4.text = textList.elementAtOrNull(3)
        binding.secondaryText5.text = textList.elementAtOrNull(4)
        binding.secondaryText6.text = textList.elementAtOrNull(5)
    }

    private fun hideSecondaryTexts(shownTexts: Int) {
        binding.secondaryText1.isVisible = shownTexts > 0
        binding.secondaryText2.isVisible = shownTexts > 1
        binding.secondaryText3.isVisible = shownTexts > 2
        binding.secondaryText4.isVisible = shownTexts > 3
        binding.secondaryText5.isVisible = shownTexts > 4
        binding.secondaryText6.isVisible = shownTexts > 5
    }
}