package com.example.guess

import android.content.Intent
import android.content.SharedPreferences
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
    private lateinit var preferences: SharedPreferences
    private var score = 0
    private var skipped = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val numberOfBlockedWords = preferences.getString("number_of_blocked_words", "5")!!.toInt()
        val fillBlockedWords = preferences.getBoolean("fill_blocked_words", true)
        val timerDuration = preferences.getInt("timer_duration", 60)
        val timerEnabled = preferences.getBoolean("enable_timer", true)
        var chosenTaskFile = preferences.getString("choose_task_file", null)

        val files = FileManager(assets)
        if (chosenTaskFile == null) {
            chosenTaskFile = files.getAllFilesInfo().random().filename
        }
        val (tasks, fileInfo) = files.getTaskFile(chosenTaskFile)
        showToast(
            getString(R.string.loading_finished, fileInfo.title, fileInfo.language, tasks.size)
        )

        hideSecondaryTexts(numberOfBlockedWords)

        timer = object : StatefulTimer(timerDuration) {
            override fun onTick(secondsUntilFinished: Int) {
                binding.timerText.text = secondsUntilFinished.toString()
                if (secondsUntilFinished < 4) {
                    vibrate(200)
                }
            }

            override fun onFinished() {
                setButtons(running = false)
                binding.timerText.text = ""
                binding.primaryText.text = ""
                showSecondaryTexts(emptyList())
                vibrate(750)
            }
        }

        if (!timerEnabled) {
            setButtons(running = true)
            resetScore()
            showRandomTask(tasks, numberOfBlockedWords, fillBlockedWords)
        }

        binding.tabooButton.setOnClickListener {
            Log.i(TAG, "taboo button clicked")
            vibrate(150)
            setButtons(running = true)
            if (timer.getState() == StatefulTimer.States.STOPPED && timerEnabled) {
                resetScore()
                timer.start()
            } else {
                score -= 1
                showScore()
            }
            showRandomTask(tasks, numberOfBlockedWords, fillBlockedWords)
        }

        binding.skipButtons.setOnClickListener {
            Log.i(TAG, "skip button clicked")
            skipped += 1
            showScore()
            vibrate(150)
            showRandomTask(tasks, numberOfBlockedWords, fillBlockedWords)
        }

        binding.nextButton.setOnClickListener {
            Log.i(TAG, "next button clicked")
            vibrate(150)
            score += 1
            showScore()
            showRandomTask(tasks, numberOfBlockedWords, fillBlockedWords)
        }
    }

    private fun vibrate(duration: Long) {
        if (preferences.getBoolean("vibrate", true)) {
            @Suppress("DEPRECATION")
            (getSystemService(VIBRATOR_SERVICE) as Vibrator).vibrate(duration)
        }
    }

    private fun showToast(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }

    private fun setButtons(running: Boolean) {
        binding.skipButtons.isVisible = running
        binding.nextButton.isVisible = running
        binding.tabooButton.text = getString(
            when (running) {
                true -> R.string.taboo_button
                false -> R.string.start_timer
            }
        )
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
        skipped = 0
        showScore()
    }

    private fun showScore() {
        val allowedSkips = preferences.getString("skip", "5")!!.toInt()
        binding.scoreText.text = if (allowedSkips != -1 && (skipped - allowedSkips > 0)) {
            score - skipped + allowedSkips
        } else {
            score
        }.toString()
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