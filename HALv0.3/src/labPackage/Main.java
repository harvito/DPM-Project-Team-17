package labPackage;

import java.io.IOException;  
import java.rmi.NotBoundException;
import java.util.HashMap;

import wifi.*;
import navigation.Driver;
import navigation.OdometryCorrection;
import navigation.OdometryCorrection_Nathan;
import lejos.hardware.*;
import lejos.hardware.ev3.EV3;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.*;
import lejos.remote.ev3.RemoteEV3;
import lejos.remote.ev3.RemoteRequestEV3;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.SampleProvider;

public class Main {

	// Static Resources:
	// Left motor connected to output A
	// Right motor connected to output D
	// Ultrasonic sensor port connected to input S1
	// Color sensor port connected to input S2



	public static void main(String[] args)  {

		//ImportedData
		int urx = 6;
		int ury = 6;
		double angle = 0;
		
		//incoming WiFi transmission info
		final String SERVER_IP = "192.168.10.200"; //"localhost";
		final int TEAM_NUMBER = 17;
		HashMap<String,Integer> t = new HashMap<String,Integer>();
		

		Brick bricks1 = null;
		RemoteRequestEV3 bricks2 = null;

		try {
			bricks1 = new RemoteEV3(BrickFinder.find("EV1")[0].getIPAddress());
			bricks2 = new RemoteRequestEV3(BrickFinder.find("EV3")[0].getIPAddress());


			//RegulatedMotor rightMotor = new EV3LargeRegulatedMotor(BrickFinder.getLocal().getPort("D"));
			//usPort1 = bricks1.getPort("S1");		
			//usPort2 = bricks1.getPort("S2");	

		} catch (IOException | NotBoundException e) {
			System.out.println("An error occured while assigning the bricks to the motors.");
			e.printStackTrace();
		}

		//bricks1.getAudio().playTone(Sound.BEEP, 100);

		EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(BrickFinder.getLocal().getPort("A"));
		EV3LargeRegulatedMotor lowerArmMotor = new EV3LargeRegulatedMotor(BrickFinder.getLocal().getPort("B")); //arm motor
		EV3LargeRegulatedMotor higherArmMotor = new EV3LargeRegulatedMotor(BrickFinder.getLocal().getPort("C")); //claw motor
		EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(BrickFinder.getLocal().getPort("D"));
		RegulatedMotor usMotor = bricks2.createRegulatedMotor("C", 'M');
		RegulatedMotor shooter = bricks2.createRegulatedMotor("A", 'L');
		RegulatedMotor loaderMotor = bricks2.createRegulatedMotor("B", 'M');
		Sound.beep();

		//Setup ultrasonic sensor

		Port uvPort1 = BrickFinder.getLocal().getPort("S1");
		Port uvPort2 = BrickFinder.getLocal().getPort("S2");
		Port colorPort1 =  BrickFinder.getLocal().getPort("S3");
		Port ballScannerColorSensorPort = BrickFinder.getLocal().getPort("S4");

		SensorModes usSensor1 = new EV3UltrasonicSensor(uvPort1);
		SensorModes usSensor2 = new EV3UltrasonicSensor(uvPort2);

		SampleProvider sampleProvider1 = usSensor1.getMode("Distance"); 
		SampleProvider sampleProvider2 = usSensor2.getMode("Distance"); 

		float[] usData1 = new float[sampleProvider1.sampleSize()];				// colorData is the buffer in which data are returned
		float[] usData2 = new float[sampleProvider2.sampleSize()];				// colorData is the buffer in which data are returned

		EV3ColorSensor colorSensor1 = new EV3ColorSensor(colorPort1);
		EV3ColorSensor scannerCS = new EV3ColorSensor(ballScannerColorSensorPort);

		final SampleProvider colorValue1 = colorSensor1.getMode("RGB");		
		final float[] colorData1 = new float[colorValue1.sampleSize()];	

		// setup the odometer and display
		Odometer odo = new Odometer(leftMotor, rightMotor, 30, true);
		LCDInfo lcd = new LCDInfo(odo, usSensor1, usData1, usSensor2, usData2);
		Navigation nav = new Navigation(odo, leftMotor, rightMotor);
		
		WifiConnection conn = null;
		try {
			conn = new WifiConnection(SERVER_IP, TEAM_NUMBER); //
		} catch (IOException e) {
			System.out.print("WiFi Failed");
		}
		
		if (conn != null){
			t = conn.StartData;
			if (t == null) {
				System.out.print("Failed to read transmission");
			} else {
			}
		} else {
			System.out.print("Connection failed");
			System.exit(0);
		}
		
		// setup localization
		USLocalizer usl = new USLocalizer( odo, sampleProvider1, usData1,sampleProvider2, usData2, USLocalizer.LocalizationType.FALLING_EDGE, startCorner(t), leftMotor, rightMotor);
		LightLocalizer lsl = new LightLocalizer(leftMotor, rightMotor, nav, odo, colorValue1, colorData1, colorSensor1);

		usl.doLocalization();
		
		//calculate target coordinates for light localization
		double[] lightLocalTarget = null;
		switch (startCorner(t)) {
		case 1:
			lightLocalTarget = new double[] {0.0, 0.0};
			break;
		case 2:
			lightLocalTarget = new double[] {309.6, 0.0};
			break;
		case 3:
			lightLocalTarget = new double[] {309.6, 309.6};
			break;
		case 4:
			lightLocalTarget = new double[] {0.0, 309.6};
			break;
		}
		nav.forward(); //necessary for light local
		lsl.doLocalization(lightLocalTarget[0], lightLocalTarget[1]);
		
		
		//position robot for next step. roughly in the centre of 1 tile diagonally inwards from sc
		switch (startCorner(t)) {
		case 1:
			nav.turnTo(45, true);
			break;
		case 2:
			nav.turnTo(135, true);
			break;
		case 3:
			nav.turnTo(225, true);
			break;
		case 4:
			nav.turnTo(315, true);
			break;
		}
		nav.goForward(21.6);
		

		GoToStartingZone goToAttackZone = new GoToStartingZone(nav, odo, usMotor, 3, usSensor2, usData2, true);
		goToAttackZone.setUpThreads();
		goToAttackZone.runThreads();
		
		//travel to correct zone
		if (t.get("OTN") == 17) { //offensive team number. our team number.
			switch (t.get("OSC")) {
			case 1: //close to origin
				nav.turnTo(0, true);
				nav.goForward(61.92); //two tiles
				break;
			case 2:
				nav.turnTo(180, true);
				nav.goForward(61.92);
				break;
			case 3:
				nav.turnTo(270, true);
				GoToStartingZone goToAttackZone3 = new GoToStartingZone(nav, odo, usMotor, 2, usSensor2, usData2, true);
				goToAttackZone3.setUpThreads();
				goToAttackZone3.runThreads();
				goToAttackZone3.killThreads();
				break;
			case 4:
				nav.turnTo(270, true);
				GoToStartingZone goToAttackZone4 = new GoToStartingZone(nav, odo, usMotor, 3, usSensor2, usData2, true);
				goToAttackZone4.setUpThreads();
				goToAttackZone4.runThreads();
				goToAttackZone4.killThreads();
				break;
			}
		}
		else { //travel to defense zone
			switch (t.get("DSC")) {
			case 1:
				nav.turnTo(90, true);
				GoToStartingZone goToDefenseZone1 = new GoToStartingZone(nav, odo, usMotor, 2, usSensor2, usData2, true);
				goToDefenseZone1.setUpThreads();
				goToDefenseZone1.runThreads();
				goToDefenseZone1.killThreads();
				break;
			case 2:
				GoToStartingZone goToDefenseZone2 = new GoToStartingZone(nav, odo, usMotor, 3, usSensor2, usData2, true);
				goToDefenseZone2.setUpThreads();
				goToDefenseZone2.runThreads();
				goToDefenseZone2.killThreads();
				break;
			case 3: //hard coding to defensive position
				nav.turnTo(180, true);
				nav.goForward(61.92);
				nav.turnTo(210, true);
				nav.goForward(61.92);
				nav.turnTo(180, true);
				break;
			case 4:
				nav.turnTo(0, true);
				nav.goForward(61.92);
				nav.turnTo(330, true);
				nav.goForward(61.92);
				nav.turnTo(0, true);
				break;
			}
		}
		
		//offense and defense-specific actions
		if (t.get("OTN") == 17) { //offense
			ShooterMilestone sho = new ShooterMilestone(lowerArmMotor, higherArmMotor, shooter, loaderMotor, nav, odo, colorValue1, colorData1, scannerCS, angle);
			GoCloseToBalls goCloseToBalls = new GoCloseToBalls(nav, odo, t.get("ll-x"), t.get("ll-y"), sampleProvider2, usData2);
			
			goCloseToBalls.runThreads();
			nav.travelToXDirection(t.get("ll-x") - 25.0, odo.getY(), true);
			nav.travelToBallsY(odo.getX(), t.get("ll-y"));
			nav.turnTo(90, true);
			sho.loadBalls(t.get("BC")); //0 = red balls, 1 = blue balls, 2 = any balls
			GoCloseToBalls toShootLoc = new GoCloseToBalls(nav, odo, 6, t.get("d2"), sampleProvider2, usData2);
			toShootLoc.runThreads();
			nav.turnTo(85, true); //aim (for middle of net)
			sho.shootAll();
					
		}
		else { //on defense
			while (true) { //go back and forth forever
				nav.goForward(92.88);
				nav.goForward(-92.88);
			}
		}
		
		//wait to for button press to exit
		while (Button.waitForAnyPress() != Button.ID_ESCAPE);
		System.exit(0);

	}
	
	/**
	 * Returns the robot's starting corner, as logged in the passed hashmap.
	 * checks if offensive team number is the same as our team number. if so, returns OSC, offensive start corner.
	 * if not, returns DSC, defensive starting corner
	 *
	 * @param  t  	the hashmap containing match parameters, received from WiFi connection
	 * @return int	the integer corresponding to the robot's starting corner
	 */
	public static int startCorner(HashMap<String, Integer> t) {
		boolean isOffense = (t.get("OTN") == 17);
		if (isOffense)
			return t.get("OSC");
		else
			return t.get("DSC");
	}
}
