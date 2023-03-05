package com.example.guess

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
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
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            preferenceScreen.findPreference<SeekBarPreference>(
                "timer_duration"
            )?.isEnabled =
                preferenceScreen.findPreference<SwitchPreference>("enable_timer")!!.isChecked

            preferenceScreen.findPreference<SwitchPreference>("enable_timer")
                ?.setOnPreferenceChangeListener { _, newValue ->
                    preferenceScreen.findPreference<SeekBarPreference>(
                        "timer_duration"
                    )?.isEnabled = newValue == true
                    true
                }
        }
    }
}