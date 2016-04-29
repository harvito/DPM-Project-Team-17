package labPackage;
import lejos.hardware.Sound;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.SampleProvider;

/**Class for navigating to the appropriate starting zone.
 * defense vs offense signified via boolean value "offense"
 * used directly after initial localization
 * to go, use setupThreads(), runThreads() then killThreads() successively
 * 
 * @author Ivan
 *
 */
public class GoToStartingZone {
	Navigation nav;
	RegulatedMotor usMotor;
	int lane;
	Odometer odo;
	Thread thread1; //nav
	Thread thread2; //obstacle avoidance
	Thread thread3; //US sensor panning
	int corner; //starting corner
	private SampleProvider usSensor1;
	private float[] usData1;
	boolean noObstacle = true;
	boolean doneNav = true;
	boolean doneAvoiding = true;
	double x = 0;
	double y; //initialized in constructor.
	boolean offence;
	
	/**constructor. sets passed variables to same named local variables
	 * 
	 * @param nav navigation class, allows control of driving motors and methods
	 * @param odo odometer, allows access to position
	 * @param usMotor front-mounted. for ultrasonic sensor to scan back and forth
	 * @param lane int. starting lane, 1, 2, 3, or 4 corresponding to the 4 side line lanes of travel outsize play zone
	 * @param usSensor1 front and centre mounted on usMotor
	 * @param usData1 
	 * @param offence boolean. true if on offense
	 */
	public GoToStartingZone(Navigation nav,Odometer odo, RegulatedMotor usMotor, int lane, SampleProvider usSensor1, float[] usData1, boolean offence){
		this.nav = nav;
		this.lane = lane;
		this.odo = odo;
		this.usMotor = usMotor;
		this.usSensor1 = usSensor1;
		this.usData1 = usData1;
		this.offence = offence;
		if (offence)
			y = 15.84; //target entry tile
		else
			y = 294.48;
		setUpThreads();
	}
	
	/**runs each thread; begins navigation
	 * 
	 */
	public void runThreads(){
		thread1.start();
		thread2.start();
		thread3.start();
	}
	
	/**NOT IMMEDIATELY. waits for navigation to conclude, then allows thread completion.
	 * if robot position is at destination with tolerance, sets local variable doneNav to true.
	 * threads then skip while loops and finish execution
	 */
	public void killThreads(){
		boolean stop = false;
		while(!stop){
			double xValue = odo.getX();
			double yValue = odo.getY();
			if((xValue < x+7 && xValue > x-7)  && (yValue < y+7 && yValue > y-7)){
				stop = true;
				doneNav = true;
			}
			
			try { //checks twice per second
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**Necessary before running navigation method runThreads().
	 * initializes and defines run function for each thread.
	 * thread1 repeatedly updates travel destination
	 * thread2 handles obstacle avoidance
	 * thread3 repeatedly pans ultrasonic sensor back and forth
	 */
	public void setUpThreads(){
		thread1 = new Thread () {
			public void run () {
				while(!doneNav){
					while(noObstacle){
						if( lane == 1){ //rover made to travel along tile centers for simpler obstacle avoidance. while navigating to starting zone only 2 lanes per side are available.
							x = -15.84;
						}else if (lane == 2){
							x = 15.84;
						}else if (lane == 3){
							x = 294.49;
						}else{
							x = 324.6;
						}
						nav.travelTo(x,y, true);
					}
				}
			}
		};
		thread2 = new Thread () {
			public void run () {
				while(!doneNav){
					if(getFilteredData1() < 19 && nav.doneTurning){   //it sees something and rover is not turning is true
						int currentY = (int) odo.getYUnsynched();
						noObstacle = false;
						nav.setKeepTravelling(false);
						nav.setSpeeds(0, 0);
						
						if( lane == 1){
							nav.setKeepTravelling(true);
							nav.travelToXDirection(15.84, currentY, false); //travel to next lane over
							lane = 2;
						}else if (lane == 2){
							nav.setKeepTravelling(true);
							nav.travelToXDirection(-15.84, currentY, false);
							lane = 1;
						}else if (lane == 3){
							nav.setKeepTravelling(true);
							nav.travelToXDirection(324.6, currentY, false);
							lane = 4;
						}else{
							nav.setKeepTravelling(true);
							nav.travelToXDirection(293.6, currentY, false);
							lane = 3;
						}
					}
					noObstacle = true;
				}
			}
		};
		
		thread3  = new Thread(){
			public void run(){ //pan US sensor
				int counter = 1;
				usMotor.setAcceleration(80);
				usMotor.setSpeed(180);
				while(true){
					if (counter == 1){
						usMotor.rotate(33,false);
					}
					else if(counter%2 == 0){
						usMotor.rotate(-66,false);
					}
					else{
						usMotor.rotate(66,false);
					}
					
					counter++;
					
					
				}
			}
		};
	}
	
	/**Truncates value of US Sensor to be 60 cm or below
	 * 
	 * @return float. truncated value of US sensor, 60 cm or less
	 */
	private float getFilteredData1() {
		usSensor1.fetchSample(usData1, 0);
		float distance = usData1[0]*100;
		if (distance > 60)														
			distance = 60;																				
		return distance;
	}
}