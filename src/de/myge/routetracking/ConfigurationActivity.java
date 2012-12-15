package de.myge.routetracking;

import de.myge.routetracking.settings.Settings;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.RadioButton;
import android.widget.RadioGroup;

/**
 * Konfigurationsmenü.
 * @author Jan
 *
 */
public class ConfigurationActivity extends Activity{
	private RadioGroup radioGroup;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.configuration);
		radioGroup = (RadioGroup) findViewById(R.id.radioSpeed);
		if (Settings.getInstance(this).isSpeedInKmh()) {
			radioGroup.check(R.id.speedKmh);
		} else {
			radioGroup.check(R.id.speedMph);
		}
	}
	
	 @Override
	    public boolean onCreateOptionsMenu(Menu menu) {
	        MenuInflater inflater = getMenuInflater();
	        inflater.inflate(R.menu.save_menu, menu);
	        return true;
	    }
	    
	    @Override
	    public boolean onOptionsItemSelected(MenuItem item) {
	        // Handle item selection
	        switch (item.getItemId()) {
	            case R.id.save_configuration:
	            	save();
	            default:
	            	return false;
	        }
	    }

		private void save() {
			int selectedId = radioGroup.getCheckedRadioButtonId();
			 
			RadioButton radioButton = (RadioButton) findViewById(selectedId);
			
			if (radioButton.getText().equals("Mp/h")) {
				Settings.getInstance(this).setSpeedInKmh(false);
			} else if (radioButton.getText().equals("Km/h")) {
				Settings.getInstance(this).setSpeedInKmh(true);
			}
			
			Intent i = getBaseContext().getPackageManager()
					.getLaunchIntentForPackage( getBaseContext().getPackageName() );
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(i);
		}
}
