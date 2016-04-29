package labPackage;

import lejos.hardware.Sound;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.robotics.SampleProvider;

public class LightLocalizer {
	private Odometer odo;
	private SampleProvider colorSensor;
	private float[] colorData;
	private double lightDistance = 11.5;
	private int count = 0;
	private double [][] points = new double [4][2];
	private double xOffset = 0.0;
	private double yOffset = 0.0;
	public static double publicXOffset = 0.0;
	public static double publicYOffset = 0.0;
	float[] intensity = {0}; //
	
	public LightLocalizer(Odometer odo, SampleProvider colorSensor, float[] colorData) {
		this.odo = odo;
		this.colorSensor = colorSensor;
		this.colorData = colorData;
	}
	
	public void doLocalization(Navigation nav) {
		// drive to location listed in tutorial
		// start rotating and clock all 4 gridlines
		// do trig to compute (0,0) and 0 degrees
		// when done travel to (0,0) and turn to 0 degrees

		nav.goForward(12);
		nav.rightTurn(70);
		
		while(count < 4){ //acquire 4 samples
			colorSensor.fetchSample(intensity,0);
			
			if(intensity[0] < 0.3){ //dark line
				points[count][0] = Math.cos(odo.getAng()*Math.PI/180) * lightDistance; //stored as distances
				points[count][1] = Math.sin(odo.getAng()*Math.PI/180) * lightDistance;
				Sound.beep();
				count++;
			}
		}
		
		nav.stop();
		xOffset = points[1][0];
		yOffset = points[0][1];
		
		publicXOffset = xOffset;
		publicYOffset = yOffset;
		//new positioning
		odo.setPosition(new double [] {xOffset, yOffset, odo.getAng()}, new boolean [] {true, true, true});
		
		nav.forward();
		nav.travelTo(0.0, 0.0);
		nav.turnTo(0.0, true);
		nav.stop();
		
		
		
	}
}
