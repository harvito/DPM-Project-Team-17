/*
 * SquareDriver.java
 */
package ev3Odometer;

import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class Driver extends Thread {
	private static final int FORWARD_SPEED = 150;
	private static final int ROTATE_SPEED = 100;
	private Odometer odometer;
	private EV3LargeRegulatedMotor leftMotor;
	private EV3LargeRegulatedMotor rightMotor;
	private double leftRadius;
	private double rightRadius;
	private double width;
	private double thetaTgt = 0;
	private int locations = 0;
	private double replacer = 0;
	
	
	// constructor
	public Driver(Odometer odometer,EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, double leftRadius, double rightRadius, double width) {
		this.odometer = odometer;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.leftRadius = leftRadius;
		this.rightRadius = rightRadius;
		this.width = width;
	}

	// run method (required for Thread)
	public void run() {
		
		double[] position = new double[3];
		double[][] locationMode1;
				
		if (Lab3.MODE == 1) { //the waypoints for mode 1 (no obstacles)
			double[][] locationMode2 = {{60.0,30.0,63.42},{30.0,30.0,270.0},{30.0,60.0,0.0},{60.0,0.0,154.43}};
			locationMode1 = locationMode2; //variable location mode 1 used throughout
			width = 15.5; //wheel separation
		}
		else { //waypoints for mode 2 (obstacle in centre)
			double[][] locationMode3 = {{0.0,60.0,0.0},{60.0,0.0,132.5}};
			locationMode1 = locationMode3;
			width = 16.0;
		}
		
		// reset the motors
		for (EV3LargeRegulatedMotor motor : new EV3LargeRegulatedMotor[] { leftMotor, rightMotor }) {
			motor.stop();
			motor.setAcceleration(3000);
		}

		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// there is nothing to be done here because it is not expected that
			// the odometer will be interrupted by another thread
		}
		
		
		if (Lab3.isNavigating) { //not currently performing an evasive maneuver
		
			for (locations = 0; locations < locationMode1.length ; locations++) {
				//First, let's determine our x, y and theta position (of the robot) :
				odometer.getPosition(position, new boolean[] { true, true, true });			
				
				if (locations == 0)
					replacer = position[2]; //replacer used to and normalize authenticate information given to turnTo
				else						//used to achieve turning of a minimal angle
					replacer = locationMode1[locations-1][2];
				
				
				if (replacer < locationMode1[locations][2]) {
					if ((locationMode1[locations][2] - replacer) < 180)
						turnTo(locationMode1[locations][2] - replacer);
					else {
						turnTo(-(360 - (locationMode1[locations][2] - replacer)));
					}
				}
				else {
					if ((locationMode1[locations][2] - replacer) < 0)
						thetaTgt = locationMode1[locations][2] - replacer + 360;
					else
						thetaTgt = (locationMode1[locations][2] - replacer);
					if (thetaTgt < 180)
						turnTo(thetaTgt);
					else
						turnTo(-(360 - (locationMode1[locations][2] - replacer)));
				}
				
				//travel to these coordinates
				travelTo(locationMode1[locations][0] - position[0], locationMode1[locations][1] - position[1]);
			}	
			
			while (true) {
				if(Lab3.MODE == 3){
					turnRight(Math.atan(position[1]/(locationMode1[1][0] - position[0])));
					travelTo(locationMode1[locations][0] - position[0], locationMode1[locations][1] - position[1]);
			break;
				}
			}
		}
		
	}

	void travelTo(double x, double y){
		double disToTravel = Math.sqrt((x*x) + (y*y)); //pythagoras theorem
		
		leftMotor.setSpeed(FORWARD_SPEED);
		rightMotor.setSpeed(FORWARD_SPEED);

		leftMotor.rotate(convertDistance(leftRadius, disToTravel), true);
		rightMotor.rotate(convertDistance(rightRadius, disToTravel), false);
	}
	
	void turnTo(double theta) {
		if (theta > 0)
			turnRight(theta);
		else
			turnLeft(-theta);
	}
	
	void turnLeft(double theta) { //turn left minimal angle
		leftMotor.setSpeed(ROTATE_SPEED);
		rightMotor.setSpeed(ROTATE_SPEED);
		
		leftMotor.rotate(-convertAngle(leftRadius, width, theta), true);
		rightMotor.rotate(convertAngle(rightRadius, width, theta), false);
	}

	void turnRight(double theta) { //turn right minimal angle
		leftMotor.setSpeed(ROTATE_SPEED);
		rightMotor.setSpeed(ROTATE_SPEED);
		
		leftMotor.rotate(convertAngle(leftRadius, width, theta), true);
		rightMotor.rotate(-convertAngle(rightRadius, width, theta), false);
	}
	
	private double angleCorrection(double theta) { //normalizing btw 0 and 360
		if(theta < 0)
			return theta + 360;
		else if(theta > 360)
			return theta - 360;
		else
			return theta;
	}
	
	boolean isNavigating() {
		return false;
		
	}

	private static int convertDistance(double radius, double distance) { //convert a distance to a wheel rotation, in degrees
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}

	private static int convertAngle(double radius, double width, double angle) { //convert an angle to be turned to a wheel rotation, in degrees
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}
}