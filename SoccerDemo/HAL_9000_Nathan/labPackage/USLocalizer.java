package labPackage;

import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;
import lejos.robotics.SampleProvider;

public class USLocalizer {
	public enum LocalizationType { FALLING_EDGE, RISING_EDGE };
	public static float ROTATION_SPEED = 70;
	public double angleLeft = 0.0;
	public double angleRight = 0.0;


	private Odometer odo;
	private SampleProvider usSensor1;
	private float[] usData1;
	private SampleProvider usSensor2;
	private float[] usData2;
	private LocalizationType locType;
	private Navigation nav;

	private static boolean isMeasured  = false;
	private double distance; 
	private double distanceUS1; 
	private double distanceUS2; 
	private final double distanceThreshold = 30;
	private double sideAngle;
	private int square;


	public USLocalizer(Odometer odo,  SampleProvider usSensor1, float[] usData1,  SampleProvider usSensor2, float[] usData2, LocalizationType locType, int square) {
		this.square = square;
		this.odo = odo;
		this.usSensor1 = usSensor1;
		this.usData1 = usData1;
		this.usSensor2 = usSensor2;
		this.usData2 = usData2;
		this.locType = locType;
		this.nav = new Navigation(odo);
	}

	public double getSideAngle(){
		int square = this.square;
		double sideAngle = 0.0;
		if( square == 1)
			sideAngle = 90.0;
		else if (square == 2)
			sideAngle = 180.0; 
		else if (square == 3)
			sideAngle = 270.0;
		else if (square == 4)
			sideAngle = 0.0;
		return sideAngle;
	}
	
	public void doLocalization() {

		nav.turnRightFast();

		while(true){
			if(getFilteredData1() > 50 && getFilteredData2() > 50)
				break;
		}
		
		while(true){
			if(getFilteredData1() < 30){
				odo.setPosition(new double[] {0.0,  0.0, 0.0}, new boolean[] {false, false, true});
				nav.turnTo(110, true);
				nav.goForward(-25);
				odo.setPosition(new double[] {0.0,  0.0, getSideAngle()}, new boolean[] {false, false, true});
				nav.goForward(11);
				nav.turnToNoError(0, true);
				nav.forward();
				break;
			}
		}		


	}

	private float getFilteredData1() {
		usSensor1.fetchSample(usData1, 0);
		float distance = usData1[0]*100;

		if (distance > 60)														
			distance = 60;																				

		return distance;
	}

	private float getFilteredData2() {
		usSensor2.fetchSample(usData2, 0);
		float distance = usData2[0]*100;

		if (distance > 60)														
			distance = 60;																				

		return distance;
	}

	private double adjustAngle(double angle){
		if(angle > 360.0)
			return (angle - 360.0);
		else if(angle < 0)
			return (angle + 360.0);
		else 
			return angle;
	}

}