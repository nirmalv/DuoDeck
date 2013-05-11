package com.duodeck.workout.service;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import org.json.*;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.util.StringUtils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;

import com.duodeck.workout.DuoDeckApplication;
import com.duodeck.workout.GameStates;

public class DuoDeckConnectionManager implements MessageListener, ChatManagerListener {

	private static final String DEFAULT_XMPP_SERVER = "talk.google.com";
	private static final int DEFAULT_XMPP_PORT = 5222;
	private static final String APP_CHAT_RESOURCE = "duo-deck-xmpp";
	
	private String username;
	private String authToken;
	private Context appContext;
	private DuoDeckConnectionListener listener;
	private Account currentAccount;
	
	private static DuoDeckConnectionManager instance;
	private Connection xmppConnection;
	private ConnectionConfiguration xmppConf;
	
	private DuoDeckSession session;
	
	private DuoDeckConnectionManager(Context appContext, String username, String token, DuoDeckConnectionListener listener) {
		this.appContext = (DuoDeckApplication) appContext;
		this.username = username;
		this.authToken = token;
		this.listener = listener;
	}
	
	public static DuoDeckConnectionManager initiate(Context appContext, String username, String token, DuoDeckConnectionListener listener){
		System.out.println("Inside initate");
		try {
			if (instance != null) {
				instance.disconnect();
				instance = null;
			}
			instance = new DuoDeckConnectionManager(appContext, username, token, listener);
			instance.connect();
			
		} finally {//catch (XMPPException e) {
			//e.printStackTrace();
			//instance = null;
		}
		return instance;
	}
	
	public static DuoDeckConnectionManager getInstance() {
		return instance;
	}
	
	public String getUsername(){
		return this.username;
	}
	
	protected void initConfig() {
		SASLAuthentication.registerSASLMechanism(SASLGoogleByOAuth.MECHANISM_NAME, SASLGoogleByOAuth.class);
		SASLAuthentication.supportSASLMechanism(SASLGoogleByOAuth.MECHANISM_NAME);
		xmppConf = new ConnectionConfiguration(DEFAULT_XMPP_SERVER, DEFAULT_XMPP_PORT);
		System.out.println("Service name:" + StringUtils.parseServer(username));
		System.out.println("Token :" + authToken);
		xmppConf.setServiceName(StringUtils.parseServer(this.username));
		xmppConf.setSASLAuthenticationEnabled(true);
	}
	
