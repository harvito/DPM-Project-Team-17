package navigation;

import labPackage.Odometer;
import lejos.hardware.Sound;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.SensorMode;
import lejos.robotics.Color;


/* 
 * OdometryCorrection.java
 */

public class OdometryCorrection extends Thread {
	private static final long CORRECTION_PERIOD = 10;
	private static final int LIGHT_THRESHOLD = 400;	    //not used anymore, keep for now 
	
	private static final double CLOSE_LINE = 15.0;
	private static final double FAR_LINE = 45.0;
	private static final double LINE_SEPARATION = 30.96;
	
	private static final double SENSOR_DISTANCE = 12.1;    //play with 
	
	private final EV3ColorSensor colorSensor;
	private final Odometer odometer;
	
	private boolean frozen = false;
	private boolean wasLine = false;
	// x, y, theta of last line crossing
	private double[] previous;
	private double[] current;
	private float[] colorData = new float[1];
	
	// constructor
	public OdometryCorrection(Odometer odometer, EV3ColorSensor colorSensor) {
		this.odometer = odometer;
		this.colorSensor = colorSensor;
		colorSensor.setCurrentMode("Red");
	}

	// run method (required for Thread)
	public void run() {
		Sound.setVolume(Sound.VOL_MAX);
		colorSensor.setFloodlight(true);						// setting up the sensor
		SensorMode sensorMode = colorSensor.getRedMode();
		long correctionStart, correctionEnd;

		while (true) {
			//sensorMode.fetchSample(colorData, 0);
			correctionStart = System.currentTimeMillis();
			
			if (!frozen) {
				colorSensor.fetchSample(colorData, 0);
				boolean isLine = (colorData[0] < 0.4);
				if (isLine && !wasLine) {
					Sound.twoBeeps();
					savePosition();
					correctOdometer();
				}
				wasLine = isLine;
			}
			
			// this ensure the odometry correction occurs only once every period
			correctionEnd = System.currentTimeMillis();
			if (correctionEnd - correctionStart < CORRECTION_PERIOD) {
				try {
					Thread.sleep(CORRECTION_PERIOD
							- (correctionEnd - correctionStart));
				} catch (InterruptedException e) {
					// there is nothing to be done here because it is not
					// expected that the odometry correction will be
					// interrupted by another thread
				}
			}
		}
	}
	
	public void freeze() {
		frozen = true;
	}
	
	public void unfreeze() {
		previous = null;
		current = null;
		frozen = false;
	}
	
	private void savePosition() {
		if (current != null) previous = current.clone();
		current = odometer.getPosition();
	}
		
	private void correctOdometer() {
		double x, y, theta = current[2];

		if (approximately(0, theta)) {
			x = outboundLine() + SENSOR_DISTANCE;
			y = deviationDisplacement(1, 0.0);
		} else if (approximately(90, theta)) {
			x = deviationDisplacement(0, 90);
			y = - outboundLine() - SENSOR_DISTANCE;
		} else if (approximately(180, theta)) {
  			x = inboundLine() - SENSOR_DISTANCE;
  			y = deviationDisplacement(1, 180);
  		} else {
			x = deviationDisplacement(0, 270);
			y = - inboundLine() + SENSOR_DISTANCE;
  		}
		
		double dx = x - current[0];
		double dy = y - current[1];
		
		if (dx != 0) odometer.setX(odometer.getX() + dx);
		if (dy != 0) odometer.setY(odometer.getY() + dy);
	}
	
	private double deviationDisplacement(int xOrY, double expected) {
		if (isFirstLine() || !wasStraightPath()) {
			return current[xOrY];
		} else {
			return previous[xOrY] + deviationDistance(expected);
		}
	}
	
	private double deviationDistance(double expected) {
		return Math.tan(averageTheta() - expected) * LINE_SEPARATION;
	}
	
	private double averageTheta() {
		double angleSum = previous[2] + current[2];
		if (Math.abs(previous[2] - current[2]) > 180) {
			angleSum += 360;
		}
		return angleSum / 2.0;
	}
	
	private boolean wasStraightPath() {
		return Math.abs(previous[2] - current[2]) < (360) / 1500.0;
	}
			
	private boolean approximately(double target, double actual) {
		return Math.abs(target - actual) < 45;
	}
	
	private double outboundLine() {
		return isFirstLine() ? CLOSE_LINE : FAR_LINE;
	}
	
	private double inboundLine() {
		return isFirstLine() ? FAR_LINE : CLOSE_LINE;
	}
	
	private boolean isFirstLine() {
		return previous == null;
	}
}