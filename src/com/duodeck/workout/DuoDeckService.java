package com.duodeck.workout;

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
import android.support.v4.app.TaskStackBuilder;

import com.duodeck.workout.service.DuoDeckConnectionListener;
import com.duodeck.workout.service.DuoDeckConnectionManager;

public class DuoDeckService extends Service implements DuoDeckConnectionListener {

	public static final int MSG_REGISTER = 1;
	public static final int MSG_UNREGISTER = 2;
	public static final int MSG_LOGIN = 3;
	public static final int MSG_GET_ROSTER = 4;
	public static final int MSG_INVITE = 5;
	public static final int MSG_INVITE_RESPONSE = 6;
	public static final int MSG_SEND_SHUFFLED_ORDER = 7;
	public static final int MSG_SHUFFLED_ORDER_RESPONSE = 8;
	public static final int MSG_DONE_WITH_CARD_INDEX = 9;
	
	public static final int DUODECK_NOTIFICATION_ID = 0;
	
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
				output("Register client");
				if (sClient == null)
					sClient = msg.replyTo;
				else
					System.out.println("There is already a client registered");
				break;
			case MSG_UNREGISTER:
				if (sClient != null)
					sClient = null;
				else
					System.out.println("No registered client present");
				output("Unregistering client");
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
				if (msg.arg1 == 1 && cState == GameStates.BuddyInviting) {
					System.out.println("Sending response inside if");
					duoDeckApp.setCurrentGameState(GameStates.StartingDuoPlayAsReceiver);
					duoDeckConnection.acceptInvite();
				} else {
					duoDeckApp.setCurrentGameState(GameStates.Solo);
					System.out.println("Sending response inside else");
					duoDeckConnection.declineInvite();
				}
				break;
			case MSG_SEND_SHUFFLED_ORDER:
				
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}
	
	private void output(String val) {
		System.out.println(val);
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
		duoDeckApp.setServiceRunning(false);
		sNotificationManager.cancelAll();
		super.onDestroy();
	}
	
	public void sendNotification(String user) {
		NotificationCompat.Builder notifi_builder = new NotificationCompat.Builder(this)
						  .setContentText("DuoDeck Workout")
						  .setContentText(user + " inviting you work for workout session");
		Intent inviteResponse = new Intent(this, InviteFromBuddy.class);
		inviteResponse.putExtra("fromUser", user);
		inviteResponse.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		/*TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		stackBuilder.addParentStack(LandingScreenActivity.class);
		stackBuilder.addNextIntent(inviteResponse);
		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);*/
		
		PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, inviteResponse, 0);
		System.out.println("Created pending intent");
		
		notifi_builder.setContentIntent(resultPendingIntent);
		Notification notification = notifi_builder.build();
		//notification.defaults |= Notification.DEFAULT_VIBRATE;
		//notification.ledARGB = 0xff0000ff;
		//notification.ledOnMS = 1000;
		//notification.ledOffMS = 1000;
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		sNotificationManager.notify(DUODECK_NOTIFICATION_ID, notification);
		System.out.println("Notified");
		System.out.println("Sending response inside if");
		duoDeckApp.setCurrentGameState(GameStates.StartingDuoPlayAsReceiver);
		duoDeckConnection.acceptInvite();
	}
	
	public void deleteNotification() {
		sNotificationManager.cancel(DUODECK_NOTIFICATION_ID);
	}
	
	public NotificationManager getNotificationManager() {
		return sNotificationManager;
	}
	
	private void sendMsgToClient(int type, int arg1, int arg2) {
		try {
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
		// TODO Auto-generated method stub
		
	}



	@Override
	public void processShuffledDeck(String fromJID) {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void shuffledDeckResponse(String fromJID, boolean success) {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void dockWithCardIndex(String fromJID, int seq, int ackSeq) {
		// TODO Auto-generated method stub
		
	}
	
}
