package com.example.guess

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Vibrator
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.guess.databinding.ActivityMainBinding

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var timer: StatefulTimer
    private lateinit var preferences: SharedPreferences
    private lateinit var scoreData: MutableList<Team>
    private var skipped = 0
    private var round = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val numberOfBlockedWords = preferences.getString("number_of_blocked_words", "5")!!.toInt()
        val timerDuration = preferences.getInt("timer_duration", 60)
        val timerEnabled = preferences.getBoolean("enable_timer", true)
        var chosenTaskFile = preferences.getString("choose_task_file", null)

        scoreData = resources.getStringArray(R.array.team_names).map { Team(it, 0) }.toMutableList()
        binding.recycleScore.layoutManager = LinearLayoutManager(this)
        binding.recycleScore.adapter = RecycleAdapter(scoreData)
        showHeader()

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
                if (secondsUntilFinished < 4) vibrate(200)
            }

            override fun onFinished() {
                setButtons(running = false)
                binding.timerText.text = ""
                binding.primaryText.text = ""
                showSecondaryTexts(emptyList())
                vibrate(750)
                round += 1
                showHeader()
            }
        }

        if (!timerEnabled) {
            setButtons(running = true)
            showScore()
            showRandomTask(tasks)
        }

        binding.tabooButton.setOnClickListener {
            Log().i(TAG, "taboo button clicked")
            vibrate(150)
            setButtons(running = true)
            if (timer.state == StatefulTimer.States.STOPPED && timerEnabled) {
                skipped = 0
                showScore()
                timer.start()
            } else {
                scoreData[getTeam()].score -= 1
                showScore()
            }
            showRandomTask(tasks)
        }

        binding.skipButtons.setOnClickListener {
            val allowedSkips = preferences.getString("skip", "5")!!.toInt()
            Log().i(TAG, "skip button clicked")
            skipped += 1
            if (skipped > allowedSkips) scoreData[getTeam()].score -= 1
            showScore()
            vibrate(150)
            showRandomTask(tasks)
        }

        binding.nextButton.setOnClickListener {
            Log().i(TAG, "next button clicked")
            vibrate(150)
            scoreData[getTeam()].score += 1
            showScore()
            showRandomTask(tasks)
        }
    }

    private fun getTeam(): Int {
        return round % scoreData.size
    }

    private fun showHeader() {
        binding.roundText.text = getString(R.string.round_counter, (round / 2) + 1)
        binding.turnText.text =
            getString(R.string.team_explaining, scoreData[getTeam()].name)
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
            R.id.action_about -> startActivity(Intent(this, AboutActivity::class.java))
            R.id.action_reset_score -> resetScore()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun resetScore() {
        repeat(scoreData.size) { setScoreView(it, 0) }
        skipped = 0
    }

    private fun showScore() {
        setScoreView(getTeam(), scoreData[getTeam()].score)
    }

    private fun setScoreView(index: Int, value: Int) {
        binding.recycleScore.children.elementAtOrNull(index)
            ?.findViewById<TextView>(R.id.team_score)?.text = value.toString()
    }

    private fun showRandomTask(
        tasks: List<GuessTask>
    ) {
        val numberOfBlockedWords = preferences.getString("number_of_blocked_words", "5")!!.toInt()
        val generationMode = preferences.getString("task_generation", "fill")

        var chosenTask = if (generationMode == "advanced")
            GuessTask(getRandomWord(tasks, emptyList()), emptyList()) else tasks.randomOrNull()
        if (chosenTask == null) {
            binding.primaryText.text = ""
            showSecondaryTexts(emptyList())
            Log().w(TAG, "failed to show next random task")
        } else {
            while (chosenTask!!.blockedWords.size < numberOfBlockedWords && generationMode != "raw") {
                chosenTask = addSimilarWord(tasks, chosenTask)
            }
            binding.primaryText.text = chosenTask.guessWord
            showSecondaryTexts(chosenTask.blockedWords.shuffled())
            Log().i(TAG, "show random word '${chosenTask.guessWord}'")
        }
    }

    private fun addSimilarWord(tasks: List<GuessTask>, chosen: GuessTask): GuessTask {
        var additional = getSimilarWords(tasks, chosen, chosen.guessWord).randomOrNull()
        if (additional == null) additional =
            chosen.blockedWords.flatMap { getSimilarWords(tasks, chosen, it) }.randomOrNull()
        if (additional == null) additional = getRandomWord(tasks, chosen.toList())
        return GuessTask(chosen.guessWord, listOf(additional) + chosen.blockedWords)
    }

    private fun getRandomWord(tasks: List<GuessTask>, blocked: List<String>): String {
        return tasks.flatMap { listOf(it.guessWord) + it.blockedWords }
            .filter { !blocked.any { blockedWord -> blockedWord.contains(it.toRegex()) } }.random()
    }

    private fun getSimilarWords(
        tasks: List<GuessTask>,
        blocked: GuessTask,
        word: String,
    ): List<String> {
        return (tasks.filter { it.guessWord == word }.flatMap { it.blockedWords } +
                tasks.filter { it.toList().contains(word) }.map { it.guessWord })
            .filter { !blocked.toList().any { blockedWord -> blockedWord.contains(it.toRegex()) } }
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

class Team(val name: String, var score: Int)

class RecycleAdapter(private val list: MutableList<Team>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return GroupViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.score_text, parent, false)
        )
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val dataList = list[position]
        holder as GroupViewHolder
        holder.score?.text = dataList.score.toString()
        holder.name?.text = dataList.name
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    class GroupViewHolder(row: View) : RecyclerView.ViewHolder(row) {
        val score = row.findViewById(R.id.team_score) as TextView?
        val name = row.findViewById(R.id.team_name) as TextView?
    }
}