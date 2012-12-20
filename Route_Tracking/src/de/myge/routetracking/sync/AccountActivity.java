package de.myge.routetracking.sync;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;
import de.myge.routetracking.R;

public class AccountActivity extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		setContentView(R.layout.create_account);
		
		AccountInformation accountInformation = new AccountInformation(this);
		
		/*
		 * Wenn der Benutzername nicht vorhanden ist, wird davon ausgegangen, dass
		 * der Anwender noch kein Konto hat. Entsprechendes Layout wird geladen,
		 * in dem der Anwender entweder ein neues Konto anlegen kann, oder ein
		 * bereits vorhandenes Konto mit diesem Smartphone verknüpfen kann.
		 */
		if (accountInformation.getUserName() == null) {
			setContentView(R.layout.create_account);
		} 
		/*
		 * Wenn der Benutzer bereits ein Konto hat, werden seine Daten geladen,
		 * damit er diese einsehen und ggf. bearbeiten kann.
		 */
		else {
			new HttpTask(this).execute();
		}
	}
	

	 @Override
	    public boolean onCreateOptionsMenu(Menu menu) {
	        MenuInflater inflater = getMenuInflater();
	        inflater.inflate(R.menu.account_menu, menu);
	        return true;
	    }
	    
	    @Override
	    public boolean onOptionsItemSelected(MenuItem item) {
	        // Handle item selection
	        switch (item.getItemId()) {
	            case R.id.save_account_information:
	            	AccountModel model = new AccountModel();
	            	model.setFirstName(((EditText)findViewById(R.id.registration_first_name)).getText().toString());
	            	model.setLastName(((EditText)findViewById(R.id.registration_last_name)).getText().toString());
	            	model.setPassword(((EditText)findViewById(R.id.registration_password)).getText().toString());
	            	model.setEmailAddress(((EditText)findViewById(R.id.registration_email_address)).getText().toString());
	            	new SaveUserInformation(this).execute(model);
	            default:
	            	return false;
	        }
	    }

	    class SaveUserInformation extends AsyncTask<AccountModel, Void, String> {
			
			private Context context;
			private ProgressDialog dialog;
			private AccountInformation accountInformation;
			
			public SaveUserInformation(Context context) {
				this.context = context;
				
				accountInformation = new AccountInformation(context);
				
				dialog = new ProgressDialog(context);
				dialog.setTitle("Register User");
				dialog.show();
			}
			
			@Override
			protected String doInBackground(AccountModel... params) {
				
				StringBuffer buffer = new StringBuffer();
				for (AccountModel model : params) {
					// Creating HTTP client
					HttpClient httpClient = new DefaultHttpClient();
					// Creating HTTP Post
					HttpPost httpPost = new HttpPost("http://jan-pc/gpstracker/index.php");
					
					// Building post parameters
					// key and value pair
					List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(2);
					nameValuePair.add(new BasicNameValuePair(ServerFields.USER_NAME, "admin"));
					nameValuePair.add(new BasicNameValuePair(ServerFields.PASSWORD, "admin"));
//					nameValuePair.add(new BasicNameValuePair("action", "register"));
					nameValuePair.add(new BasicNameValuePair(ServerFields.ADD_FIRST_NAME, model.getFirstName()));
					nameValuePair.add(new BasicNameValuePair(ServerFields.ADD_LAST_NAME, model.getLastName()));
					nameValuePair.add(new BasicNameValuePair(ServerFields.ADD_EMAIL_ADDRESS, model.getEmailAddress()));
					nameValuePair.add(new BasicNameValuePair(ServerFields.ADD_PASSWORD, model.getPassword()));
					
					// Url Encoding the POST parameters
					try {
						httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));
					} catch (UnsupportedEncodingException e) {
						Log.e("GPS-Tracker", e.getLocalizedMessage(), e);
					}
					
					// Making HTTP Request
					try {
						HttpResponse response = httpClient.execute(httpPost);
						
						HttpEntity entity = response.getEntity();
						if (response.getStatusLine().getStatusCode() == 200) {
							BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()));
							String line;
							while ((line = reader.readLine()) != null) {
								if (line.trim().equals(ErrorCodes.WRONG_USERNAME_OR_PASSWORD)) {
									// Buffer überschreiben und die Fehlermeldung reinschreiben.
									buffer = new StringBuffer();
									buffer.append(getResources().getString(R.string.error_101));
									break;
								}
								buffer.append(line);
							}
						}
						
					} catch (Exception e) {
						Log.e("GPS Tracker", e.getLocalizedMessage(), e);
					}	
				}
				return buffer.toString();
			}
			
			@Override
			protected void onPostExecute(String result) {
				super.onPostExecute(result);
				
				if (dialog.isShowing()) dialog.dismiss();			
				if (result != null && !result.equals("")) Toast.makeText(context, result, Toast.LENGTH_LONG).show();
			}
		}   
	    
	    
	class HttpTask extends AsyncTask<Void, Void, String> {
		
		private Context context;
		private ProgressDialog dialog;
		private AccountInformation accountInformation;
		
		public HttpTask(Context context) {
			this.context = context;
			
			accountInformation = new AccountInformation(context);
			
			dialog = new ProgressDialog(context);
			dialog.setTitle(getResources().getString(R.string.connect_to_server));
			dialog.show();
		}
		
		@Override
		protected String doInBackground(Void... params) {
			String userName = accountInformation.getUserName();
			String password = accountInformation.getPassword();
			
			if (userName != null) {
				StringBuffer buffer = new StringBuffer();
				// Creating HTTP client
				HttpClient httpClient = new DefaultHttpClient();
				// Creating HTTP Post
				HttpPost httpPost = new HttpPost("http://jan-pc/gpstracker/index.php");
				
				// Building post parameters
				// key and value pair
				List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(2);
				nameValuePair.add(new BasicNameValuePair(ServerFields.USER_NAME, userName));
				nameValuePair.add(new BasicNameValuePair(ServerFields.PASSWORD, password));
				
				// Url Encoding the POST parameters
				try {
					httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));
				} catch (UnsupportedEncodingException e) {
					Log.e("GPS-Tracker", e.getLocalizedMessage(), e);
				}
				
				// Making HTTP Request
				try {
					HttpResponse response = httpClient.execute(httpPost);
					
					HttpEntity entity = response.getEntity();
					if (response.getStatusLine().getStatusCode() == 200) {
						BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()));
						String line;
						while ((line = reader.readLine()) != null) {
							if (line.trim().equals(ErrorCodes.WRONG_USERNAME_OR_PASSWORD)) {
								// Buffer überschreiben und die Fehlermeldung reinschreiben.
								buffer = new StringBuffer();
								buffer.append(getResources().getString(R.string.error_101));
								break;
							}
							buffer.append(line);
						}
					}
					
				} catch (Exception e) {
					Log.e("GPS Tracker", e.getLocalizedMessage(), e);
				}	
				return buffer.toString();
			}
			
			return null;
		}
		
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			
			if (dialog.isShowing()) dialog.dismiss();			
			if (result != null && !result.equals("")) Toast.makeText(context, result, Toast.LENGTH_LONG).show();
		}
	}
}
