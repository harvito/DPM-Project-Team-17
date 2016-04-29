package labPackage;

import java.lang.reflect.Array;
import java.util.Arrays;

import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.robotics.SampleProvider;

public class USLocalizer {
	public enum LocalizationType { FALLING_EDGE, RISING_EDGE };
	public static double ROTATION_SPEED = 30;
	public static float ROTATION_SPEED_FLOAT = 70;
	public static double publicAngleA;
	public static double publicAngleB;

	private Odometer odo;
	private SampleProvider usSensor;
	private float[] usData;
	private LocalizationType locType;
	private Navigation nav;
	float[] filterMedian = new float[5]; //0 is most recent, 4 is earliest
	
	public USLocalizer(Odometer odo,  SampleProvider usSensor, float[] usData, LocalizationType locType, Navigation nav) {
		this.odo = odo;
		this.usSensor = usSensor;
		this.usData = usData;
		this.locType = locType;
		this.nav = nav;
	}
	
	public void doLocalization() {
		double [] pos = new double [3];
		double angleA = 0.0, angleB = 0.0;
		double wallDistance = 16.0; //Placed by N LARUE
		double distance;
		int count = 0;
		
		for (int i = 0; i < 5; i++)
			filterMedian[i] = getRawData();
		
		if (locType == LocalizationType.FALLING_EDGE) {
			
			// rotate the robot until it sees no wall
			nav.rightTurn(ROTATION_SPEED_FLOAT);
			
			while(true){
				if(getFilteredData() > wallDistance){
					break;
				}
			}
			
			odo.setPosition(new double [] {0.0, 0.0, 0}, new boolean [] {true, true, true});
			
			// keep rotating until the robot sees a wall, then latch the angle
			while(true){
				
				//If the distance recored by the sensor is smaller than 30 (then if there's a wall) [...]
				if(getFilteredData() < wallDistance){
					
					//[...] and save the first theta with the angle.
					Sound.beep();
					angleA = odo.getAng();
					publicAngleA = angleA;
					break;
				}
			}
			
			//Stop the motors:
			nav.stop();
			
			
			//Turn 15 degrees to mitigate to precedent value
			nav.turnTo(angleCorrector(odo.getAng() + 35), true);
			nav.stop();
			
			// switch direction and wait until it sees no wall
			nav.leftTurn( ROTATION_SPEED_FLOAT);
			
			// keep rotating until the robot sees a wall, then latch the angle
			while(true){
				//If the distance recored by the sensor is smaller than 30 (then if there's a wall) [...]
				if(getFilteredData() < wallDistance){
					Sound.beep();
					//[...] and save the first theta with the angle.
					nav.stop();
					angleB = odo.getAng();
					publicAngleB = angleB;
					break;
				}
			}
			
			//Calculate the distance between the actual position and the real position:
			distance = (angleB+angleA)/2 - 225;

			// update the odometer position (example to follow:)
			odo.setPosition(new double [] {0.0, 0.0, angleCorrector(angleB - distance)}, new boolean [] {true, true, true});
			
			nav.turnTo( 0 ,true);
			
			odo.setPosition(new double [] {0.0, 0.0, 0.0}, new boolean [] {true, true, true});
			// angleA is clockwise from angleB, so assume the average of the
			// angles to the right of angleB is 45 degrees past 'north'

			nav.stop();
			nav.forward();
			
		} else {
			/*
			 * The robot should turn until it sees the wall, then look for the
			 * "rising edges:" the points where it no longer sees the wall.
			 * This is very similar to the FALLING_EDGE routine, but the robot
			 * will face toward the wall for most of it.
			 */
			
			nav.rightTurn(ROTATION_SPEED_FLOAT);
			
		
			while(true){
				
				//If the distance recored by the sensor is smaller than 30 (then if there's a wall) [...]
				if(getFilteredData() < wallDistance){
					
					//[...] and save the first theta with the angle.
					angleA = odo.getAng();
					publicAngleA = angleA;
					break;
				}
			}
			
			while(true){
				
				//If the distance recored by the sensor is smaller than 30 (then if there's a wall) [...]
				if(getFilteredData() > wallDistance){
					
					//[...] and save the first theta with the angle.
					angleB = odo.getAng();
					publicAngleB = angleB;
					nav.stop();
					break;
				}
			}
			

			
			//Calculate the distance between the actual position and the real position:
			distance = (angleB-angleA)/2 - 240;
			
			// update the odometer position (example to follow:)
			odo.setPosition(new double [] {0.0, 0.0, angleB - distance}, new boolean [] {true, true, true});
			
			nav.turnTo( 0 ,true);
			
			odo.setPosition(new double [] {0.0, 0.0, 0.0}, new boolean [] {true, true, true});
			
		}
	}
	
	public double angleCorrector(double angle){
		if(angle > 360){
			return (angle-360);
		}else if(angle < 0){
			return (angle + 360);
		}else{
			return angle;
			
		}
	}
	
	public float getFilteredData() {
		float[] temp = new float[5];
		for(int i = 0 ; i < 5; i++)
			temp[i] = getRawData();
		Arrays.sort(temp);
		return temp[2]; //return the median value
	}
	
	public float getRawData() {
		usSensor.fetchSample(usData, 0);
		return (float) (usData[0]*100.0);
	}
}
