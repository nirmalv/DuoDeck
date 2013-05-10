package com.duodeck.workout;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.view.View;
import android.widget.TextView;

public class InviteFromBuddyActivity extends Activity {

	TextView inviteLabel;
	private Messenger mService = null;
	DuoDeckApplication duoDeckApp;
	
	private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			mService = new Messenger(service);
		}
		
		@Override
		public void onServiceDisconnected(ComponentName className) {
			mService = null;
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		bindService(new Intent(this, DuoDeckService.class), mConnection, Context.BIND_AUTO_CREATE);
		setContentView(R.layout.activity_invite_from_buddy);
		inviteLabel = (TextView) findViewById(R.id.invite_label);
		Bundle extra = getIntent().getExtras();
		String user = extra.getString("fromUser");
		inviteLabel.setText(user + " invited you for a workout");
		duoDeckApp = (DuoDeckApplication) getApplication();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if (mService == null) 
			bindService(new Intent(this, DuoDeckService.class), mConnection, Context.BIND_AUTO_CREATE);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		unbindService(mConnection);
	}
	
	public void acceptInvite(View view) {
		System.out.println("Creating msg in accept");
		Message msg = Message.obtain(null, DuoDeckService.MSG_INVITE_RESPONSE, 1, 1);
		try {
			mService.send(msg);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		duoDeckApp.setCurrentGameState(GameStates.StartingDuoPlayAsReceiver);
		Intent intent = new Intent(this, GameActivity.class);
		startActivity(intent);
	}
	
	public void declineInvite(View view) {
		Message msg = Message.obtain(null, DuoDeckService.MSG_INVITE_RESPONSE, 0, 0);
		try {
			mService.send(msg);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		duoDeckApp.setCurrentGameState(GameStates.Solo);
		Intent intent = new Intent(this, LandingScreenActivity.class);
		startActivity(intent);
	}

}
