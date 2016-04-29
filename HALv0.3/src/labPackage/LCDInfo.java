package labPackage;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.robotics.SampleProvider;
import lejos.utility.Timer;
import lejos.utility.TimerListener;

/**update LCD screen to display odometer's x, y and theta. also shows the ultrasonic sensors' readings.
 * uses simple get methods to read values and simple draw method to update screen output
 * 
 */
public class LCDInfo implements TimerListener{
	public static final int LCD_REFRESH = 100; //refresh period
	private Odometer odo;
	private Timer lcdTimer;
	private TextLCD LCD = LocalEV3.get().getTextLCD();;
	private SampleProvider usSensor1;
	private float[] usData1;
	private SampleProvider usSensor2;
	private float[] usData2;
	
	// arrays for displaying data
	private double [] pos;
	
	 /**Constructor. initializes most variables to their passed values
	  * 
	  * @param odo odometer, so values may be read
	  * @param usSensor1, to read values
	  * @param usData1, to read values
	  * @param usSensor2, to read values
	  * @param usData2, to read values
	  */
	public LCDInfo(Odometer odo,  SampleProvider usSensor1, float[] usData1,  SampleProvider usSensor2, float[] usData2) {
		this.odo = odo;
		this.lcdTimer = new Timer(LCD_REFRESH, this);
		this.usSensor1 = usSensor1;
		this.usData1 = usData1;
		this.usSensor2 = usSensor2;
		this.usData2 = usData2;
		
		
		// initialize the arrays for displaying odometer data
		pos = new double [3];
		
		// start the timer
		lcdTimer.start();
	}
	
	//draw output data
	public void timedOut() { 
		odo.getPosition(pos);
		LCD.clear();
		LCD.drawString("X: ", 0, 0);
		LCD.drawString("Y: ", 0, 1);
		LCD.drawString("H: ", 0, 2);
		LCD.drawString("US1: ", 0, 3);
		LCD.drawString("US2: ", 0, 4);
		LCD.drawString("AL: ", 0, 5);
		LCD.drawString("AR: ", 0, 6);
		LCD.drawInt((int)(pos[0] ), 8, 0);
		LCD.drawInt((int)(pos[1]), 8, 1);
		LCD.drawInt((int)pos[2], 8, 2);
		LCD.drawInt((int) getUSData1(), 8, 3);
		LCD.drawInt((int) getUSData2(), 8, 4);
		LCD.drawInt((int) getUSData1(), 8, 3);
		LCD.drawInt((int) getUSData2(), 8, 4);
	}
	
	/**Unfiltered data for sensor 1. front-mounted scanning.
	 * 
	 * @return float. sensor reading
	 */
	private float getUSData1() {
		float distance;
		
		usSensor1.fetchSample(usData1, 0);
		distance = usData1[0]*100;																			
				
		return distance;
	}
	
	/**Unfiltered data for sensor 2. right diagonal facing. fixed.
	 * 
	 * @return float. sensor reading
	 */
	private float getUSData2() {
		float distance;
		
		usSensor2.fetchSample(usData2, 0);
		distance = usData2[0]*100;																			
				
		return distance;
	}
}
