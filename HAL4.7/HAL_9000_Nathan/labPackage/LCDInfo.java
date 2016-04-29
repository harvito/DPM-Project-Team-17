package labPackage;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.robotics.SampleProvider;
import lejos.utility.Timer;
import lejos.utility.TimerListener;

public class LCDInfo implements TimerListener{
	public static final int LCD_REFRESH = 100;
	private Odometer odo;
	private Timer lcdTimer;
	private TextLCD LCD = LocalEV3.get().getTextLCD();;
	private SampleProvider usSensor1;
	private float[] usData1;
	private SampleProvider usSensor2;
	private float[] usData2;
	
	// arrays for displaying data
	private double [] pos;
	
	public LCDInfo(Odometer odo,  SampleProvider usSensor1, float[] usData1,  SampleProvider usSensor2, float[] usData2) {
		this.odo = odo;
		this.lcdTimer = new Timer(LCD_REFRESH, this);
		this.usSensor1 = usSensor1;
		this.usData1 = usData1;
		this.usSensor2 = usSensor2;
		this.usData2 = usData2;
		
		
		// initialise the arrays for displaying data
		pos = new double [3];
		
		// start the timer
		lcdTimer.start();
	}
	
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
		LCD.drawInt((int) getFilteredData1(), 8, 3);
		LCD.drawInt((int) getFilteredData2(), 8, 4);
		LCD.drawInt((int) getFilteredData1(), 8, 3);
		LCD.drawInt((int) getFilteredData2(), 8, 4);
	}
	
	private float getFilteredData1() {
		float distance;
		
		usSensor1.fetchSample(usData1, 0);
		distance = usData1[0]*100;																			
				
		return distance;
	}
	
	private float getFilteredData2() {
		float distance;
		
		usSensor2.fetchSample(usData2, 0);
		distance = usData2[0]*100;																			
				
		return distance;
	}
}
