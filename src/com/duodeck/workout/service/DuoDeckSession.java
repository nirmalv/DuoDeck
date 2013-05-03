package com.duodeck.workout.service;

import java.io.IOException;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.XMPPException;

public class DuoDeckSession {

	private Chat chat;
	private String myName;
	private String buddyName;
	private boolean accepted;
	
	public DuoDeckSession(Chat chat, String myName, String buddyName) {
		this.chat = chat;
		this.myName = myName;
		this.buddyName = buddyName;
	}
	
	public void sendInvite() throws IOException, XMPPException {
		DuoDeckMessage.create(DuoDeckMessage.MessageType.Invite, buddyName)
					  .send(this);
	}
	
	public void sendMessage(DuoDeckMessage message) throws XMPPException, IOException {
		sendMessage(message.put(DuoDeckMessage.MessageKey.User, myName).toMessageString());
	}
	
	public void sendMessage(String message) throws XMPPException {
		chat.sendMessage(message);
	}
	
	public String getBuddyName() {
		return buddyName;
	}
	
	public String getMyName() {
		return myName;
	}
	
	public void buddyAccepted(String user) {
		if (buddyName.equals(buddyName))
			accepted = true;
	}
	
	public void buddyDeclined(String user) {
		if (buddyName.equals(buddyName)) 
			accepted = false;
	}
	
	public void close(DuoDeckConnectionManager conn) {
		if (conn != null)
			chat.removeMessageListener(conn);
	}
}
