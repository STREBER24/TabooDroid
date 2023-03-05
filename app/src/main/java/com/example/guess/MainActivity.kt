package com.example.guess

import android.content.Intent
import android.os.Bundle
import android.os.Vibrator
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.example.guess.databinding.ActivityMainBinding

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var timer: StatefulTimer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var score = 0

        val files = FileManager(assets)
        val allTaskFiles = files.getAllFilesInfo()
        val (tasks, fileInfo) = files.getTaskFile(allTaskFiles[0].filename)
        Toast.makeText(
            this,
            getString(R.string.loading_finished, fileInfo.title, fileInfo.language, tasks.size),
            Toast.LENGTH_LONG
        ).show()

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)

        @Suppress("DEPRECATION")
        val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        timer = object : StatefulTimer(preferences.getInt("timer_duration", 60)) {
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
                score = 0
                binding.nextButton.text = getString(R.string.next_button)
                timer.start()
            } else {
                score += 1
            }
            binding.scoreText.text = score.toString()
            showRandomTask(tasks)
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
        }
        return super.onOptionsItemSelected(item)
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