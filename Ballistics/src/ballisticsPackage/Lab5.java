package ballisticsPackage;

import lejos.hardware.*;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.*;
import lejos.robotics.SampleProvider;

public class Lab5 {

	private static final EV3LargeRegulatedMotor loaderMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A")); //for spring loading
	private static final EV3MediumRegulatedMotor launcherMotor = new EV3MediumRegulatedMotor(LocalEV3.get().getPort("B")); //

	
	public static void main(String[] args) { //hard coded since it is very simple
		Button.waitForAnyPress();
		loaderMotor.setSpeed(360); //not too fast
		loaderMotor.rotate(1080); //wind back the shuttle 
		loaderMotor.rotate(-1080); //unwind the string to prevent resistance when firing

		launcherMotor.setSpeed(360);
		launcherMotor.rotate(180); //pull the trigger
		launcherMotor.rotate(-180); //trigger back to locking position
		
	}
}
