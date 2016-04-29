package labPackage;
import lejos.robotics.SampleProvider;
public class USLocalizer {
	public enum LocalizationType { FALLING_EDGE, RISING_EDGE };
	public static double ROTATION_SPEED = 30;
	private Odometer odo;
	private SampleProvider usSensor;
	private float[] usData;
	private LocalizationType locType;
	private Navigation nav;
	
	public USLocalizer(Odometer odo,  SampleProvider usSensor, float[] usData, LocalizationType locType, Navigation nav) {
		this.odo = odo;
		this.usSensor = usSensor;
		this.usData = usData;
		this.locType = locType;
		this.nav = nav;
	}
	
	public void doLocalization() {
		double [] pos = new double [3];
		double angleA, angleB;
		double wallDistance = 20.0;
		
		if (locType == LocalizationType.FALLING_EDGE) {
			// rotate the robot until it sees no wall
			//nav.turnTo(1, false);
			
			// keep rotating until the robot sees a wall, then latch the angle
			while(true){
				
				//If the distance recored by the sensor is smaller than 30 (then if there's a wall) [...]
				if(getFilteredData() < wallDistance){
					
					//[...] then update your actual location [...]
					odo.setPosition(new double [] {0.0, 0.0, 0.0}, new boolean [] {true, true, true});
					pos = odo.getPosition();
					
					//[...] and save the first theta with the angle.
					angleA = pos[2];
					nav.setSpeeds(0, 0);
					break;
				}
			}
			
			// switch direction and wait until it sees no wall
			nav.turnTo(359, false);
			
			//Wait two seconds to make sure that we don't scan the same wall again:
			//-----------------------------------------------------------------------------------------------------
			
			// keep rotating until the robot sees a wall, then latch the angle
			while(true){
				
				//If the distance recored by the sensor is smaller than 30 (then if there's a wall) [...]
				if(getFilteredData() < wallDistance){
					
					//[...] then update your actual location [...]
					odo.setPosition(new double [] {0.0, 0.0, 0.0}, new boolean [] {true, true, true});
					pos = odo.getPosition();
					
					//[...] and save the first theta with the angle.
					angleB = pos[2];
					nav.setSpeeds(0, 0);
					break;
				}
			}
			
			// angleA is clockwise from angleB, so assume the average of the
			// angles to the right of angleB is 45 degrees past 'north'
			
			// update the odometer position (example to follow:)
			odo.setPosition(new double [] {0.0, 0.0, 0.0}, new boolean [] {true, true, true});
			
		} else {
			/*
			 * The robot should turn until it sees the wall, then look for the
			 * "rising edges:" the points where it no longer sees the wall.
			 * This is very similar to the FALLING_EDGE routine, but the robot
			 * will face toward the wall for most of it.
			 */
			
			//
			// FILL THIS IN
			//
		}
	}
	
	private float getFilteredData() {
		usSensor.fetchSample(usData, 0);
		float distance = usData[0];
				
		return distance;
	}
}