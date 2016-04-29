package wallFollower;
import lejos.hardware.motor.*;

public class BangBangController implements UltrasonicController{
	private final int bandCenter, bandwidth;
	private final int motorLow, motorHigh;
	private int distance;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	
	public BangBangController(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor,
							  int bandCenter, int bandwidth, int motorLow, int motorHigh) {
		//Default Constructor
		this.bandCenter = bandCenter;
		this.bandwidth = bandwidth;
		this.motorLow = motorLow;
		this.motorHigh = motorHigh;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		leftMotor.setSpeed(motorHigh);				// Start robot moving forward
		rightMotor.setSpeed(motorHigh);
		leftMotor.forward();
		rightMotor.forward();
	}
	
	@Override
	public void processUSData(int distance) {
		this.distance = distance;
		
		if ( distance < bandCenter - bandwidth ){ //too close
			leftMotor.setSpeed((int) (motorLow / 1.5)); //outside wheel speed very slow to avoid approaching beyond a critical angle
			rightMotor.setSpeed(motorHigh); //inside wheel faster than outside; bot turns away from wall
			leftMotor.forward(); 
			rightMotor.forward();
		} else if ( distance > bandCenter + bandwidth ){ //too far
			leftMotor.setSpeed(motorHigh);
			rightMotor.setSpeed(motorLow); //outside wheel faster than outside; bot turn towards wall
			leftMotor.forward(); 
			rightMotor.forward();
		} else { //wall distance within tolerance
			leftMotor.setSpeed((motorHigh + motorLow) / 2); //an average speed
			rightMotor.setSpeed((motorHigh + motorLow) / 2); //both wheels same speed; bot goes straight
			leftMotor.forward(); 
			rightMotor.forward();
		}
	}

	@Override
	public int readUSDistance() {
		return this.distance;
	}
}
