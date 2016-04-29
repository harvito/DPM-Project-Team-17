package labPackage;
import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

/**allows simplified and organized control of robot's main driving motors.
 * 
 * @author Ivan, Nate, Mike
 *
 */
public class Navigation {
	final static int FAST = 250, SLOW = 150, ACCELERATION = 1000;
	final static double DEG_ERR = 1, CM_ERR = 3;
	private Odometer odometer;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	private float rotationSpeed = 0;
	private double leftRadius = 2.05, rightRadius = 2.05;
	public boolean keepTravelling = true;
	public boolean doneTurning = true;
	public boolean doneGoingForward = false;
	
	/**standard constructor
	 * 
	 * @param odo, odometer
	 * @param leftMotor
	 * @param rightMotor
	 */
	public Navigation(Odometer odo, EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor) {
		this.odometer = odo;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		// set acceleration
		this.leftMotor.setAcceleration(ACCELERATION);
		this.rightMotor.setAcceleration(ACCELERATION);
	}
	
	/**Set the motor speeds jointly, with floats, and start rotating indefinitely
	 *
	 * 
	 * @param float lSpd the speed at which the left wheel should turn, in deg per second
	 * @param float rSpd the speed at which the right wheel should turn, in deg per second
	 *
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
	
	/**Set the motor speeds jointly, with integers, and start rotating indefinitely
	 *
	 * 
	 * @param float lSpd the speed at which the left wheel should turn, in deg per second
	 * @param float rSpd the speed at which the right wheel should turn, in deg per second
	 *
	 */
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

	 /**stop then float the two motors jointly
	  * motors will freely rotate if torque is applied
	 */
	public void setFloat() {
		this.leftMotor.stop();
		this.rightMotor.stop();
		this.leftMotor.flt(true);
		this.rightMotor.flt(true);
	}
	
	 /**TravelTo function which takes as arguments the x and y position in cm will travel to designated position, while
	 * constantly updating its heading
	 * 
	 * @param double x: x coordinate of destination
	 * @param double y: y coordinate of destination
	 * @param boolean unnecessaryBool: enter true
	 */
	public void travelTo(double x, double y, boolean unneccessaryBool) {
		double minAng;
		minAng = (Math.atan2(y - odometer.getY(), x - odometer.getX())) * (180.0 / Math.PI); //computing minimal turn angle
		if (minAng < 0)
			minAng += 360.0;
		this.turnTo(minAng, false);
		while (Math.abs(x - odometer.getX()) > CM_ERR || Math.abs(y - odometer.getY()) > CM_ERR && this.keepTravelling) {
			this.doneTurning = unneccessaryBool;
			this.setSpeeds(FAST, FAST);
		}
		this.setSpeeds(0, 0);
	}
	
	/**travel in only the x direction, for use in GoCloseToBalls class
	 * 
	 * @param x destination
	 * @param y current
	 * @param factorOfNegOneDependingOnBallRack if ball rack's right side is aligned with a gridline
	 */
	public void travelToBallsX(double x, double y, int factorOfNegOneDependingOnBallRack) {
		int speed = 250;
		double minAng;
		minAng = (Math.atan2(y - odometer.getY(), x - odometer.getX())) * (180.0 / Math.PI);
		if (minAng < 0)
			minAng += 360.0;
		this.turnTo(minAng, true);
		while ((factorOfNegOneDependingOnBallRack*(x - odometer.getX()) > CM_ERR ) && this.keepTravelling) {
			this.setSpeeds(speed, speed);
		}
		while ((x - odometer.getX() < -CM_ERR) && this.keepTravelling) {
			this.setSpeeds(-speed, -speed);
		}
		this.setSpeeds(0, 0);
	}
	
	/**travel in only the y direction, for use in GoCloseToBalls class
	 * 
	 * @param x current
	 * @param y destination 
	 * @param factorOfNegOneDependingOnBallRack if ball rack's right side is aligned with a gridline
	 */
	public void travelToBallsY(double x, double y) {
		int speed = 250;
		double minAng;
		minAng = (Math.atan2(y - odometer.getY(), x - odometer.getX())) * (180.0 / Math.PI);
		if (minAng < 0)
			minAng += 360.0;
		this.turnTo(minAng, true);
		while ((y - odometer.getY() > CM_ERR) && this.keepTravelling) {
			this.setSpeeds(speed, speed);
		}
		while ((y - odometer.getY() < -CM_ERR) && this.keepTravelling) {
			this.setSpeeds(-speed, -speed);
		}
		this.setSpeeds(0, 0);
	}
	
