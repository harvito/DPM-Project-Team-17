// Lab3.java

package ev3Odometer;

import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.robotics.SampleProvider;
import ev3Odometer.PController;
import ev3Odometer.UltrasonicPoller;
import lejos.hardware.Button;

public class Lab3 {
	
	// Static Resources:
	// Left motor connected to output A
	// Right motor connected to output D
	public static final EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
	public static final EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
	public static final EV3LargeRegulatedMotor sensorMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("B"));
	private static final Port usPort = LocalEV3.get().getPort("S2");
	//public static final EV3UltrasonicSensor usSensor = new EV3UltrasonicSensor(LocalEV3.get().getPort("S3"));

	// Constants
	public static final double LEFT_WHEEL_RADIUS = 2.035; //tweaked to 2.13
	public static final double RIGHT_WHEEL_RADIUS = 2.035;
	public static final double TRACK = 15.5; //measured and tweaked
	public static int MODE = 2;
	
	public static boolean isNavigating = false;

	private static final int bandCenter = 33;			// Offset from the wall (cm)
	private static final int bandWidth = 2;				// Width of dead band (cm)
	private static final int motorLow = 125;			// Speed of slower rotating wheel (deg/sec)
	private static final int motorHigh = 250;			// Speed of the faster rotating wheel (deg/seec)

	public static void main(String[] args) {
		int buttonChoice;

		// some objects that need to be instantiated
		
		final TextLCD t = LocalEV3.get().getTextLCD();
		Odometer odometer = new Odometer();
		Driver driver = new Driver(odometer,leftMotor, rightMotor, LEFT_WHEEL_RADIUS, RIGHT_WHEEL_RADIUS, TRACK);
		PController p = new PController(odometer, leftMotor, rightMotor, sensorMotor, bandCenter, bandWidth, motorLow, motorHigh, LEFT_WHEEL_RADIUS, RIGHT_WHEEL_RADIUS, TRACK);
		OdometryDisplay odometryDisplay = new OdometryDisplay(odometer,t,p);
		UltrasonicPoller usPoller = null;		

		@SuppressWarnings("resource")							    // Because we don't bother to close this resource
		SensorModes usSensor = new EV3UltrasonicSensor(usPort);		// usSensor is the instance
		SampleProvider usDistance = usSensor.getMode("Distance");	// usDistance provides samples from this instance
		float[] usData = new float[usDistance.sampleSize()];		// usData is the buffer in which data are returned
		
		do {
			// clear the display
			t.clear();

			// ask the user if this run should incorporate object avoidance or not
			t.drawString("< Left: Navigation", 0, 0);
			t.drawString(">Right: Avoidance", 0, 1);

			buttonChoice = Button.waitForAnyPress();
			
		} while (buttonChoice != Button.ID_LEFT
				&& buttonChoice != Button.ID_RIGHT);

		if (buttonChoice == Button.ID_LEFT) { //no object avoidance (mode 1)
			isNavigating = true;
			MODE = 1;
			odometer.start();
			odometryDisplay.start();
			driver.start();			
			
		}else if (buttonChoice == Button.ID_RIGHT) { //with object avoidance (mode 2)
			isNavigating = true;
			MODE = 2;
			odometer.start();
			odometryDisplay.start();
			driver.start();	
			usPoller = new UltrasonicPoller(usDistance, usData, p);
			usPoller.start(); //class that samples US readings
			
		}
		
		while (Button.waitForAnyPress() != Button.ID_ESCAPE);
		System.exit(0);
	}
}