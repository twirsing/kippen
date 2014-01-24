package nerdproject;

import java.io.IOException;

public class AbletonCommunicationException extends IOException {
	public AbletonCommunicationException(IOException e){
		super(e);
	}
}
