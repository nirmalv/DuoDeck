package com.duodeck.workout;

import java.util.Arrays;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;

import com.duodeck.workout.service.DuoDeckConnectionListener;
import com.duodeck.workout.service.DuoDeckConnectionManager;

public class DuoDeckService extends Service implements DuoDeckConnectionListener {

	public static final int MSG_REGISTER = 1;
	public static final int MSG_UNREGISTER = 2;
	public static final int MSG_LOGIN = 3;
	public static final int MSG_GET_ROSTER = 4; // contacts
	public static final int MSG_INVITE = 5;
	public static final int MSG_INVITE_RESPONSE = 6;
	public static final int MSG_SEND_SHUFFLED_ORDER = 7;
	public static final int MSG_SEND_SHUFFLED_ORDER_RESPONSE = 8;
	public static final int MSG_GOT_SHUFFLED_ORDER = 9;
	public static final int MSG_GOT_SHUFFLED_ORDER_RESPONSE = 10;
	public static final int MSG_DONE_WITH_CARD_INDEX = 11;
	public static final int MSG_SESSION_CLOSED = 12;
	
	public static final int DUODECK_NOTIFICATION_ID = 100;
	
	private DuoDeckApplication duoDeckApp;
	private Messenger sClient;
	private DuoDeckConnectionManager duoDeckConnection;
	
	private NotificationManager sNotificationManager;
	
	final Messenger sMessenger = new Messenger(new HandleMessage());
	
