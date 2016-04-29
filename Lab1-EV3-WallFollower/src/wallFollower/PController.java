package wallFollower;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class PController implements UltrasonicController {
	
	private final int bandCenter, bandwidth;
	private final int motorStraight = 200, FILTER_OUT = 20;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	private int distance;
	private int filterControl;
	int deltaSpeedClose = 0;
	int deltaSpeedFar = 0;
	
	public PController(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor,
					   int bandCenter, int bandwidth) {
		//Default Constructor
		this.bandCenter = bandCenter;
		this.bandwidth = bandwidth;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		leftMotor.setSpeed(motorStraight);					// Initalize motor rolling forward
		rightMotor.setSpeed(motorStraight);
		leftMotor.forward();
		rightMotor.forward();
		filterControl = 0;
	}
	
	@Override
	public void processUSData(int distance) {
		
		// rudimentary filter - toss out invalid samples corresponding to null signal.
		// (n.b. this was not included in the Bang-bang controller, but easily could have).
		//
		if (distance >= 255 && filterControl < FILTER_OUT) { //EDITED BY M HARVEY
			// bad value, do not set the distance var, however do increment the filter value
			filterControl ++;
		} else if (distance >= 255){ //EDITED BY M HARVEY
			// true 255, therefore set distance to 255
			this.distance = distance;
		} else {
			// distance went below 255, therefore reset everything.
			filterControl = 0;
			this.distance = distance;
		}
		
		//-------------------------------------------------------------------FROM HERE, COPY
		
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
		
		//--------------------------------------------------------------------TO HERE, COPY
		
		
	}

	
	@Override
	public int readUSDistance() {
		return this.distance;
	}

}
