import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Calendar;
import java.util.Date;

import processing.core.PApplet;
import processing.data.JSONObject;

public class Visuals extends PApplet {
	public CubeObject[] objects = new CubeObject[4];

	private int session = 1;

	// static ServerSocket variable
	private static ServerSocket server;
	// socket server port on which it will listen
	private static int port = 9876;

	public void setup() {
		size(displayWidth, displayHeight);

		frameRate(20);
		background(0);
		text("UM-KIPPEN V1.0 Session#" + this.session, (width / 2) - 80, 40);

		objects[0] = new CubeObject(this, random(width), random(height), color(255, 204, 0), color(255, 100, 200, 15));
		objects[1] = new CubeObject(this, random(width), random(height), color(50, 200, 100), color(255, 250, 0, 15));
		objects[2] = new CubeObject(this, random(width), random(height), color(200, 102, 102), color(134, 122, 102, 15));
		objects[3] = new CubeObject(this, random(width), random(height), color(100, 250, 200), color(10, 200, 2, 15));

		new Thread(new MessageServer(this)).start();
	}

	
	private void reset(){
		saveFrame();
		background(0);
		session++;
		text("UM-KIPPEN V1.0: Session#" + this.session, (width / 2) - 40, 40);
	}

	private class MessageServer implements Runnable {
		private Visuals canvas;

		public MessageServer(Visuals canvas) {
			this.canvas = canvas;
		}

		public void run() {
			try {
				server = new ServerSocket(port);

				while (true) {
					System.out.println("Waiting for client request on port " + port);
					// creating socket and waiting for client connection
					Socket socket = server.accept();
					// read from socket to ObjectInputStream object
					InputStream ois = socket.getInputStream();
					// convert ObjectInputStream object to String

					JSONObject jsonObject = new JSONObject(new InputStreamReader(ois));
					System.out.println("Message Received: " + jsonObject);

					if (jsonObject.hasKey("command")) {
						if (jsonObject.getString("command").equals("sideChange")) {
							int cubeNumber = jsonObject.getInt("trackNumber");
							int clipNumber = jsonObject.getInt("clipNumber");
							System.out.println(cubeNumber);
							canvas.objects[cubeNumber].start();
							canvas.objects[cubeNumber].sideChange(cubeNumber);
						}

						else if (jsonObject.getString("command").equals("stop")) {
							int cubeNumber = jsonObject.getInt("trackNumber");
							canvas.objects[cubeNumber].stop();

							boolean oneRunning = false;
							// check if all are stopped
							for (CubeObject o : objects) {
							//	if (o.isRunning() && !o.getCurrentSide() == 0) {
								if (o.isRunning() ) {
									oneRunning = true;
									break;
								}
							}

							if (!oneRunning)
								reset();
						}

						else if (jsonObject.getString("command").equals("barrelRoll")) {
							int cubeNumber = jsonObject.getInt("trackNumber");
							Double value = Double.valueOf(jsonObject.getString("value"));
							int valueInt = (int) (value * 250);
							canvas.objects[cubeNumber].changeColor(valueInt);
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

	public void draw() {
		// background(50);

		for (int i = 0; i < objects.length; i++) {
			objects[i].draw();
		}
	}

	public static void main(String args[]) {
		PApplet.main(new String[] { "--present", "Visuals" });
	}

}

class CubeObject {
	private boolean isRunning = true;
	private int currentSide = 0;

	public int getCurrentSide() {
		return currentSide;
	}


	PApplet canvas;
	private int stroke;

	private int fill;

	float wander_offset;
	float x;
	float y;
	float wander_theta;
	float wander_radius;

	// bigger = more edgier, hectic
	float max_wander_offset = 0.1f;
	// bigger = faster turns
	float max_wander_radius = 2;

	CubeObject(PApplet canvas, float _x, float _y, int fill, int stroke) {
		this.canvas = canvas;
		this.fill = fill;
		this.stroke = stroke;
		x = _x;
		y = _y;

		wander_theta = canvas.random(PApplet.TWO_PI);
		wander_radius = canvas.random(max_wander_radius);

		wander_offset = canvas.random(-max_wander_offset, max_wander_offset);
	}

	public boolean isRunning() {
		return isRunning;
	}

	void stayInsideCanvas() {
		x %= canvas.width;
		y %= canvas.height;
	}

	void stop() {
		this.isRunning = false;
	}

	public void sideChange(int cubeNumber) {
		wander_offset = canvas.random(-max_wander_offset, max_wander_offset);
	}

	public void changeColor(int color) {
		this.stroke = canvas.color(255, color, color / 2, 15);
	}

	public void toggleIsRunning() {
		System.out.println("toggle");
		if (isRunning) {
			isRunning = false;
		} else
			isRunning = true;
	}

	void move() {
		if (isRunning) {
			// wander_offset = canvas.random(-max_wander_offset,
			// max_wander_offset);
			wander_theta += wander_offset;

			x += canvas.cos(wander_theta);
			y += canvas.sin(wander_theta);
		}
	}

	void start() {
		isRunning = true;
	}

	public void draw() {
		canvas.noFill();
		canvas.stroke(stroke);
		this.stayInsideCanvas();
		this.move();
		canvas.rect(x, y, 20, 20);

	}

}