	class HandleMessage extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_REGISTER:
				sClient = null;
				sClient = msg.replyTo;
				break;
			case MSG_UNREGISTER:
				sClient = null;
				break;
			case MSG_LOGIN:
				connect();
				break;
			case MSG_INVITE:
				duoDeckApp.setCurrentGameState(GameStates.MeInviting);
				String buddyName = duoDeckApp.getBuddyAtIndex(msg.arg1);
				System.out.println("Before inviting buddy: " + buddyName);
				duoDeckConnection.inviteBuddy(buddyName);
				System.out.println("Affer inviting buddy: " + buddyName);
				break;
			case MSG_INVITE_RESPONSE:
				System.out.println("Sending response before if");
				GameStates cState = duoDeckApp.getCurrentGameState();
				if (msg.arg1 == 1 && cState == GameStates.StartingDuoPlayAsReceiver) {
					System.out.println("Sending response inside if");
					duoDeckConnection.acceptInvite();
				} else {
					System.out.println("Sending response inside else");
					duoDeckConnection.declineInvite();
				}
				break;
			case MSG_SEND_SHUFFLED_ORDER:
				System.out.println("Sending shuffled order");
				duoDeckConnection.sendShuffledOrder();
				break;
			case MSG_SEND_SHUFFLED_ORDER_RESPONSE:
				duoDeckConnection.sendShuffledOrderResponse(true);
				break;
			case MSG_DONE_WITH_CARD_INDEX:
				//arg1 is buddyIndex and arg2 is myIndex
				duoDeckConnection.doneWithCardIndex(msg.arg1, msg.arg2);
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}
		
	private void connect() {
		if (duoDeckConnection == null || !duoDeckConnection.getUsername().equals(duoDeckApp.getUsername())) {
			System.out.println("Connecting to xmpp");
			duoDeckConnection = DuoDeckConnectionManager.initiate(getApplicationContext(), duoDeckApp.getUsername(), 
							duoDeckApp.getAuthToken(), this);
		} else {
			this.loginResponse(true);
		}
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		System.out.println("Starting service..");
		duoDeckApp = (DuoDeckApplication) getApplication();
		duoDeckApp.setServiceRunning(true);
		sNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		t.scheduleAtFixedRate(sessionTimeout, 60000, 60000);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		duoDeckApp.setServiceRunning(true);
		return Service.START_STICKY;
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return sMessenger.getBinder();
	}
	
	@Override
	public void onDestroy() {
		//disconnect XMPP stream
		System.out.println("Stopping service");
		duoDeckApp.setServiceRunning(false);
		sNotificationManager.cancelAll();
		t.cancel();
		super.onDestroy();
	}
	
	public void sendNotification(String user) {
		
		Intent inviteResponse = new Intent(this, InviteFromBuddyActivity.class);
		inviteResponse.putExtra("fromUser", user);
		
		PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, inviteResponse, PendingIntent.FLAG_UPDATE_CURRENT);
		System.out.println("Created pending intent");
		
		NotificationCompat.Builder notifi_builder = new NotificationCompat.Builder(this);
		notifi_builder.setContentIntent(resultPendingIntent)
					  .setContentTitle("DuoDeck Workout")
					  .setContentText(user + " inviting for a workout session")
					  .setSmallIcon(R.drawable.ic_launcher);
		
		Notification notification = notifi_builder.build();
		
		notification.defaults |= Notification.DEFAULT_VIBRATE;
		notification.ledARGB = 0xff0000ff;
		notification.ledOnMS = 1000;
		notification.ledOffMS = 1000;
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		sNotificationManager.notify(DUODECK_NOTIFICATION_ID, notification);
		System.out.println("Notified");
	}
	
	public void deleteNotification() {
		sNotificationManager.cancel(DUODECK_NOTIFICATION_ID);
	}
	
	public NotificationManager getNotificationManager() {
		return sNotificationManager;
	}
	
	private void sendMsgToClient(int type, int arg1, int arg2) {
		try {
			if (sClient != null) 
				sClient.send(Message.obtain(null, type, arg1, arg2));
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void loginResponse(boolean success) {
		if (success) {
			this.sendMsgToClient(MSG_LOGIN, 1, 1);
		} else {
			System.out.println("Calling auth Failed");
			this.sendMsgToClient(MSG_LOGIN, 0, 0);
		}
	}
	
	@Override
	public void getRosterResponse() {
		this.sendMsgToClient(MSG_GET_ROSTER, 1, 1);
	}
	
	@Override
	public void errorReported(Exception e) {
		// TODO Auto-generated method stub
		e.printStackTrace();
	}

	@Override
	public void invite(String user) {
		// TODO Auto-generated method stub
		this.sendNotification(user);
		System.out.println("Got Invite from :" + user);
		
	}

	@Override
	public void inviteResponse(String fromJID, boolean accepted) {
		System.out.println("Sending invite response back to Activity : " + accepted);
		if (accepted) 
			sendMsgToClient(MSG_INVITE_RESPONSE, 1, 1);
		else
			sendMsgToClient(MSG_INVITE_RESPONSE, 0, 0);
	}

	@Override
	public void processShuffledDeck(String fromJID, String deckOrderStr) {
		String[] items = deckOrderStr.replaceAll("\\[", "").replaceAll("\\]", "").split(", ");
		System.out.println("raw string " + items);
		int[] deckOrder = new int[items.length];
		for (int i = 0; i < items.length ; i++) {
			try {
				deckOrder[i] = Integer.parseInt(items[i]);
			} catch (NumberFormatException nfe) {
				System.out.println("Error in processing order");
				this.duoDeckConnection.sendShuffledOrderResponse(false);
				break;
			}
		}
		System.out.println("Processed order " + Arrays.toString(deckOrder));
		duoDeckApp.setDeckOrder(deckOrder);
		sendMsgToClient(MSG_GOT_SHUFFLED_ORDER, 1, 1);
	}

	@Override
	public void shuffledDeckResponse(String fromJID, boolean success) {
		if (success)
			sendMsgToClient(MSG_GOT_SHUFFLED_ORDER_RESPONSE, 1, 1);
		else
			sendMsgToClient(MSG_GOT_SHUFFLED_ORDER_RESPONSE, 0, 0);
	}

	@Override
	public void dockWithCardIndex(String fromJID, int buddyIndex, int myIndex) {
		sendMsgToClient(MSG_DONE_WITH_CARD_INDEX, buddyIndex, myIndex);
	}
	
	Timer t = new Timer();
	TimerTask sessionTimeout = new TimerTask() {

		@Override
		public void run() {
			GameStates currentState = duoDeckApp.getCurrentGameState();
			Date currentTime = new Date(System.currentTimeMillis());
			Date inviteTime = duoDeckApp.getInviteStartTime();
			Date sessionTime = duoDeckApp.getSessionLastMsgTime();
			int inviteElapse = 0;
			int sessionElapse = 0;
			if (inviteTime != null)
				inviteElapse = (int) ((currentTime.getTime() - inviteTime.getTime()) / 1000);
			if (sessionTime != null)
				sessionElapse = (int) ((currentTime.getTime() - sessionTime.getTime()) / 1000);
			System.out.println("Inside timer with " + inviteElapse + " and " + sessionElapse + " and state: " + currentState);
			System.out.println("Invite time:" + inviteTime);
			System.out.println("session time:" + sessionTime);
			switch(currentState) {
			case MeInviting:
				if (inviteElapse > 60) {
					System.out.println("Expiring invite session");
					duoDeckConnection.cleanupSession();
					sendMsgToClient(MSG_INVITE_RESPONSE, 0, 1);
				}
				break;
			case BuddyInviting:
				if (inviteElapse > 60) { // provide this as a settings edit-able
					System.out.println("Expiring buddy invite session");
					duoDeckConnection.declineInvite();
					deleteNotification();
				}
				break;
			default:
				if (sessionElapse > 300) { // if idle for more than 5min, provide as a settings edit-able
					System.out.println("Expiring workout session");
					if (duoDeckConnection.isConnected()) {
						duoDeckConnection.cleanupSession();
						sendMsgToClient(MSG_SESSION_CLOSED, 0, 0);
					}
				}
				break;
			}
		}
	};
	
}
