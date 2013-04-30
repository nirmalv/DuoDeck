package com.duodeck.workout.xmpp;

import java.io.IOException;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.ListView;

import com.duodeck.workout.DuoDeckApplication;
import com.example.duodeck.R;

public class WorkoutWithBuddyActivity extends Activity {

	private AccountManager accountManager;
	private Account[] accounts = null;
	private int accSelected = 0;
	
	private String accName;
	private String authToken;
	private String connectError = null;
	private DuoDeckApplication duoDeckApp;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		duoDeckApp = (DuoDeckApplication) getApplication();
		accountManager = AccountManager.get(getApplicationContext());
		if (!duoDeckApp.isAccountsetup()) {
			accounts = accountManager.getAccountsByType("com.google");
			this.chooseAccount(accounts);
		} else {
			System.out.println("totaly did it in else");
			this.accName = duoDeckApp.getUsername();
			this.authToken = duoDeckApp.getAuthToken();
		}
		final ListView listView = (ListView) findViewById(R.id.wwb_buddyList);
		
		//final BuddyListAdapter adapter = new BuddyListAdapter(this, ) 
		
		//this.setListAdapter(new ArrayAdapter(this, R.layout.activity_workout_with_buddy, accounts));
		setContentView(R.layout.activity_workout_with_buddy);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (this.accounts != null) {
			this.getAuthToken(this.accounts[accSelected]);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.workout_with_buddy, menu);
		return true;
	}
	
	private void debugAccDetails() {
		Log.i("Log Info", "Account Name: " + this.accName);
		Log.i("Log Info", "AuthToken: " + this.authToken);
		Log.i("Log Info", "Error: " + this.connectError);
	}

	private void chooseAccount(Account[] accounts) {
		String[] accNames = new String[accounts.length];
		for (int i = 0; i < accounts.length; i++) 
			accNames[i] = accounts[i].name;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.wwb_chooseAccount)
			   .setItems(accNames, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					accSelected = which;
				}
			});
		AlertDialog dialog = builder.create();
		dialog.show();
	}
	
	private void getAuthToken(Account account) {
		Bundle options = new Bundle();
		String authTokenType = "oauth2:https://www.googleapis.com/auth/googletalk";
		accountManager.getAuthToken(account, authTokenType, options, this,
				new AccountManagerCallback<Bundle>() {
					@Override
					public void run(AccountManagerFuture<Bundle> future) {
						Bundle bundle;
						try {
							bundle = future.getResult();
							String username = bundle.getString(AccountManager.KEY_ACCOUNT_NAME);
							WorkoutWithBuddyActivity.this.accName = username;
							duoDeckApp.setUsername(username);
							String authtoken = bundle.getString(AccountManager.KEY_AUTHTOKEN);
							WorkoutWithBuddyActivity.this.authToken = authtoken;
							duoDeckApp.setAuthToken(authtoken);
							WorkoutWithBuddyActivity.this.debugAccDetails();
						} catch (OperationCanceledException e) {
							connectError = "User denied authorization";
						} catch (AuthenticatorException e) {
							connectError = "Error when authorizing";
						} catch (IOException e) {
							connectError = "Network error when trying to get authToken";
						}
						
					}
			
				}
				, null);
	}
}
