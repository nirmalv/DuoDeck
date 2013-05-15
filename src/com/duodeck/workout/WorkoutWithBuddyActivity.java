package com.duodeck.workout;

import java.io.IOException;
import java.util.ArrayList;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class WorkoutWithBuddyActivity extends Activity {

	private AccountManager accountManager;
	private Account[] accounts = null;
	private Account accSelected = null;
	private int authAttempt = 0;
	
	private Messenger mService = null;
	private DuoDeckApplication duoDeckApp;
	private TextView labelDisplay;
	private ListView listView;
	private ArrayList<String> contactList;
	private AlertDialog waitPopup;
	
	final Messenger mMessenger = new Messenger(new HandleMessage());
	
	class HandleMessage extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
			case DuoDeckService.MSG_LOGIN:
				if (msg.arg1 == 0) {
					if (authAttempt <= 1) {
						getAuthToken(accSelected, true);
						authAttempt += 1;
					} else {
						labelDisplay.setText("Authentication Failed.");
						authAttempt = 0;
					}
				}
				break;
			case DuoDeckService.MSG_GET_ROSTER:
				displayRoster();
				break;
			case DuoDeckService.MSG_INVITE_RESPONSE:
				System.out.println("Received invite response from service");
				actOnInviteResponse(msg.arg1);
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}
	
	private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			mService = new Messenger(service);
			sendMsgToService(DuoDeckService.MSG_REGISTER, 1, 1);
		}
		
		@Override
		public void onServiceDisconnected(ComponentName className) {
			mService = null;
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_workout_with_buddy);
		duoDeckApp = (DuoDeckApplication) getApplication();
		
		labelDisplay = (TextView) findViewById(R.id.wwb_displayLabel);
		listView = (ListView) findViewById(R.id.wwb_buddyList);
		this.contactList = duoDeckApp.getContactList();

		accountManager = AccountManager.get(getApplicationContext());
		accounts = accountManager.getAccountsByType("com.google");
		if (!duoDeckApp.isAccountsetup()) {
			this.chooseAccount(accounts);
		} else {
			for(Account a : accounts) {
				if (a.name.equals(duoDeckApp.getUsername())) {
					this.accSelected = a;
					break;
				}
			}
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		bindService(new Intent(this, DuoDeckService.class), mConnection, Context.BIND_AUTO_CREATE);
		
		listView.setAdapter(new ArrayAdapter<String>(this, R.layout.activity_workout_with_buddy, R.id.wwb_displayLabel, contactList));
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View v, int pos, long id) {
				switch(duoDeckApp.getCurrentGameState()) {
				case Solo:
					sendMsgToService(DuoDeckService.MSG_INVITE, pos, 1);
					AlertDialog.Builder builder = new AlertDialog.Builder(WorkoutWithBuddyActivity.this);
					builder.setTitle("Waiting for buddy to respond...");
					/*builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dismissPopup();
							sendMsgToService(DuoDeckService.MSG_CANCEL_INVITE,1,1);
						}
					});*/
					waitPopup = builder.create();
					waitPopup.show();
					break;
				case MeInviting:
					Toast.makeText(getBaseContext(), "Please wait 60sec to get response for your previous invite", Toast.LENGTH_LONG);
					break;
				case BuddyInviting:
					Toast.makeText(getBaseContext(), "There is an invite waiting for you in the notification bar", Toast.LENGTH_LONG);
					break;
				default:
					Toast.makeText(getBaseContext(), "You are in the middle of a workout session, complete that first", Toast.LENGTH_LONG);
					break;
				}
			}
			
		});
		if (accSelected != null)
			this.getAuthToken(accSelected, false);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		sendMsgToService(DuoDeckService.MSG_UNREGISTER, 1, 1);
		if (mService != null)
			unbindService(mConnection);
	}
	
	private void dismissPopup() {
		if (waitPopup != null) {
			waitPopup.dismiss();
			waitPopup = null;
		}
	}
	
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		this.dismissPopup();
	}
	
	private void sendMsgToService(int type, int arg1, int arg2) {
		Message msg = Message.obtain(null, type, arg1, arg2);
		msg.replyTo = mMessenger;
		try {
			if (mService != null)
				mService.send(msg);
			else {
				duoDeckApp.mService.send(msg);
				System.out.println("No service available, but sent with app service");
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void chooseAccount(final Account[] accounts) {
		if (accounts.length == 0) {
			labelDisplay.setText("Please add a gmail account in the device to workout with buddies");
			return;
		}
		String[] accNames = new String[accounts.length];
		for (int i = 0; i < accounts.length; i++) 
			accNames[i] = accounts[i].name;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.wwb_chooseAccount)
			   .setItems(accNames, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					duoDeckApp.setUsername(accounts[which].name);
					accSelected = accounts[which];
					getAuthToken(accSelected, false);
				}
			});
		AlertDialog dialog = builder.create();
		dialog.show();
	}
	
	private void getAuthToken(Account account, boolean invalidate) {
		Bundle options = new Bundle();
		String authTokenType = "oauth2:https://www.googleapis.com/auth/googletalk";
		if (invalidate) {
			accountManager.invalidateAuthToken("com.google", duoDeckApp.getAuthToken());
			System.out.println("Invalidating AuthToken");
		}
		accountManager.getAuthToken(account, authTokenType, options, this,
				new AccountManagerCallback<Bundle>() {
					@Override
					public void run(AccountManagerFuture<Bundle> future) {
						Bundle bundle;
						try {
							bundle = future.getResult();
							String authtoken = bundle.getString(AccountManager.KEY_AUTHTOKEN);
							duoDeckApp.setAuthToken(authtoken);
							for (int i = 0; i < 10; i++) {
								if (mService == null)
									try {
										Thread.sleep(500);
									} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								else break;
							}
							sendMsgToService(DuoDeckService.MSG_LOGIN,1,1);
							System.out.println("Done calling MSG_LOGIN");
						} catch (OperationCanceledException e) {
							System.out.println("User denied authorization");
							labelDisplay.setText("User denied Permission");
							accSelected = null;
						} catch (AuthenticatorException e) {
							System.out.println("Error when authorizing");
						} catch (IOException e) {
							System.out.println("Network error when trying to get authToken");
						}
						
					}
			
				}
				, null);
	}
	
	private void displayRoster() {
		contactList.clear();
		contactList.addAll(duoDeckApp.getContactList());
		if (contactList.size() == 0)
			labelDisplay.setText("No contacts available online");
		else 
			labelDisplay.setText("Online Buddies");
	}
	
	@SuppressLint("ShowToast")
	private void actOnInviteResponse(int success) {
		System.out.println("Inside act on invite response: " + success);
		this.dismissPopup();
		String buddy = "";
		if (accSelected != null)
			buddy = accSelected.name;
		if (success == 0) {
			Toast.makeText(this, buddy + " requested for a different time", Toast.LENGTH_LONG).show();
			duoDeckApp.setCurrentGameState(GameStates.Solo);
		} else {
			duoDeckApp.delayedService = 1;
			duoDeckApp.setCurrentGameState(GameStates.StartingDuoPlayAsSender);
			Intent intent = new Intent(this, GameActivity.class);
			startActivity(intent);
		}
	}
	
	public void resetConnection(View view) {
		System.out.println("Reset");
		Message msg = Message.obtain(null, DuoDeckService.MSG_RESET, 1, 1);
	}
}
