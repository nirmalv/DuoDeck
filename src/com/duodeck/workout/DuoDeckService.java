package com.duodeck.workout;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class DuoDeckService extends Service {

	private static final String TAG = "DuoDeckService";
	private static final int DEFAULT_XMPP_PORT = 5222;
	
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private void startConnecting() {
		
	}
	

}
