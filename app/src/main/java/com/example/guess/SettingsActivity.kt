package com.example.guess

import android.content.res.AssetManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SeekBarPreference
import androidx.preference.SwitchPreference

private const val TAG = "SettingsActivity"

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment(assets))
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    class SettingsFragment(private val assets: AssetManager) : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            val enableTimerPreference =
                preferenceScreen.findPreference<SwitchPreference>("enable_timer")
            val timerDurationPreference =
                preferenceScreen.findPreference<SeekBarPreference>("timer_duration")
            val chosenTaskFilePreference =
                preferenceScreen.findPreference<ListPreference>("choose_task_file")

            timerDurationPreference?.isEnabled = enableTimerPreference!!.isChecked
            enableTimerPreference.setOnPreferenceChangeListener { _, newValue ->
                timerDurationPreference?.isEnabled = newValue == true
                true
            }

            val files = FileManager(assets).getAllFilesInfo()
            chosenTaskFilePreference?.entryValues = files.map { it.filename }.toTypedArray()
            chosenTaskFilePreference?.entries =
                files.map { getString(R.string.task_file_header_display, it.title, it.language) }
                    .toTypedArray()
        }
    }
}