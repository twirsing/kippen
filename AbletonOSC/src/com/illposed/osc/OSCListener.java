/*
 * Copyright (C) 2003-2006, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * See file LICENSE (or LICENSE.html) for more information.
 */

package com.illposed.osc;

import java.util.Date;

/**
 * Interface for things that listen for incoming OSC Messages
 *
 * @author Chandrasekhar Ramakrishnan
 */
public abstract class OSCListener {
	protected boolean isMessageReceived = false;
	protected Date receivedTimestamp = null;
	
	protected OSCMessage message = null;
	/**
	 * Accept an incoming OSCMessage
	 * @param time     The time this message is to be executed.
	 *          <code>null</code> means execute now
	 * @param message  The message to execute.
	 */
	public abstract void acceptMessage(Date time, OSCMessage message);
	
	public boolean isMessageReceived(){
		return this.isMessageReceived;
	}
	
	public OSCMessage getMessage(){
		return this.message;
	};
	
	public Date getReceivedTimestamp() {
		return receivedTimestamp;
	}

}
