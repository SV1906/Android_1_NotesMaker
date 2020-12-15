package com.example.notesmaker;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SettingsActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseUser mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            final Preference localStorage = findPreference("LocalStorage");
            PreferenceCategory cloud = findPreference("Cloud");

            FirebaseAuth auth = FirebaseAuth.getInstance();
            FirebaseUser user = auth.getCurrentUser();

            if (user!=null){
                cloud.setEnabled(true);
            } else {
                cloud.setEnabled(false);
            }

            if (preferences.getBoolean("CloudUpload", false)){
                localStorage.setEnabled(true);
            } else {
                localStorage.setEnabled(false);
            }



            preferences.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                    Log.e("CloudUPload", "" + preferences.getBoolean("CloudUpload", false));
                    switch (key){
                        case "CloudUpload":{
                            if (preferences.getBoolean("CloudUpload", false)){
                                //localStorage.setSelectable(false);
                                localStorage.setEnabled(true);

                            } else {
                                //localStorage.setSelectable(true);
                                localStorage.setEnabled(false);
                            }
                            break;
                        }
                    }
                }
            });

        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}