package navigation;
import labPackage.Navigation;
import labPackage.Odometer;
import lejos.hardware.Sound;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.SensorMode;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.SampleProvider;

public class odometryCorrection extends Thread{
	
	Navigation nav;
	RegulatedMotor usMotor;
	int xSquare;
	int ySquare;
	Odometer odo;
	private SampleProvider colorValue;
	private EV3ColorSensor colorSensor;
	private float[] colorData;
	private volatile boolean isRunning = true;
	
	public odometryCorrection(Navigation nav, RegulatedMotor usMotor, int xSquare, int ySquare, Odometer odometer, SampleProvider colorValue, float[] colorData, EV3ColorSensor colorSensor){
		this.nav = nav;
		this.usMotor = usMotor;
		this.xSquare = xSquare;
		this.ySquare = ySquare;
		this.odo = odometer;
		this.colorValue = colorValue;
		this.colorSensor = colorSensor; 
		this.colorData = colorData;
		usMotor.rotate(45, false);
	}

	public void run(){
		
		colorSensor.setFloodlight(true);						// setting up the sensor
		SensorMode sensorMode = colorSensor.getRedMode();
		double angle = 0.0;
		double x = 0.0;
		double y = 0.0;
		
		while(true){
			sensorMode.fetchSample(colorData, 0);
			angle = odo.getAng();
			if(colorData[0] * 10 < 4){
				
				//If we update the y:
				if((angle > 45 && angle < 135) || (angle > 225 && angle < 315)){
				
					for(int i = 0 ; i < 7; i++){
						if(x > (-15 + 30 * i) && x < (15 + 30*i)){
							odo.setPosition(new double[] {0,30*(i),0}, new boolean[] {false,true,false});
							Sound.beep();
						}
					}
					
				//If we update the X:
				}else{
					for(int i = 0 ; i < 7; i++){
						if(x > (-15 + 30 * i) && x < (15 + 30*i)){
							odo.setPosition(new double[] {30*(i),0,0}, new boolean[] {true,false,false});
							Sound.beep();
						}
					}
				}
			}
		}
		
	}
	
}
