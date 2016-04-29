package labPackage;

import java.util.Arrays;

import lejos.hardware.Button;
import lejos.hardware.motor.*;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.SensorMode;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.SampleProvider;


/**ball loading, holding and shooting mechanism
 * uses an arm, claw, color sensor, ballista motor and holder motor
 * 
 * @author Mike
 *
 */
public  class ShooterMilestone {
	private EV3LargeRegulatedMotor lowerArmMotor, higherArmMotor; //lower is arm, higher is claw
	private RegulatedMotor shooter, loaderMotor;
	private Navigation nav;
	private Odometer odo;
	private SampleProvider colorValue;
	private EV3ColorSensor colorSensor;
	private float[] colorData = new float[3];
	private double angle;
	private double[] odoxyini = new double[3];
	private double[] odoxyfin = new double[3];
	
	
	/**constructor. set local variables to passed parameters
	 * 
	 * @param lowerArmMotor for arm movement
	 * @param higherArmMotor for claw movement
	 * @param shooter for winding and shooting
	 * @param loaderMotor for shifting balls in holding mechanism
	 * @param nav access to robot navigation
	 * @param odo access to position information
	 * @param colorValue for sensor values
	 * @param colorData
	 * @param colorSensor
	 * @param angle
	 */
	public ShooterMilestone(EV3LargeRegulatedMotor lowerArmMotor, EV3LargeRegulatedMotor higherArmMotor, RegulatedMotor shooter, RegulatedMotor loaderMotor, Navigation nav, Odometer odo, SampleProvider colorValue, float[] colorData, EV3ColorSensor colorSensor, double angle){
		this.lowerArmMotor = lowerArmMotor;
		this.higherArmMotor = higherArmMotor;
		this.shooter = shooter;
		this.loaderMotor = loaderMotor;
		this.nav = nav;
		this.odo = odo;
		this.colorValue = colorValue;
		this.colorSensor = colorSensor; 
		this.colorData = colorData;
		this.angle = angle;
		colorSensor.setCurrentMode("RGB");
	}
	
	/**Load balls of color targetBallColor into shooter and holder
	 * 
	 * @param targetBallColor integer of ball to pick up. 0 for red, 1 for blue, 2 for either
	 */
	public void loadBalls(int targetBallColor){
		nav.forward();
		nav.setSpeeds(60, 60); //go forward slowly
		odoxyini = odo.getPosition(); //remember initial position
		loaderMotor.setSpeed(80);
		lowerArmMotor.setAcceleration(200);
		lowerArmMotor.setSpeed(190);
		higherArmMotor.setSpeed(80);
		lowerArmMotor.rotate(-120); //release color sensor
		lowerArmMotor.rotate(120);
		
		
		//pick up the desired balls. exit if 4 balls have been loaded or if the rover has traveled 2 tiles
		for (int i = 0; i < 4 && Math.sqrt(((odoxyfin[0] - odoxyini[0]) * (odoxyfin[0] - odoxyini[0])) + ((odoxyfin[1] - odoxyini[1]) * (odoxyfin[1] - odoxyini[1]))) < 61.92; ) {
			
			if(isDesiredBall(targetBallColor)) {
				nav.goForwardSpeed(1.0, 60);
				nav.setSpeeds(0,0);
				shooter.setSpeed(450);
				if (i == 0) {
					lowerArmMotor.rotate(-15); //bring upright
					shooter.rotate(-1000, true); //winding up shooter
					higherArmMotor.rotate(150); //opening claw. START W CLAW FULLY CLOSED
					lowerArmMotor.rotate(-120); //lowering arm
					shooter.rotate(1000, true); //unwind
					higherArmMotor.rotate(-130); //closing claw grabbing ball
					lowerArmMotor.rotate(135); //bringing up arm
					higherArmMotor.rotate(50); //opening claw
					lowerArmMotor.rotate(-15, true); //bring upright
					loaderMotor.rotate(363, true); //make room for new ball
				}
				else if (i == 1 || i == 2) {
					lowerArmMotor.rotate(-120); //lowering arm
					higherArmMotor.rotate(-50); //closing claw; grabbing ball
					lowerArmMotor.rotate(135); //bringing up arm
					higherArmMotor.rotate(50); //opening claw
					lowerArmMotor.rotate(-15, true); //bring upright
					loaderMotor.rotate(91, true); //make room for new ball
				}
				else {
					lowerArmMotor.rotate(-120); //lowering arm
					higherArmMotor.rotate(-50); //closing claw; grabbing ball
					lowerArmMotor.rotate(135); //bringing up arm
					higherArmMotor.rotate(50); //opening claw
					lowerArmMotor.rotate(-15); //bring upright
				}
				i++;
				nav.setSpeeds(60, 60);
			}
			odoxyfin = odo.getPosition();
		
		try {
			Thread.sleep(300); //repoll every 300 ms
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	nav.setSpeeds(0, 0);
	}
	
	/**shoots all 4 balls one at a time.
	 * loading time is roughly 6 seconds
	 * 
	 */
	public void shootAll() {
		shooter.rotate(500);//shoot
		shooter.rotate(-1500); //release trigger and wind shuttle
		shooter.rotate(1000); //unwind excess string
		for (int j = 0; j < 3; j++) {
			loaderMotor.rotate(91); //load next ball
			shooter.rotate(500);
			shooter.rotate(-1500);
			shooter.rotate(1000);
		}
	}
	
	/**checks if the sensed ball is the desired color
	 * 
	 * @param clr the desired color. 0 for red, 1 for blue, 2 for either
	 * @return true if the ball is desirable
	 */
	public boolean isDesiredBall(int clr) {
		switch (clr) {
		case 0:
			return isRedBall();
		case 1:
			return isBlueBall();
		case 2:
			return (isRedBall() || isBlueBall());
		}
		return false;
	}
	
	/**returns whether or not the sensed ball is red
	 * 
	 * @return true if the ball is red
	 */
	public boolean isRedBall() {
		return (getFilteredRValue() >= 0.015 && getFilteredRValue() < 2.0);  //values above 2.0 are unstable and suspect
	}
	
	/**returns whether or not the sensed ball is blue
	 * 
	 * @return true if the ball is blue
	 */
	public boolean isBlueBall() {
		return (getFilteredBValue() >= 0.008 && getFilteredBValue() < 2.0); //values above 2.0 are unstable and suspect
	}
	
	/**takes 5 red color samples and returns the lowest value
	 * value is a float between 0.0 and 10.0
	 * 
	 * @return the lowest of 5 consecutive red color readings
	 */
	public float getFilteredRValue() {
		float[] temp = new float[5];
		for(int i = 0 ; i < 5; i++) {
			colorSensor.fetchSample(colorData, 0);
			temp[i] = colorData[0];
		}
		Arrays.sort(temp);
		return temp[0]; //return the lowest value
	}
	
	/**takes 5 blue color samples and returns the lowest value
	 * value is a float between 0.0 and 10.0
	 * 
	 * @return the lowest of 5 consecutive blue color readings
	 */
	public float getFilteredBValue() {
		float[] temp = new float[5];
		for(int i = 0 ; i < 5; i++) {
			colorSensor.fetchSample(colorData, 0);
			temp[i] = colorData[2];
		}
		Arrays.sort(temp);
		return temp[0]; //return the lowest value
	}

}
