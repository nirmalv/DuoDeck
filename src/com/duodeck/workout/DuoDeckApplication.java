package com.duodeck.workout;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

public class DuoDeckApplication extends Application {

	public static final String ACCOUNT_NAME = "username";
	public static final String ACCOUNT_TOKEN = "oauth_token";
	public static final String USER_STATUS = "offline";
	public static final String FULL_JID = "full_jid";
	
	
	private boolean isServiceRunning = false;
	private boolean isAccountsetup = false;
	private boolean isConnected = false;
	private GameStates currentGameState = GameStates.Alone;
	private SharedPreferences settings;
	
	/**
	 * Constructor
	 */
	public DuoDeckApplication(){
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		settings = PreferenceManager.getDefaultSharedPreferences(this);
		String username = settings.getString(DuoDeckApplication.ACCOUNT_NAME, "");
		String token = settings.getString(DuoDeckApplication.ACCOUNT_TOKEN, "");
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

	public GameStates getCurrentGameState() {
		return currentGameState;
	}

	public void setCurrentGameState(GameStates currentState) {
		this.currentGameState = currentState;
	}

	public SharedPreferences getSettings() {
		return settings;
	}

	public void setSettings(SharedPreferences settings) {
		this.settings = settings;
	}
	
	
}
