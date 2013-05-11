package com.duodeck.workout;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Messenger;
import android.text.TextUtils;

public class DuoDeckApplication extends Application {

	public static final String ACCOUNT_NAME = "username";
	public static final String ACCOUNT_TOKEN = "oauth_token";
	
	public static int[] shuffledOrder;
	public Messenger mService = null;
	
	private String username;
	private String token;
	
	private boolean isServiceRunning = false;
	private boolean isAccountsetup = false;
	private boolean isConnected = false;
	private GameStates currentGameState = GameStates.Solo;
	private HashMap<String, String> contactList = new HashMap<String, String>();

	private PersistentStorage ps;
	
	private Date inviteStartTime = null;
	private Date sessionLastMsgTime = null;
	
	private int[] deckOrder = null;
	
	/**
	 * Constructor
	 */
	public DuoDeckApplication(){
	}
	
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
	public void onCreate() {
		super.onCreate();
		ps = new PersistentStorage();
		username = ps.getUserName(this);
		token = ps.getAuthToken(this);
		isAccountsetup = !TextUtils.isEmpty(username) && !TextUtils.isEmpty(token);
        startService(new Intent(DuoDeckApplication.this, DuoDeckService.class));
        bindService(new Intent(this, DuoDeckService.class), mConnection, Context.BIND_AUTO_CREATE);
        
	}

       @Override
	public void onTerminate() {
		// Its ok even if this method is not called, since we could reuse the service
    	unbindService(mConnection);
		stopService(new Intent(DuoDeckApplication.this, DuoDeckService.class));
	}	

	public String getUsername() {
		return username;
	}
	
	public void setUsername(String uname) {
		username = uname;
		ps.updateUserName(this, uname);
		isAccountsetup = !TextUtils.isEmpty(username) && !TextUtils.isEmpty(token);
	}
	
	public String getAuthToken() {
		return token;
	}
	
	public void setAuthToken(String authToken) {
		token = authToken;
		ps.updateAuthToken(this, authToken);
		isAccountsetup = !TextUtils.isEmpty(username) && !TextUtils.isEmpty(token);
	}

	public boolean isServiceRunning() {
		return isServiceRunning;
	}

	public void setServiceRunning(boolean isServiceRunning) {
		this.isServiceRunning = isServiceRunning;
	}

	public boolean isAccountsetup() {
		return isAccountsetup;
	}

	public void setAccountsetup(boolean isAccountsetup) {
		this.isAccountsetup = isAccountsetup;
	}

	public boolean isConnected() {
		return isConnected;
	}

	public void setConnected(boolean isConnected) {
		this.isConnected = isConnected;
	}

	public synchronized GameStates getCurrentGameState() {
		return currentGameState;
	}

	public synchronized void setCurrentGameState(GameStates currentState) {
		this.currentGameState = currentState;
	}
	
	public ArrayList<String> getContactList() {
		HashSet<String> cList = new HashSet<String>();
		for (String JID : this.contactList.keySet()) {
			cList.add(this.contactList.get(JID));
		}
		return new ArrayList<String>(cList);
	}
	
	public void updateContactList(String JID, String user) {
		this.contactList.put(JID, user);
	}
	
	public void removeContact(String JID, String user) {
		if (this.contactList.containsKey(JID))
			this.contactList.remove(JID);
	}
	
	public String getBuddyAtIndex(int index) {
		int i = 0;
		for (String JID : this.contactList.keySet()) {
			if (index == i)
				return JID;
			i++;
		}
		return null;
	}
	
	public PersistentStorage getPersistentStorage() {
		return ps;
	}

	public synchronized Date getInviteStartTime() {
		return inviteStartTime;
	}

	public synchronized void setInviteStartTime(Date inviteStartTime) {
		this.inviteStartTime = inviteStartTime;
	}

	public synchronized Date getSessionLastMsgTime() {
		return sessionLastMsgTime;
	}

	public synchronized void setSessionLastMsgTime(Date sessionLastMsgTime) {
		this.sessionLastMsgTime = sessionLastMsgTime;
	}

	public synchronized int[] getDeckOrder() {
		return deckOrder;
	}

	public synchronized void setDeckOrder(int[] deckOrder) {
		this.deckOrder = deckOrder;
	}
	
}
