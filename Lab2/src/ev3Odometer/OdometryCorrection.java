/* 
 * OdometryCorrection.java
 */
package ev3Odometer;

import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.sensor.EV3ColorSensor;

public class OdometryCorrection extends Thread {
	private static final long CORRECTION_PERIOD = 10;
	private Odometer odometer;
	private EV3ColorSensor colorSensor;
	float[] intensity = {0}; //
	private double t; //a sample of theta from odometer class, accessed later
	private final double offSet = 1.0; // distance from sensor to centre of rotation
	
	

	// constructor
	public OdometryCorrection(Odometer odometer, EV3ColorSensor colorSensor) { //changed to take in a color sensor
		this.odometer = odometer;
		this.colorSensor = colorSensor; //take in colorSensor as instantiated in Lab2.java
	}

	// run method (required for Thread)
	public void run() {
		long correctionStart, correctionEnd;

		while (true) {
			correctionStart = System.currentTimeMillis();
			colorSensor.getRedMode().fetchSample(intensity,0); // take a sample of light intensity and place is in intensity[0]

			//CORRECTION CODE
			if(intensity[0] < 0.3){ //black line detected
				Sound.buzz();
				t = odometer.getTheta(); //get information about current direction of travel
				
				if (t > 350 || t < 10) { //traveling up if started at bottom left
					if (odometer.getY() < 30.0)
						odometer.setY(15.0 - offSet); //correct odometer to 15 or 45, whichever is closer to current reading
					else
						odometer.setY(45.0 - offSet);
				}
				else if (t > 80 && t < 100) { //traveling RIGHT
					if (odometer.getX() < 30.0)
						odometer.setX(15.0 - offSet);
					else
						odometer.setX(45.0 - offSet);
				}
				else if (t > 170 && t < 190) { //traveling DOWN
					if (odometer.getY() < 30.0)
						odometer.setY(15.0 + offSet);
					else
						odometer.setY(45.0 + offSet);
				}
				else if (t > 260 && t < 280) { //traveling LEFT
					if (odometer.getX() < 30.0)
						odometer.setX(15.0 + offSet);
					else
						odometer.setX(45.0 + offSet);
				}
			}

			// this ensures the odometry correction occurs only once every period
			correctionEnd = System.currentTimeMillis();
			if (correctionEnd - correctionStart < CORRECTION_PERIOD) {
				try {
					Thread.sleep(CORRECTION_PERIOD
							- (correctionEnd - correctionStart));
				} catch (InterruptedException e) {
					// there is nothing to be done here because it is not
					// expected that the odometry correction will be
					// interrupted by another thread
				}
			}
		}
	}
}