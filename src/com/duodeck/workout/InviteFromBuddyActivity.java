package com.duodeck.workout;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;
import android.view.View;
import android.widget.TextView;

public class InviteFromBuddyActivity extends Activity {

	TextView inviteLabel;
	DuoDeckApplication duoDeckApp;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_invite_from_buddy);
		inviteLabel = (TextView) findViewById(R.id.invite_label);
		Bundle extra = getIntent().getExtras();
		String user = extra.getString("fromUser");
		inviteLabel.setText(user + " invited you for a workout");
		duoDeckApp = (DuoDeckApplication) getApplication();
	}
	
	public void acceptInvite(View view) {
		System.out.println("Creating msg in accept");
		Message msg = Message.obtain(null, DuoDeckService.MSG_INVITE_RESPONSE, 1, 1);
		try {
			duoDeckApp.mService.send(msg);
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
			duoDeckApp.mService.send(msg);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		duoDeckApp.setCurrentGameState(GameStates.Solo);
		Intent intent = new Intent(this, LandingScreenActivity.class);
		startActivity(intent);
	}

}