	protected void connect() {
		this.initConfig();
		AccountManager accountManager = AccountManager.get(this.appContext);
		for(Account a : accountManager.getAccountsByType("com.google")) {
			if (a.name.equals(this.username)) {
				this.currentAccount = a;
				break;
			}
		}
		if (this.currentAccount == null) return;
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					xmppConnection = new XMPPConnection(xmppConf);
					xmppConnection.connect();
					xmppConnection.login(username, authToken, APP_CHAT_RESOURCE);
					xmppConnection.getChatManager().addChatListener(instance);
					System.out.println(xmppConnection.getUser());
					((DuoDeckApplication) appContext).setConnected(true);
					listener.loginResponse(true);
					registerForRosterUpdates();
				} catch(XMPPException e) {
					e.printStackTrace();
					((DuoDeckApplication) appContext).setConnected(false);
					listener.loginResponse(false);
				}
			}
		}).start();
	}
	
	private void disconnect() {
		if (session != null) {
			session.close(this);
			session = null;
		}
		if (xmppConnection != null) {
			xmppConnection.disconnect();
			xmppConnection = null;
		}
		this.username = "";
		listener = null;
	}
	
	public void setDuoDeckConnectionListener(DuoDeckConnectionListener listener) {
		this.listener = listener;
	}
	
	public static boolean isInitiated() {
		return instance != null;
	}
	
	public boolean isConnected() {
		return xmppConnection != null && xmppConnection.isConnected();
	}
	
	private void registerForRosterUpdates() {
		if (xmppConnection != null) {
			Roster roster = xmppConnection.getRoster();
			Presence p;
			for (RosterEntry entry : roster.getEntries()) {
				p = roster.getPresence(entry.getUser());
				String JID = p.getFrom();
				String user = StringUtils.parseName(JID);
				String resource = StringUtils.parseResource(JID);
				System.out.println("Got " + JID + " with " + p.getType());
				if (resource.contains("duo-deck")) 
					((DuoDeckApplication) appContext).updateContactList(JID, user);
			}
			
			roster.addRosterListener(new RosterListener() {

				@Override
				public void entriesAdded(Collection<String> arg0) { }

				@Override
				public void entriesDeleted(Collection<String> arg0) { }

				@Override
				public void entriesUpdated(Collection<String> arg0) { }

				@Override
				public void presenceChanged(Presence presence) {
					// TODO Auto-generated method stub
					System.out.println("Presence changed:" + presence.getFrom() + " " + presence);
					String JID = presence.getFrom();
					String user = StringUtils.parseName(JID);
					String resource = StringUtils.parseResource(JID);
					if (resource.contains("duo-deck")){ 
						((DuoDeckApplication) appContext).updateContactList(JID, user);
						System.out.println("JID: " + JID + " , user: " + user);
						listener.getRosterResponse();
					}
				}
				
			});
		}
	}
	
	public void cleanupSession() {
		if (session != null) {
			System.out.println("Cleaning up session between " + session.getMyName() + " and " + session.getBuddyName());
			session.close(this);
		}
		session = null;
		((DuoDeckApplication) appContext).setInviteStartTime(null);
		((DuoDeckApplication) appContext).setSessionLastMsgTime(null);
		((DuoDeckApplication) appContext).setCurrentGameState(GameStates.Solo);
	}
	
	public void acceptInvite(){
		try {
			DuoDeckMessage.create(DuoDeckMessage.MessageType.InviteResponse, "Yes")
					.put(DuoDeckMessage.MessageKey.Response, Boolean.TRUE.toString())
					.send(session);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			this.cleanupSession();
			errorReported(e);
		}
	}
	
	public void declineInvite() {
		try {
			DuoDeckMessage.create(DuoDeckMessage.MessageType.InviteResponse, "Not right now")
					.put(DuoDeckMessage.MessageKey.Response, Boolean.FALSE.toString())
					.send(session);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XMPPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.cleanupSession();
	}
	
	public void inviteBuddy(String buddyName) {
		try {
			if (((DuoDeckApplication) appContext).getCurrentGameState() == GameStates.BuddyInviting) {
				System.out.println("there is a notification pending from Buddy");
				return;
			}
			((DuoDeckApplication) appContext).setInviteStartTime(new Date(System.currentTimeMillis()));
			if (session != null){
				if (session.getBuddyName().equals(buddyName)) {
					System.out.println("Invite sent inside if");
					session.sendInvite();
					return;
				}
				else
					session.close(this);
			}
			Chat c;
			try {
				c = xmppConnection.getChatManager().createChat(buddyName, APP_CHAT_RESOURCE, this);
			} catch (IllegalArgumentException e) { // generally there is already a thread id
				c = xmppConnection.getChatManager().createChat(buddyName, this);
			}
			session = new DuoDeckSession(c, this.username, buddyName, this);
			session.sendInvite();
			System.out.println("Invite sent");
			session.sendInvite();
		} catch (Exception e) {
			e.printStackTrace();
			this.cleanupSession();
			errorReported(e);
		}
	}
	
	public void sendShuffledOrder() {
		int[] deckOrder = ((DuoDeckApplication) appContext).getDeckOrder();
		System.out.println("Sending shuffled order " + Arrays.toString(deckOrder));
		if (deckOrder != null) {
			String deckStr = Arrays.toString(deckOrder);
			try {
				DuoDeckMessage.create(DuoDeckMessage.MessageType.SendShuffledDeck, deckStr).send(session);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				this.listener.errorReported(e);
			}
				
		} else {
			System.out.println("Deckorder is not set yet");
		}
	}
	
	public void sendShuffledOrderResponse(boolean success) {
		try {
			if (success) {
				DuoDeckMessage.create(DuoDeckMessage.MessageType.ShuffledDeckResponse, "Yes")
				.put(DuoDeckMessage.MessageKey.Response, Boolean.TRUE.toString())
				.send(session);
			} else {
				DuoDeckMessage.create(DuoDeckMessage.MessageType.ShuffledDeckResponse, "No")
				.put(DuoDeckMessage.MessageKey.Response, Boolean.FALSE.toString())
				.send(session);
				cleanupSession();
			}
		} catch (Exception e) {
			e.printStackTrace();
			this.listener.errorReported(e);
		}
	}
	
	public void doneWithCardIndex(int buddyIndex, int myIndex) {
		try {
			DuoDeckMessage.create(DuoDeckMessage.MessageType.DoneWithCardIndex, "Inform")
			.put("BuddyIndex", Integer.toString(buddyIndex))
			.put("MyIndex", Integer.toString(myIndex))
			.send(session);
		} catch (Exception e) {
			e.printStackTrace();
			this.listener.errorReported(e);
		}		
	}
	
	@Override
	public void chatCreated(Chat chat, boolean createdLocal) {
		// TODO Auto-generated method stub
		
		if (!createdLocal) { 
			String buddyName = StringUtils.parseName(chat.getParticipant());
			if (session == null || StringUtils.parseName(session.getBuddyName()).equals(buddyName)) {
				if (session != null) session.close(this);
				session = new DuoDeckSession(chat, username, buddyName, this);
				chat.addMessageListener(this);
				System.out.println("Chat session created with " + buddyName);
			} else if (chat.getThreadID().contains(APP_CHAT_RESOURCE)){
				System.out.println("Sorry, we are alreay in a workout session");
				DuoDeckSession temp = new DuoDeckSession(chat, username, buddyName, this);
				try {
					// we are already in a work-out session, so decline invite
					DuoDeckMessage.create(DuoDeckMessage.MessageType.InviteResponse, Boolean.FALSE.toString())
								  .send(temp);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (XMPPException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
				temp = null;
			}
		} 
		System.out.println("New chat session created");
	}

	@Override
	public synchronized void processMessage(Chat chat, Message message) {
		// TODO Auto-generated method stub
		System.out.println("Inside processMessage: " + message + " and " + message.getBody());
		if (listener != null) {
			((DuoDeckApplication) appContext).setSessionLastMsgTime(new Date(System.currentTimeMillis()));
			try {
				DuoDeckMessage properties = DuoDeckMessage.fromMessageString(message.getBody());
				String fromJID = properties.getProperty(DuoDeckMessage.MessageKey.User);
				switch(properties.getType()) {
				case Invite:
					System.out.println("Invite: " + message.getBody());
					((DuoDeckApplication) appContext).setInviteStartTime(new Date(System.currentTimeMillis()));
					this.notifyInvite(properties);
					((DuoDeckApplication) appContext).setCurrentGameState(GameStates.BuddyInviting);
					break;
				case InviteResponse:
					System.out.println("Invite Response: " + message.getBody());
					boolean accepted = properties.getBooleanProperty(DuoDeckMessage.MessageKey.Response);
					this.listener.inviteResponse(chat.getParticipant(), accepted);
					if (!accepted) {
						cleanupSession();
					} else {
						((DuoDeckApplication) appContext).setInviteStartTime(null);
						((DuoDeckApplication) appContext).setCurrentGameState(GameStates.StartingDuoPlayAsSender);
					}
					break;
				case SendShuffledDeck:
					System.out.println("Received shuffled deck");
					String deckOrder = properties.getProperty(DuoDeckMessage.MessageType.SendShuffledDeck);
					this.listener.processShuffledDeck(fromJID, deckOrder);
					break;
				case ShuffledDeckResponse:
					System.out.println("Received shuffled deck response");
					boolean success = properties.getBooleanProperty(DuoDeckMessage.MessageKey.Response);
					this.listener.shuffledDeckResponse(fromJID, success);
					break;
				case DoneWithCardIndex:
					System.out.println("Done with Card index");
					int buddyIndex = Integer.parseInt(properties.getProperty("BuddyIndex"));
					int myIndex = Integer.parseInt(properties.getProperty("MyIndex"));
					this.listener.dockWithCardIndex(fromJID, buddyIndex, myIndex);
					break;
				case Close:
					System.out.println("Closing session");
					cleanupSession();
				}
			} catch (IOException e) {
				e.printStackTrace();
				listener.errorReported(e);
			}
		}
	}
	
	private void notifyInvite(DuoDeckMessage prop) {
		String fromJID = prop.getProperty(DuoDeckMessage.MessageKey.User);
		System.out.println("Received invite from: " + fromJID);
		if (listener != null) {
			listener.invite(StringUtils.parseName(fromJID));
		} else {
			this.declineInvite();
		}
	}
	
	public void errorReported(Exception e) {
		this.listener.errorReported(e);
	}
	
	public static void close() {
		//make sure to call this method at the end of the work-out completion
		if (isInitiated()) {
			instance.disconnect();
			instance.xmppConnection = null;
			instance = null;
		}
	}

}
