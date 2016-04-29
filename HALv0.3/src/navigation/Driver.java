package navigation;
import labPackage.Navigation;
import labPackage.Odometer;
import lejos.hardware.Sound;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.SampleProvider;

/**specific instance of navigation. drives orthogonally and attempts to avoid all obstacles.
 * 
 * @author Nathan
 *
 */
public class Driver extends Thread{

	Navigation nav;
	RegulatedMotor usMotor;
	private SampleProvider usSensor1;
	private float[] usData1 = new float[1];
	int xSquare;
	int ySquare;
	int llx;
	int lly;
	Odometer odo;
	private volatile boolean isRunning = true;
	private double xDiff;
	private double yDiff;
	private final double squareDist = 30.96;

	public Driver(Navigation nav, RegulatedMotor usMotor, int xSquare, int ySquare, Odometer odometer, int llx, int lly, SampleProvider usSensor1){
		this.nav = nav;
		this.usMotor = usMotor;
		this.usSensor1 = usSensor1;
		this.odo = odometer;
		this.llx = llx;
		this.lly = lly;
		usMotor.rotate(45, false);
	}

	public void run(){
		int[] iniLoc = new int[] {1,1};
		int[] finLoc = new int[] {llx,lly};
		xDiff = finLoc[0] - iniLoc[0];
		yDiff = finLoc[1] - iniLoc[1];
		boolean Switch = true;

		while(!(xDiff > 0 && yDiff > 0)){
			xDiff = finLoc[0] - iniLoc[0];
			yDiff = finLoc[1] - iniLoc[1];

			if(yDiff > 0){
				nav.turnToNoError(90, true);

				if(getFilteredData() > squareDist){  // if it doesn't see a wall
					nav.goForward(squareDist);      // go forward one block (30 cm)
					++iniLoc[1];					//increase the y location 
					xDiff = finLoc[0] - iniLoc[0];  //set x postition again
					yDiff = finLoc[1] - iniLoc[1];  //set the y position again
					Sound.beep();
				}else{
					nav.turnToNoError(0, true);
					nav.goForward(squareDist);
					++iniLoc[0];
					xDiff = finLoc[0] - iniLoc[0];
					yDiff = finLoc[1] - iniLoc[1];
					Sound.beep();
				}
			}else{
				nav.turnToNoError(0, true);

				if(getFilteredData() > squareDist){
					nav.goForward(squareDist);
					++iniLoc[0];
					xDiff = finLoc[0] - iniLoc[0];
					yDiff = finLoc[1] - iniLoc[1];
					Sound.beep();
				}else{
					nav.turnToNoError(90, true);
					nav.goForward(squareDist);
					++iniLoc[1];
					xDiff = finLoc[0] - iniLoc[0];
					yDiff = finLoc[1] - iniLoc[1];
					Sound.beep();
				}

			}
			
			try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

		}

		nav.turnToNoError(90, true);
		//nav.goForward(50);
		//nav.setSpeeds(60,60);

	}

	private float getFilteredData() {
		usSensor1.fetchSample(usData1, 0);
		float distance = usData1[0]*100;

		if (distance > 60){													
			distance = 60;
		}

		return distance;
	}


}
