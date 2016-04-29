package labPackage;

import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.SensorMode;
import lejos.robotics.SampleProvider;

/**sets odometer x and y values.
 * doLocalization is primary method.
 * 
 * @author Ivan, Nicola
 *
 */
public class LightLocalizer {
	public static double ROTATION_SPEED = 250;
	public final double sensorDistance = 12.1;
	private Navigation nav;
	private Odometer odo;
	private SampleProvider colorValue;
	private EV3ColorSensor colorSensor;
	private float[] colorData;
	private EV3LargeRegulatedMotor leftMotor;
	private EV3LargeRegulatedMotor rightMotor;
	
	/**constructor. assigns like values.
	 * 
	 * @param leftMotor
	 * @param rightMotor
	 * @param nav
	 * @param odometer
	 * @param colorValue
	 * @param colorData
	 * @param colorSensor
	 */
	public LightLocalizer(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, Navigation nav, Odometer odometer, SampleProvider colorValue, float[] colorData, EV3ColorSensor colorSensor) {
		this.nav = nav;
		this.odo = odometer;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.colorValue = colorValue;
		this.colorSensor = colorSensor; 
		this.colorData = colorData;
	}

	public void doLocalization(double xtarget, double ytarget) { 
		colorSensor.setFloodlight(true); //setting up the sensor
		SensorMode sensorMode = colorSensor.getRedMode();

		while(true){
			sensorMode.fetchSample(colorData, 0);
			if(colorData[0] * 10 < 4){ //black line detected. EV3 is close to target intersection
				Sound.beep();
				break;
			}
		}

		nav.goForward(-17); //approach intersection

		double x;
		double y;

		double[][] locationArray = new double[4][3];

		//begin turning to record relative line locations
		nav.turnRightFast();

		for(int i = 0 ; i < 4 ; i++){
			while(true){
				sensorMode.fetchSample(colorData, 0);
				if(colorData[0] * 10 < 4){
					Sound.beep();
					locationArray[i][0] = odo.getX();
					locationArray[i][1] = odo.getY();
					locationArray[i][2] = odo.getAng();
					break;
				}
			}
		}
		
		nav.setSpeeds(0, 0);
		x = xDisplacement(locationArray[1][2], locationArray[3][2]);
		y = yDisplacement(locationArray[0][2], locationArray[2][2]);
		odo.setPosition(new double[] {xtarget + x, ytarget + y, 0.0} , new boolean[] {true,true,false});
	
		
		


	}

	// calculates distance from x and y axis
	public double xDisplacement ( double theta1, double theta2){
		double x;
		x = -1 * sensorDistance * Math.cos(Math.toRadians((theta1-theta2)/2));
		return x;
	}

	// calculates distance from x and y axis
	public double yDisplacement (double theta1, double theta2){
		double y;
		y = -1 * sensorDistance * Math.cos(Math.toRadians((theta2-theta1)/2));  
		return y;
	}
	
	public double theta(double theta1, double theta2, boolean xdpos) {
		if (xdpos)
			return normal(theta2 - theta1) / 2.0 + theta1 + 180.0;
		else
			return normal(theta1 - theta2) / 2.0 + theta1; 
	}
	
	/**Returns an angle, in degrees, normalized to be between 0 and 360
	 * setup to be recursive, in case an angle is more than 360 degrees outside the set 0 to 360.
	 * 
	 * @param ang double. angle to be normalized.
	 * @return double angle, normalized between 0 and 360
	 */
	public double normal (double ang) {
		if (ang > 360.0)
			return normal (ang - 360.0);
		else if (ang < 0.0)
			return normal (ang + 360.0);
		else
			return ang;
	}
}