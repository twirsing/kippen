/*
 * Copyright (C) 2001, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * See file LICENSE (or LICENSE.html) for more information.
 */

package com.illposed.osc;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;


public class AbletonOSCListener implements OSCListener {
	
	static Logger log =  Logger.getLogger(AbletonOSCListener.class.getName());
	
	private OSCMessage message;
	private boolean isMessageReceived = false;
	Date receivedTimestamp;
	
	public OSCMessage getMessage() {
		return message;
	}

	public boolean isMessageReceived() {
		return isMessageReceived;
	}

	@Override
	public void acceptMessage(Date time, OSCMessage message) {
		receivedTimestamp = time;

		this.message = message;
		isMessageReceived = true;
	}
}