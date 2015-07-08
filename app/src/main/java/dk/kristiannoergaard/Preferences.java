package dk.kristiannoergaard;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;

public class Preferences extends PreferenceActivity {

	CharSequence levelPrefTitleOrg;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		
		Preference levelPref = (Preference) findPreference("levelPref");
		levelPrefTitleOrg = levelPref.getTitle();
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
    	String levelStr = prefs.getString("levelPref", "?");
		updatePrefLevel(levelStr);
		
		levelPref.setOnPreferenceChangeListener( new OnPreferenceChangeListener() {

			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				updatePrefLevel((String) newValue);
				return true;
			}
			
			
		} );
		/*
		levelPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {
				Toast.makeText(getBaseContext(),
						"The custom preference has been clicked",
						Toast.LENGTH_LONG).show();
				SharedPreferences customSharedPreference = getSharedPreferences(
						"myCustomSharedPrefs", Activity.MODE_PRIVATE);
				SharedPreferences.Editor editor = customSharedPreference
						.edit();
				editor.putString("myCustomPref",
						"The preference has been clicked");
				editor.commit();
				return true;
			}
		});
		*/
		// Get the custom preference
		/*
		Preference customPref = (Preference) findPreference("customPref");
		customPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {
				Toast.makeText(getBaseContext(),
						"The custom preference has been clicked",
						Toast.LENGTH_LONG).show();
				SharedPreferences customSharedPreference = getSharedPreferences(
						"myCustomSharedPrefs", Activity.MODE_PRIVATE);
				SharedPreferences.Editor editor = customSharedPreference
						.edit();
				editor.putString("myCustomPref",
						"The preference has been clicked");
				editor.commit();
				return true;
			}

		});
		*/
	}
	
	void updatePrefLevel(String newLevel){
       
		Preference levelPref = (Preference) findPreference("levelPref");
		CharSequence newTitle = levelPrefTitleOrg + "  " + newLevel;
		levelPref.setTitle(newTitle);
	}
}