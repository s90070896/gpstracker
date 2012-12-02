package de.myge.routetracking.export;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.Toast;

import com.j256.ormlite.dao.Dao;

import de.myge.routetracking.R;
import de.myge.routetracking.arrayadapter.Model;
import de.myge.routetracking.database.CreateDatabase;
import de.myge.routetracking.database.Profile;

public class ExportActivity extends Activity{
	private CreateDatabase db;
	private List<String> toBeExport = new ArrayList<String>();
	private List<Model> profilesAsList;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.export);
		
		// neue Instanz für den Zugriff auf die DB erzeugen
        db = new CreateDatabase(getApplicationContext());
		
		ListView listView1 = (ListView)findViewById(R.id.export_profiles);
		
		try {
			listView1.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, convertAllProfilesToArrays()));
			listView1.setItemsCanFocus(false);
			listView1.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
			listView1.setOnItemClickListener(listCheckBoxListener());
			Button btn = (Button) findViewById(R.id.export_btn);
			btn.setOnClickListener(addButtonListener());
		} catch (SQLException e) {
			Log.e("de.myge.routetracking", e.getLocalizedMessage(), e);
		}
	}

	private OnClickListener addButtonListener() {
		return new OnClickListener() {
			@Override
			public void onClick(View v) {
				new ExportProfiles(getApplicationContext()).execute(new Object[] {});
			}
		};
	}

	private void exportProfiles() {
		if (toBeExport.size() > 0) {
			try {
				List<Model> allProfiles = getAllProfiles();
				for (Model m : allProfiles) {
					for (int i = 0; i < toBeExport.size(); i++) {
						if (m.getProfile().getProfileName()
								.equals(toBeExport.get(i))) {
							Export export = new Export(m.getProfile(), db);
							export.export();
						}
					}
				}
				Toast.makeText(getApplicationContext(), "Export done",
						Toast.LENGTH_LONG).show();
			} catch (Exception e) {
				Log.e("de.myge.routetracking", e.getLocalizedMessage(), e);
			}
		}
	}
	private OnItemClickListener listCheckBoxListener() {
		return new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				CheckedTextView ctv = (CheckedTextView)arg1;
				
				String string = ctv.getText().toString();
				if (!ctv.isChecked()) {
					toBeExport.add(string);
				} else {
					for (int i = 0; i < toBeExport.size(); i++) {
						if (toBeExport.get(i).equals(string)) {
							toBeExport.remove(i);
							break;
						}
					}
				}
			}
		};
	}
	
	private List<Model> getAllProfiles() throws SQLException {
		final Dao<Profile, Integer> dao = db.getProfileCoordinatesDao();
		List<Profile> list = dao.queryForAll();

		List<Model> listModel = new ArrayList<Model>();
		
		for (Profile profile : list) {
			listModel.add(new Model(profile, this));
		}
		return listModel;
	}
	
	private String[] convertAllProfilesToArrays() throws SQLException {
		profilesAsList = getAllProfiles();
		
		int size = profilesAsList.size();
		String returnArray[] = new String[size];
		for (int i = 0; i < size; i++) {
			returnArray[i] = profilesAsList.get(i).getProfile().getProfileName();
		}
		return returnArray;
	}
	
	class ExportProfiles extends AsyncTask {
		
		private Context context;
		private ProgressDialog dialog;

		public ExportProfiles(Context context) {
			this.context = context;
			dialog = new ProgressDialog(context);
		}
		
		@Override
		protected Object doInBackground(Object... params) {
			exportProfiles(); 
			return null;
		}
		
		@Override
	    protected void onPostExecute(Object result) {
			if (dialog.isShowing()) dialog.cancel();
		}
	}
}
