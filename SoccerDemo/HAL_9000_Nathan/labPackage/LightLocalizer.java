/*
 * Authors: Ivan Arredondo (260566638), Nicola Polifroni (260690864)
 * Last updated on Tuesday February 16nd 2016
 */
package labPackage;

import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.SensorMode;
import lejos.robotics.SampleProvider;

public class LightLocalizer {
	public static double ROTATION_SPEED = 110;
	public final double sensorDistance = 12.1;
	private Navigation nav;
	private Odometer odo;
	private SampleProvider colorValue;
	private EV3ColorSensor colorSensor;
	private float[] colorData;
	private EV3LargeRegulatedMotor leftMotor;
	private EV3LargeRegulatedMotor rightMotor;

	public LightLocalizer(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, Navigation nav, Odometer odometer, SampleProvider colorValue, float[] colorData, EV3ColorSensor colorSensor) {
		this.nav = nav;
		this.odo = odometer;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.colorValue = colorValue;
		this.colorSensor = colorSensor; 
		this.colorData = colorData;
	}

	public void doLocalization() {


		colorSensor.setFloodlight(true);						// setting up the sensor
		SensorMode sensorMode = colorSensor.getRedMode();

		while(true){
			sensorMode.fetchSample(colorData, 0);
			if(colorData[0] * 10 < 4){
				Sound.beep();
				break;
			}
		}

		nav.goForward(-10);

		double x;
		double y;

		double[][] locationArray = new double[4][3];

		nav.turnRight();

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
		odo.setPosition(new double[] {x , y , 0.0} , new boolean[] {true,true,false});
	
		
		


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
}