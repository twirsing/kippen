package vis2;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import processing.core.PApplet;
import processing.data.JSONObject;

public class KippenInterface extends PApplet {
	public CubeObject[] cubes = new CubeObject[4];
	public BarrelObject[] barrels = new BarrelObject[3];

	public int cubeWidth = 170;
	public int cubeY = 100;
	public int rectHeight = 250;
	public int rectWidth = cubeWidth;
	public int rectY = 300;

	// static ServerSocket variable
	private static ServerSocket server;
	// socket server port on which it will listen
	private static int PORT = 9876;
	public static int flashTime = 0;

	public void setup() {
		size(displayWidth, displayHeight);

		frameRate(15);
		background(0);

		int offset = (width - (4 * cubeWidth)) / 5;

		// add cubes
		cubes[0] = new CubeObject(this, offset, cubeY, cubeWidth, color(252, 87, 122));
		cubes[1] = new CubeObject(this, 2 * offset + cubeWidth, cubeY, cubeWidth, color(255, 200, 200));
		cubes[2] = new CubeObject(this, 3 * offset + 2 * cubeWidth, cubeY, cubeWidth, color(83, 243, 195));
		cubes[3] = new CubeObject(this, 4 * offset + 3 * cubeWidth, cubeY, cubeWidth, color(143, 250, 86));

		noFill();
		// add rectangles
		barrels[0] = new BarrelObject(this, offset, rectY);
		barrels[1] = new BarrelObject(this, offset * 2 + rectWidth, rectY);
		barrels[2] = new BarrelObject(this, offset * 3 +  2 * rectWidth, rectY);

		new Thread(new MessageServer(this)).start();
	}

	public void draw() {
//		System.out.println();
//		if(KippenInterface.flashTime > 0){
//			System.out.println("in flash");
//			background(color(0,0,255,70));
//			KippenInterface.flashTime--;
//		}
		background(0);
		textSize(13);
		
		fill(color(255,255,255));
		text("UM-KIPPEN V1.1", (width / 2), 40);

		for (int i = 0; i < cubes.length; i++) {
			cubes[i].draw();
		}

		for (int i = 0; i < barrels.length; i++) {
			noFill();
			barrels[i].draw();
		}
	}

	private class MessageServer implements Runnable {
		private KippenInterface canvas;

		public MessageServer(KippenInterface canvas) {
			this.canvas = canvas;
		}

		public void run() {
			try {
				server = new ServerSocket(PORT);

				while (true) {
					//System.out.println("Waiting for client request on port " + PORT);
					// creating socket and waiting for client connection
					Socket socket = server.accept();
					// read from socket to ObjectInputStream object
					InputStream ois = socket.getInputStream();
					// convert ObjectInputStream object to String

					JSONObject jsonObject = new JSONObject(new InputStreamReader(ois));

					if (jsonObject.hasKey("command")) {
						if (jsonObject.getString("command").equals("sideChange")) {
							int cubeNumber = jsonObject.getInt("trackNumber");
							int clipNumber = jsonObject.getInt("clipNumber");
							// canvas.objects[cubeNumber].start();
							canvas.cubes[cubeNumber].sideChange(clipNumber);
						}

						else if (jsonObject.getString("command").equals("stop")) {
							int cubeNumber = jsonObject.getInt("trackNumber");
							canvas.cubes[cubeNumber].stop();
						}

						else if (jsonObject.getString("command").equals("barrelRoll")) {
							int trackNumber = jsonObject.getInt("trackNumber");
							Double value = Double.valueOf(jsonObject.getString("value"));
							int valueInt = (int) (value * rectHeight);
							canvas.barrels[trackNumber].setValue(valueInt);
						}
						
						else if (jsonObject.getString("command").equals("ball")) {
//							KippenInterface.flashTime = 200;
//							System.out.println("flash");
						}
					}

					// close resources
					ois.close();
					socket.close();
					// terminate the server if client sends exit request
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	public static void main(String args[]) {
		PApplet.main(new String[] { "--present", "vis2.KippenInterface" });
	}

}

class BarrelObject {

	int value = 50;
	KippenInterface canvas;
	int flashCount = 0;
	
	int x, y;

	public BarrelObject(KippenInterface canvas, int x, int y) {
		this.canvas = canvas;
		this.x = x;
		this.y = y;
	}

	public void draw() {
	
		canvas.rect(x, y, canvas.rectWidth, canvas.rectHeight);

		canvas.fill(canvas.color(255, 255, 255, 50));
		canvas.rect(x, y + canvas.rectHeight - value, canvas.rectWidth, value);
	}

	public void setValue(int value) {
		this.value = value;
	

	}
}

class CubeObject {
	KippenInterface canvas;
	
	int flashColor;

	float x;
	float y;
	int cubeWidth;
	
	int flashCount = 0;

	private int state = -1;

	public CubeObject(KippenInterface canvas, int x, int y, int cubeWidth, int flashColor) {
		this.canvas = canvas;
		this.x = x;
		this.y = y;
		this.cubeWidth = cubeWidth;
		this.flashColor = flashColor;
	}

	public int getCubeState() {
		return state;
	}

	public void stop() {
		state = -2;
	}

	public void sideChange(int clipNumber) {
		this.flashCount = 3;
		this.state = clipNumber;
	}

	private String getClipnumberString(int clipNumber) {

		switch (clipNumber) {
		case 0:
			return "MUTE";
		case 1:
			return "1";
		case 2:
			return "2";
		case 3:
			return "3";
		case 4:
			return "4";
		case 5:
			return "5";
		case -1:
			return "TOUCH ME!";
		case -2:
			return "TOUCH ME!";
		}
		return "err";
	}

	public void draw() {
		canvas.stroke(canvas.color(255, 255, 255));
		canvas.fill(canvas.color(255, 255, 255));
		canvas.textSize(28);
		canvas.textAlign(canvas.CENTER, canvas.CENTER);
		canvas.stroke(canvas.color(255, 255, 255));
		canvas.text(this.getClipnumberString(this.state), x + cubeWidth / 2, y + cubeWidth / 2);
		if(flashCount > 0){
			canvas.fill(flashColor);
			this.flashCount--;
		}
		else
			canvas.noFill();
		canvas.rect(x, y, cubeWidth, cubeWidth);
	}
}
