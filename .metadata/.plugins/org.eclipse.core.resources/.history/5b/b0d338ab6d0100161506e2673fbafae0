package labPackage;

import static org.junit.Assert.*;

import org.junit.Test;

public class NavigationTest {

	@Test
	public void testSetSpeedsFloatFloat() {
		fail("Not yet implemented");
	}

	@Test
	public void testTurnTo() {
		fail("Not yet implemented");
	}
	
	@Test //takes wheel radius, track and an angle of rotation and returns the angle each wheel should rotate (one fwd, one bckwd) to turn the rover angle degrees
	public void testconvertAngle(double radius, double width, double angle) {
		int actual = Navigation.convertAngle(radius, width, angle);
		double f = angle / 360.0; //fraction of circle through which to rotate rover
		double c = Math.PI * width; //circumference of circle of rotation
		double a = f * c; //length of arc one wheel should rotate through
		double wheelCircumference = 2.0 * Math.PI * radius;
		double nw = a / wheelCircumference; //number of wheel rotations necessary to move arclength
		int theta = (int) nw * 360; //number of degrees each wheel should rotate
		assertEquals(theta, actual);
	}
	
	@Test
	public void testconvertDistance(double radius, double distance) {
		int actual = Navigation.convertDistance(radius, distance);
		double wheelCircumference = 2.0 * Math.PI * radius;
		double n = distance / wheelCircumference; //number of wheel rotations
		int theta = 360 * (int) n; //number of degrees to rotate
		assertEquals(theta, actual);
	}

	@Test
	public void testTurnToNoError() {
		fail("Not yet implemented");
	}

	@Test
	public void testGoForward() {
		fail("Not yet implemented");
	}

	@Test
	public void testTurnLeft() {
		fail("Not yet implemented");
	}

	@Test
	public void testTurnRight() {
		fail("Not yet implemented");
	}

	@Test
	public void testTurnLeftFast() {
		fail("Not yet implemented");
	}

	@Test
	public void testTurnRightFast() {
		fail("Not yet implemented");
	}

	@Test
	public void testForward() {
		fail("Not yet implemented");
	}

}
