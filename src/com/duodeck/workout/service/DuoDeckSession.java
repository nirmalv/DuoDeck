package com.duodeck.workout.service;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.XMPPException;

public class DuoDeckSession {

	private Chat chat;
	private String myName;
	private String buddyName;
	private boolean accepted;
	DuoDeckConnectionManager holder;
	
	DuoDeckMessage failureMessageBuffer;
	int failureAttempt = 0;
	
	Timer t;  
	TimerTask retryFailure = new TimerTask() {
		@Override
		public void run() {
			if (failureAttempt >= 6) {
				if (t!=null) {t.cancel(); t = null;}
				holder.errorReported(new XMPPException("Retry attempts exeeded"));
			} else if (failureMessageBuffer != null) {
				try {
					sendMessage(failureMessageBuffer);
				} catch (Exception e) {
					e.printStackTrace();
					failureAttempt++;
				} 
			} else {
				failureAttempt = 0;
				if (t!=null) {t.cancel(); t = null;}
			}
		}
	};
	
	public DuoDeckSession(Chat chat, String myName, String buddyName, DuoDeckConnectionManager holder) {
		this.chat = chat;
		this.myName = myName;
		this.buddyName = buddyName; 
		this.holder = holder;
	}
	
	public void sendInvite() throws IOException, XMPPException {
		DuoDeckMessage.create(DuoDeckMessage.MessageType.Invite, buddyName)
					  .send(this);
	}
	
	public void sendMessage(DuoDeckMessage message) throws XMPPException, IOException {
		try {
			if (failureMessageBuffer != null && !failureMessageBuffer.equals(message)) { // the last message got through
				failureMessageBuffer = null; failureAttempt = 0; 
				if (t != null) {
					t.cancel();
					t = null;
				}
			} 
			sendMessage(message.put(DuoDeckMessage.MessageKey.User, myName).toMessageString());
		} catch (XMPPException e) {
			if (t == null) {
				failureMessageBuffer = message;
				t = new Timer();
				t.scheduleAtFixedRate(retryFailure, 10000, 10000);
			} else
				throw e;
		}
	}
	
	public void setChat(Chat c) {
		if (this.chat != null)
			this.chat.removeMessageListener(holder);
		this.chat = c;
	}
	
	public void sendMessage(String message) throws XMPPException {
		chat.sendMessage(message);
		failureMessageBuffer = null;
		failureAttempt = 0;
		if (t!=null) {t.cancel(); t = null;}
	}
	
	public String getBuddyName() {
		return buddyName;
	}
	
	public String getMyName() {
		return myName;
	}
	
	public void close(DuoDeckConnectionManager conn) {
		try {
			DuoDeckMessage.create(DuoDeckMessage.MessageType.Close, buddyName)
			  .send(this);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XMPPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (conn != null)
			chat.removeMessageListener(conn);
	}

	public boolean isAccepted() {
		return accepted;
	}

	public void setAccepted(boolean accepted) {
		this.accepted = accepted;
	}
	
	
}
