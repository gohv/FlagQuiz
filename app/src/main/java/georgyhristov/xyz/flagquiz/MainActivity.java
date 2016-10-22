package georgyhristov.xyz.flagquiz;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.Set;

public class MainActivity extends AppCompatActivity {

    public static final String CHOICES = "pref_numberOfChoices";
    public static final String REGIONS = "pref_regionsToInclude";

    private boolean phoneDevice = true; // force portrait mode
    private boolean preferencesChanged = true; //did preferences change


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //set default values in the app's SharedPreferences
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        //register listener for SharedPreferences changes
        PreferenceManager.getDefaultSharedPreferences(this).
                registerOnSharedPreferenceChangeListener(
                        preferencesChangeListener);

        //get the screenSize
        int screenSize = getResources()
                .getConfiguration()
                .screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;

        //if device is a tablet, set phoneDevice to false
        if (screenSize == Configuration.SCREENLAYOUT_SIZE_LARGE ||
                screenSize == Configuration.SCREENLAYOUT_SIZE_XLARGE)
            phoneDevice = false;//not a phone - a tablet

        //if phone-sized device - allow portrait mode
        if (phoneDevice) setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    //show menu if app is  running on a phone or a portrait-oriented tablet
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //get the device current orientation
        int orientation = getResources().getConfiguration().orientation;

        //display the app's menu only in portrait orientation
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            getMenuInflater().inflate(R.menu.menu_main, menu);
            return true;
        } else return false;
    }

    //called after onCreate completes execution
    @Override
    protected void onStart() {
        super.onStart();

        if (preferencesChanged) {
            //when the default pref are set initialize MainActivityFragment and start the quiz
            MainActivityFragment quizFragment = (MainActivityFragment)
                    getSupportFragmentManager().findFragmentById(R.id.quizFragment);

            quizFragment.updateGuessRows(PreferenceManager.getDefaultSharedPreferences(this));
            quizFragment.updateRegions(PreferenceManager.getDefaultSharedPreferences(this));
            quizFragment.resetQuiz();
            preferencesChanged = false;
        }
    }

    //display settings activity:
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent preferencesIntent = new Intent(this, SettingsActivity.class);
        startActivity(preferencesIntent);
        return super.onOptionsItemSelected(item);
    }

    //listener fo changes to the app's SharedPreferences
    private OnSharedPreferenceChangeListener preferencesChangeListener =
            new OnSharedPreferenceChangeListener() {
        //called when the user changes the app's preferences
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            preferencesChanged = true; // user changed app settings

            MainActivityFragment quizFragment = (MainActivityFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.quizFragment);

            if (key.equals(CHOICES)) { // number of choices to be changed
                quizFragment.updateGuessRows(sharedPreferences);
                quizFragment.resetQuiz();
            } else if (key.equals(REGIONS)) {//regions to include changed
                Set<String> regions = sharedPreferences
                        .getStringSet(REGIONS, null);

                if (regions != null && regions.size() > 0) {
                    quizFragment.updateRegions(sharedPreferences);
                    quizFragment.resetQuiz();
                } else {
                    //must select one region--set North America as default
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    regions.add(getString(R.string.defalt_region));
                    editor.putStringSet(REGIONS, regions);
                    editor.apply();

                    Toast.makeText(MainActivity.this,
                            R.string.default_region_message,
                            Toast.LENGTH_SHORT).show();
                }
            }
            Toast.makeText(MainActivity.this,
                    R.string.restarting_quiz,
                    Toast.LENGTH_SHORT).show();
        }
    };
}
