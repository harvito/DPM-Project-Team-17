package labPackage;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.utility.Timer;
import lejos.utility.TimerListener;

public class LCDInfo implements TimerListener{
	public static final int LCD_REFRESH = 100;
	private Odometer odo;
	private USLocalizer USLoco;
	private Timer lcdTimer;
	private TextLCD LCD = LocalEV3.get().getTextLCD();;
	
	// arrays for displaying data
	private double [] pos;
	
	public LCDInfo(Odometer odo, USLocalizer USLoco) {
		this.odo = odo;
		this.USLoco = USLoco;
		this.lcdTimer = new Timer(LCD_REFRESH, this);
		
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
		LCD.drawString("US: ", 0, 3);
		LCD.drawString("A: ", 0, 4);
		LCD.drawString("B: ", 0, 5);
		LCD.drawString("Xo: ", 0, 6);
		LCD.drawString("Yo: ", 0, 7);
		LCD.drawInt((int)(pos[0] * 10), 3, 0);
		LCD.drawInt((int)(pos[1] * 10), 3, 1);
		LCD.drawInt((int)pos[2], 3, 2);
		LCD.drawInt((int)USLoco.getRawData(), 3, 3);
		LCD.drawInt((int)USLocalizer.publicAngleA, 3, 4);
		LCD.drawInt((int)USLocalizer.publicAngleB, 3, 5);
		LCD.drawInt((int)LightLocalizer.publicXOffset, 3, 6);
		LCD.drawInt((int)LightLocalizer.publicYOffset, 3, 7);
	}
}
