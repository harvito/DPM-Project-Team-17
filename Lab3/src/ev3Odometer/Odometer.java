/*
 * Odometer.java
 */

package ev3Odometer;

import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class Odometer extends Thread {
	// robot position
	private double x, y, theta, displacement;
	
	//last tachometer readings (for calculating difference)
	double lastTachoL = 0;
	double lastTachoR = 0;
	double thetaIni = 0; //
	
	// odometer update period, in ms
	private static final long ODOMETER_PERIOD = 25;

	// lock object for mutual exclusion
	private static Object lock;

	// default constructor
	public Odometer() {
		x = 0.0;
		y = 0.0;
		theta = 0.0;
		lock = new Object();
		
	}

	// run method (required for Thread)
	public void run() {
		long updateStart, updateEnd;
		

		while (true) {
			updateStart = System.currentTimeMillis();
			//ODOMETER CODE------------------------------------
			
			double tachoL = Lab3.leftMotor.getTachoCount(); //live sample
			double tachoR = Lab3.rightMotor.getTachoCount();
			double leftDistance = ( (tachoL - lastTachoL) / 180 ) * Math.PI * Lab3.LEFT_WHEEL_RADIUS; //distance travelled since last iteration
			double rightDistance = ( (tachoR - lastTachoR) / 180 ) * Math.PI * Lab3.RIGHT_WHEEL_RADIUS;
			lastTachoL = tachoL; //reset variable
			lastTachoR = tachoR;
			double deltaD = 0.5*(leftDistance + rightDistance); //change in distance traveled
			double deltaT = (leftDistance - rightDistance) / Lab3.TRACK; //change in facing angle rad
			thetaIni = thetaIni + deltaT; //theta rad
			
			synchronized (lock) {
				// don't use the variables x, y, or theta anywhere but here!
				theta = thetaIni * 180 / Math.PI; //theta in deg
				if (theta > 360) //normalization to 0 to 360 deg
					theta -= 360;
				else if (theta < 0)
					theta += 360;
				
				x = x + deltaD * Math.sin(theta*Math.PI/180); //update x coordinate
				y = y + deltaD * Math.cos(theta*Math.PI/180); //update y coordinate
				
			}

			// this ensures that the odometer only runs once every period
			updateEnd = System.currentTimeMillis();
			if (updateEnd - updateStart < ODOMETER_PERIOD) {
				try {
					Thread.sleep(ODOMETER_PERIOD - (updateEnd - updateStart));
				} catch (InterruptedException e) {
					// there is nothing to be done here because it is not
					// expected that the odometer will be interrupted by
					// another thread
				}
			}
		}
	}

	// accessors
	public void getPosition(double[] position, boolean[] update) {
		// ensure that the values don't change while the odometer is running
		synchronized (lock) {
			if (update[0])
				position[0] = x;
			if (update[1])
				position[1] = y;
			if (update[2])
				position[2] = theta;
		}
	}

	public double getX() {
		double result;

		synchronized (lock) {
			result = x;
		}

		return result;
	}

	public double getY() {
		double result;

		synchronized (lock) {
			result = y;
		}

		return result;
	}

	public double getTheta() {
		double result;

		synchronized (lock) {
			result = theta;
		}

		return result;
	}

	// mutators
	public void setPosition(double[] position, boolean[] update) {
		// ensure that the values don't change while the odometer is running
		synchronized (lock) {
			if (update[0])
				x = position[0];
			if (update[1])
				y = position[1];
			if (update[2])
				theta = position[2];
		}
	}

	public void setX(double x) {
		synchronized (lock) {
			this.x = x;
		}
	}

	public void setY(double y) {
		synchronized (lock) {
			this.y = y;
		}
	}

	public void setTheta(double theta) {
		synchronized (lock) {
			this.theta = theta;
		}
	}
}