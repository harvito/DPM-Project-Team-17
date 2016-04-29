package ev3Odometer;
import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class PController implements UltrasonicController {
	
	private final int bandCenter, bandwidth;
	private final int motorStraight = 200, FILTER_OUT = 20;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	private int distance;
	private int filterControl;
	int deltaSpeedClose = 0;
	int deltaSpeedFar = 0;
	private int motorLow;
	private int motorHigh;
	private static final int FORWARD_SPEED = 150;
	private static final int ROTATE_SPEED = 100;
	private Odometer odometer;
	private double leftRadius;
	private double rightRadius;
	private double width;
	private EV3LargeRegulatedMotor sensorMotor;
	private boolean yHeight;
	private double[] position = new double[3];
	
	public PController(Odometer odometer, EV3LargeRegulatedMotor leftMotor,EV3LargeRegulatedMotor rightMotor, EV3LargeRegulatedMotor sensorMotor,
					   int bandCenter, int bandwidth, int motorLow, int motorHigh, double leftRadius, double rightRadius, double width) {
		//Default Constructor
		this.odometer = odometer;
		this.bandCenter = bandCenter;
		this.bandwidth = bandwidth;
		this.leftMotor = rightMotor;
		this.rightMotor = leftMotor;
		this.motorLow = motorLow;
		this.motorHigh = motorHigh;
		this.leftRadius = leftRadius;
		this.rightRadius = rightRadius;
		this.sensorMotor = sensorMotor;
		this.width = width;
		leftMotor.setSpeed(motorStraight);					// Initalize motor rolling forward
		rightMotor.setSpeed(motorStraight);
		filterControl = 0;
	}
	
	@Override
	public void processUSData(int distance) {

		odometer.getPosition(position, new boolean[] { true, true, true });	
		
			
		
		// rudimentary filter - toss out invalid samples corresponding to null signal.
		// (n.b. this was not included in the Bang-bang controller, but easily could have).
		//
		if (distance >= 255 && filterControl < FILTER_OUT) { //EDITED BY M HARVEY
			// bad value, do not set the distance var, however do increment the filter value
			filterControl ++;
		} else if (distance >= 255){ 
			// true 255, therefore set distance to 255
			this.distance = distance;
		} else {
			// distance went below 255, therefore reset everything.
			filterControl = 0;
			this.distance = distance;
		}
		
		if(distance < (bandCenter/2) && Lab3.isNavigating){ //first encounter with wall
			leftMotor.stop(); //stop both motors
			rightMotor.stop();
			Lab3.isNavigating = false; //stop navigating and begin evasive maneuver
			sensorMotor.setSpeed(ROTATE_SPEED);
			sensorMotor.rotate(-90, false);
		}
		
		if(Lab3.isNavigating == false){
			odometer.getPosition(position, new boolean[] { true, true, true });
			
			if(distance < bandCenter){ //too close
				leftMotor.setSpeed((int)((150*distance/bandCenter))); // decrease outside wheel speed as bot approaches wall
				rightMotor.setSpeed(150); //keep inside wheel speed constant
				leftMotor.forward(); 
				rightMotor.forward();
			} else if (distance < 2 * bandCenter){ //between as far and twice as far as desired
				leftMotor.setSpeed((int)((150*(distance - bandCenter)/bandCenter) + 150)); // increase outside wheel speed an amount proportional to error
				rightMotor.setSpeed(150); //keep inside wheel constant
				leftMotor.forward(); 
				rightMotor.forward();
			}else{ //at least twice as far from wall as desired
				leftMotor.setSpeed(300); //set outside wheel to twice as fast as inside
				rightMotor.setSpeed(150);
				leftMotor.forward(); 
				rightMotor.forward();
			}
				
				//if((position[2] > 40) && (position[1] > (57-position[0])) && (position[1] < (63-position[0]))){
				//	Lab3.MODE=3;
				//}
				}
			}
	
	@Override
	public int readUSDistance() {
		return this.distance;
	}
}

	
	
	