	/**travel in only the x direction
	 * 
	 * @param x destination
	 * @param y current
	 * @param setDoneTurning unimplemented
	 */
	public void travelToXDirection(double x,double y, boolean setDoneTurning) {
		int speed = 200;
		double minAng;
		minAng = (Math.atan2(0, x - odometer.getX())) * (180.0 / Math.PI);
		if (minAng < 0)
			minAng += 360.0;
		this.turnToForXDirection(minAng, true);
		while (Math.abs(x - odometer.getX()) > CM_ERR  && this.keepTravelling) {
			this.setSpeeds(speed, speed);
		}
		this.setSpeeds(0, 0);
	}
	
	/**travel in only the y direction
	 * 
	 * @param x current
	 * @param y destination
	 * @param stop stops turning when complete if true
	 */
	public void turnToForXDirection(double angle, boolean stop) {
		double error = angle - this.odometer.getAng();
		while (Math.abs(error) > 5  && this.keepTravelling) {
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
	
	/**
	 * stops driving motors and sets them in forward mode
	 */
	public void stopMoving() {																	 		// This method will just stop the motors
		this.rightMotor.setSpeed(0);
		this.leftMotor.setSpeed(0);
		this.leftMotor.forward();
		this.rightMotor.forward();
	}
	
	/**
	 * TurnTo function which takes an angle and boolean as arguments
	 * The boolean controls whether or not to stop the
	 * motors when the turn is completed
	 */
	public void turnTo(double angle, boolean stop) {
		double error = angle - this.odometer.getAng();
		while (Math.abs(error) > DEG_ERR  && this.keepTravelling) {
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
	
	/**turn to face given direction quickly
	 * 
	 * @param angle the direction to turn towards
	 * @param stop stop turning when angle is reached if true
	 */
	public void turnToFast(double angle, boolean stop) {
		double error = angle - this.odometer.getAng();
		while (Math.abs(error) > 10  && this.keepTravelling) {
			error = angle - this.odometer.getAng();
			if (error < -180.0) {
				this.setSpeeds(-250, 250);
			} else if (error < 0.0) {
				this.setSpeeds(250, -250);
			} else if (error > 180.0) {
				this.setSpeeds(250, -250);
			} else {
				this.setSpeeds(-250, 250);
			}
		}
		if (stop) {
			this.setSpeeds(0, 0);
		}
	}
	
	/**
	 *TurnTo with no error, as in it's supposed to turn slower for no wheel slip, it is basically the same as TurnTo
	 * 
	 * @param angle the direction to turn towards
	 * @param stop stop turning when angle is reached if true
	 */
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
	
	/**
	 * Go forward a set distance in cm. for use in the Y direction
	 * 
	 * @param distance double. the distance to go forward in cm.
	 */
	public void goForwardY(double distance) {
		double YVal = odometer.getY();
		this.setSpeeds(FAST, FAST);
		leftMotor.rotateTo(convertDistance(leftRadius, distance), true);
		rightMotor.rotateTo(convertDistance(rightRadius, distance), false);
	}
	
	/**
	 * Go forward a set distance in cm. for general use
	 * 
	 * @param distance double. the distance to go forward in cm.
	 */
	public void goForward(double distance) {
		leftMotor.setSpeed(FAST);
		rightMotor.setSpeed(FAST);
		leftMotor.rotate(convertDistance(leftRadius, distance), true);
		rightMotor.rotate(convertDistance(rightRadius, distance), false);
	}
	
	/**
	 * Go forward a set distance in cm. for use in the X direction
	 * 
	 * @param distance double. the distance to go forward in cm.
	 */
	public void goForwardX(double distance) {
		double XVal = odometer.getX();
		this.setSpeeds(FAST, FAST);
		leftMotor.rotateTo(convertDistance(leftRadius, distance), true);
		rightMotor.rotateTo(convertDistance(rightRadius, distance), false);
	}
	
	/**
	 * Go forward a set distance in cm with speed control as a parameter
	 * 
	 * @param distance double. the distance to go forward in cm.
	 * @param speed int. speed at which wheels should rotate, in deg per second
	 */
	public void goForwardSpeed(double distance, int speed) {
		//	this.travelTo(Math.cos(Math.toRadians(this.odometer.getAng())) * distance, Math.cos(Math.toRadians(this.odometer.getAng())) * distance);
		leftMotor.setSpeed(speed);
		rightMotor.setSpeed(speed);
		leftMotor.rotate(convertDistance(leftRadius, distance), true);
		rightMotor.rotate(convertDistance(rightRadius, distance), false);
		Sound.beepSequence();
	}
	
	/**set speed of wheels, in deg per second, to passed value for rover rotation ccw.
	 * sets left wheel to speed and right wheel to -speed
	 * 
	 * @param speed float. the speed at which wheels should rotate, in deg per second
	 */
	public void setRotationSpeed(float speed) {
		rotationSpeed = speed;
		setSpeeds(rotationSpeed, -rotationSpeed);
	}
	
	/**turn the rover left medium speed, indefinitely.
	 * 
	 */
	public void turnLeft(){
		this.setSpeeds(-SLOW, SLOW);
	}
	
	/**turn the rover right medium speed, indefinitely.
	 * 
	 */
	public void turnRight(){
		this.setSpeeds(SLOW, -SLOW);
	}
	
	/**turn the rover left quickly indefinitely.
	 * 
	 */
	public void turnLeftFast(){
		this.setSpeeds(-FAST, FAST);
	}
	
	/**turn the rover right quickly indefinitely.
	 * 
	 */
	public void turnRightFast(){
		this.setSpeeds(250, -250);
	}
	
	/**turn the rover left slowly indefinitely.
	 * 
	 */
	public void turnLeftSlow(){
		this.setSpeeds(-20, 20);
	}
	
	/**turn the rover right slowly indefinitely.
	 * 
	 */
	public void turnRightSlow(){
		this.setSpeeds(20, -20);
	}
	
	/**set robot to go forward quickly indefinitely
	 * 
	 */
	public void forward(){
		this.setSpeeds(FAST, FAST);
	}
	
	/**navigate to the passed coordinates.
	 * calculates smallest angle rotation and appropriate distance.
	 * returns only once destination is reached
	 * 
	 * @param x x coordinate of destination
	 * @param y y coordinate of destination
	 */
	public void travelToSimple(double x, double y){
		leftMotor.setSpeed(200);
		rightMotor.setSpeed(200);
		double distance = Math.sqrt((x*x + y*y));
		while(odometer.getX()< (x-2) && odometer.getY() < (y-2)){
			leftMotor.rotate(convertDistance(2.05, distance ), true);
			rightMotor.rotate(convertDistance(2.05, distance), true);
		}
	}
	
	/**
	 * turn BY theta degrees
	 * @param theta
	 * @param leftFactor
	 * @param rightFactor
	 */
	public void turnToSimple(double theta, int leftFactor, int rightFactor) {
		leftMotor.setSpeed(200);
		rightMotor.setSpeed(200);
		leftMotor.rotate(leftFactor*convertAngle(2.06, 24.1, theta), true);
		rightMotor.rotate(rightFactor*convertAngle(2.06, 24.1, theta), false);
	}
	
	/**
	 * Converts the desired distance into rotations using the radius of the wheels
	 */
	public static int convertDistance(double radius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}
	
	/**
	 * Converts the desired angle into rotations using the radius of the wheels and width between them
	 */
	public static int convertAngle(double radius, double width, double angle) {
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}
	
	/**setter for local boolean keepTravelling variable
	 * used for obstacle avoidance
	 * 
	 * @param shouldItTravel true if robot should continue traveling normally
	 */
	public void setKeepTravelling(boolean shouldItTravel){
		this.keepTravelling = shouldItTravel;
	}
	
	/**setter for local boolean doneTurning variable
	 * used for obstacle avoidance
	 * 
	 * @param shouldItTurn true if robot is finished turning and should continue traveling normally
	 */
	public void setDoneTurning(boolean shouldItTurn){
		this.doneTurning = shouldItTurn;
	}
}