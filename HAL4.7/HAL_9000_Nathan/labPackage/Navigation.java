package labPackage;

import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class Navigation {
	final static int FAST = 250, SLOW = 200, ACCELERATION = 2000;
	final static double DEG_ERR = 3.0, CM_ERR = 0.2;
	private Odometer odometer;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	private float motorStraight = 0, rotationSpeed = 0;
	private double RADIUS = 2.05, TRACK = 24.6;

	public Navigation(Odometer odo) {
		this.odometer = odo;

		EV3LargeRegulatedMotor[] motors = this.odometer.getMotors();
		this.leftMotor = motors[0];
		this.rightMotor = motors[1];

		// set acceleration
		this.leftMotor.setAcceleration(ACCELERATION);
		this.rightMotor.setAcceleration(ACCELERATION);
	}

	/*
	 * Functions to set the motor speeds jointly
	 */
	public void setSpeeds(float lSpd, float rSpd) {
		this.leftMotor.setSpeed(lSpd);
		this.rightMotor.setSpeed(rSpd);
		if (lSpd < 0)
			this.leftMotor.backward();
		else
			this.leftMotor.forward();
		if (rSpd < 0)
			this.rightMotor.backward();
		else
			this.rightMotor.forward();
	}

	public void setSpeeds(int lSpd, int rSpd) {
		this.leftMotor.setSpeed(lSpd);
		this.rightMotor.setSpeed(rSpd);
		if (lSpd < 0)
			this.leftMotor.backward();
		else
			this.leftMotor.forward();
		if (rSpd < 0)
			this.rightMotor.backward();
		else
			this.rightMotor.forward();
	}

	/*
	 * Float the two motors jointly
	 */
	public void setFloat() {
		this.leftMotor.stop();
		this.rightMotor.stop();
		this.leftMotor.flt(true);
		this.rightMotor.flt(true);
	}


	/*
	 * TravelTo function which takes as arguments the x and y position in cm Will travel to designated position, while
	 * constantly updating it's heading
	 */
	public void travelTo(double x, double y) {
		int speed = 150;
		double minAng;
		while (Math.abs(x - odometer.getX()) > CM_ERR || Math.abs(y - odometer.getY()) > CM_ERR) {
			minAng = (Math.atan2(y - odometer.getY(), x - odometer.getX())) * (180.0 / Math.PI);
			if (minAng < 0)
				minAng += 360.0;
			this.turnTo(minAng, false);
			this.setSpeeds(speed, speed);
		}
		this.setSpeeds(0, 0);
	}

	public void stopMoving() {																	 		// This method will just stop the motors

		this.rightMotor.setSpeed(0);
		this.leftMotor.setSpeed(0);
		this.leftMotor.forward();
		this.rightMotor.forward();
	}

	/*
	 * TurnTo function which takes an angle and boolean as arguments The boolean controls whether or not to stop the
	 * motors when the turn is completed
	 */
	public void turnTo(double angle, boolean stop) {

		double error = angle - this.odometer.getAng();

		while (Math.abs(error) > DEG_ERR) {

			error = angle - this.odometer.getAng();

			if (error < -180.0) {
				this.setSpeeds(-SLOW, SLOW);
			} else if (error < 0.0) {
				this.setSpeeds(SLOW, -SLOW);
			} else if (error > 180.0) {
				this.setSpeeds(SLOW, -SLOW);
			} else {
				this.setSpeeds(-SLOW, SLOW);
			}
		}


		if (stop) {
			this.setSpeeds(0, 0);
		}
	}
	
	
	//TurnTo with no error, as in it's supposed to turn slower for no wheel slip, it is basically the same as TurnTo
	public void turnToNoError(double angle, boolean stop) {

		double error = angle - this.odometer.getAng();
		int speed = 200;

		while (Math.abs(error) > 0.5) {

			error = angle - this.odometer.getAng();

			if (error < -180.0) {
				this.setSpeeds(speed, speed);
			} else if (error < 0.0) {
				this.setSpeeds(speed, -speed);
			} else if (error > 180.0) {
				this.setSpeeds(speed, -speed);
			} else {
				this.setSpeeds(-speed, speed);
			}
		}


		if (stop) {
			this.setSpeeds(0, 0);
		}
	}
	
	/*
	 * Go foward a set distance in cm
	 */
	public void goForward(double distance) {
		//	this.travelTo(Math.cos(Math.toRadians(this.odometer.getAng())) * distance, Math.cos(Math.toRadians(this.odometer.getAng())) * distance);
		leftMotor.setSpeed(FAST);
		rightMotor.setSpeed(FAST);
		leftMotor.rotate(convertDistance(RADIUS, distance), true);
		rightMotor.rotate(convertDistance(RADIUS, distance), false);

	}
	
	public void goForwardwSpeed(double distance, int speed) {
		//	this.travelTo(Math.cos(Math.toRadians(this.odometer.getAng())) * distance, Math.cos(Math.toRadians(this.odometer.getAng())) * distance);
		leftMotor.setSpeed(speed);
		rightMotor.setSpeed(speed);
		leftMotor.rotate(convertDistance(RADIUS, distance), true);
		rightMotor.rotate(convertDistance(RADIUS, distance), false);

	}


	public void setRotationSpeed(float speed) {
		rotationSpeed = speed;
		setSpeeds(rotationSpeed, -rotationSpeed);

	}

	public void turnLeft(){
		this.setSpeeds(-SLOW, SLOW);
	}

	public void turnRight(){
		this.setSpeeds(SLOW, -SLOW);
	}

	public void turnLeftFast(){
		this.setSpeeds(-FAST, FAST);
	}

	public void turnRightFast(){
		this.setSpeeds(FAST, -FAST);
	}

	public void turnLeftSlow(){
		this.setSpeeds(-20, 20);
	}

	public void turnRightSlow(){
		this.setSpeeds(20, -20);
	}

	public void forward(){
		this.setSpeeds(FAST, FAST);
	}

	/*
	 * Converts the desired distance into rotations using the radius of the wheels
	 */
	public static int convertDistance(double radius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}

	/*
	 * Converts the desired angle into rotations using the radius of the wheels and width between them
	 */
	public static int convertAngle(double radius, double width, double angle) {
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}
}


