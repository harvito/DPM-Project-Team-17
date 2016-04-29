package navigation;
import labPackage.Navigation;
import labPackage.Odometer;
import lejos.hardware.Sound;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.SampleProvider;

public class Driver{
	
	Navigation nav;
	RegulatedMotor usMotor;
	private SampleProvider usSensor1;
	private float[] usData1;
	int xSquare;
	int ySquare;
	int llx;
	int lly;
	Odometer odo;
	private volatile boolean isRunning = true;
	private double xDiff;
	private double yDiff;
	private final double squareDist = 30.96;
	
	public Driver(Navigation nav, RegulatedMotor usMotor, int xSquare, int ySquare, Odometer odometer, int llx, int lly, SampleProvider usSensor1, float[] usData1){
		this.nav = nav;
		this.usMotor = usMotor;
		this.xSquare = xSquare;
		this.ySquare = ySquare;
		this.usSensor1 = usSensor1;
		this.usData1 = usData1;
		this.odo = odometer;
		this.llx = llx;
		this.lly = lly;
		usMotor.rotate(45, false);
	}

	public void doDriver(){
		int[] iniLoc = new int[] {1,1};
		int[] finLoc = new int[] {llx,lly};
		xDiff = finLoc[0] - iniLoc[0];
		yDiff = finLoc[1] - iniLoc[1];
		boolean Switch = true;
		
		while(!(xDiff == 0 && yDiff == 0)){
			xDiff = finLoc[0] - iniLoc[0];
			yDiff = finLoc[1] - iniLoc[1];
			
			if(yDiff != 0){
				nav.turnToNoError(90, true);
				
				if(getFilteredData() > squareDist){
					nav.goForward(squareDist);
					iniLoc[1]++;
					xDiff = finLoc[0] - iniLoc[0];
					yDiff = finLoc[1] - iniLoc[1];
					Sound.beep();
				}else{
					nav.turnToNoError(0, true);
					nav.goForward(squareDist);
					iniLoc[0]++;
					xDiff = finLoc[0] - iniLoc[0];
					yDiff = finLoc[1] - iniLoc[1];
					Sound.beep();
				}
			}else{
				nav.turnToNoError(0, true);
				
				if(getFilteredData() > squareDist){
					nav.goForward(squareDist);
					iniLoc[0]++;
					xDiff = finLoc[0] - iniLoc[0];
					yDiff = finLoc[1] - iniLoc[1];
					Sound.beep();
				}else{
					nav.turnToNoError(90, true);
					nav.goForward(squareDist);
					iniLoc[1]++;
					xDiff = finLoc[0] - iniLoc[0];
					yDiff = finLoc[1] - iniLoc[1];
					Sound.beep();
				}
				
			}
			
		}
		
		nav.turnToNoError(90, true);
		nav.goForward(50);
		nav.setSpeeds(60,60);
		
		
		
		
		
		
	}

	private float getFilteredData() {
		usSensor1.fetchSample(usData1, 0);
		float distance = usData1[0]*100;																		

		return distance;
	}
	
	
}
