package org.usfirst.frc.team5112.robot;

import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import com.kylecorry.frc.vision.CameraSource;
import com.kylecorry.frc.vision.TargetGroup;
import com.kylecorry.frc.vision.TargetGroupDetector;
import com.kylecorry.frc.vision.TargetSpecs;
import com.thegongoliers.input.EnhancedXboxController;

import edu.wpi.cscore.CvSource;
import edu.wpi.cscore.UsbCamera;
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class Robot extends IterativeRobot {
	final String defaultAuto = "Default";
	final String customAuto = "My Auto";
	String autoSelected;
	SendableChooser<String> chooser = new SendableChooser<>();
	SendableChooser<Integer> chooser2 = new SendableChooser<>();
	TargetGroupDetector pegDetect;
	CameraSource cam;
	EnhancedXboxController xbox;

	/**
	 * This function is run when the robot is first started up and should be
	 * used for any initialization code.
	 */
	@Override
	public void robotInit() {
		chooser.addDefault("Show normal image", "normal");
		chooser.addObject("Show HSV threshold", "threshold");
		chooser.addObject("Outline target", "target");
		SmartDashboard.putData("Camera Chooser", chooser);

		chooser2.addDefault("Hue", 0);
		chooser2.addObject("Saturation", 1);
		chooser2.addObject("Value", 2);
		SmartDashboard.putData("HSV Chooser", chooser2);

		xbox = new EnhancedXboxController(0);

		new Thread(() -> {

			TargetSpecs specs = new PegRetroreflective();

			double[][] hsv = { { specs.getHue().start, specs.getHue().end },
					{ specs.getSaturation().start, specs.getSaturation().end },
					{ specs.getValue().start, specs.getValue().end } };

			UsbCamera camera = CameraServer.getInstance().startAutomaticCapture();
			camera.setResolution(160, 120);
			camera.setBrightness(0);
			camera.setExposureManual(0);
			camera.setWhiteBalanceManual(10000);

			cam = new CameraSource(camera);
			pegDetect = new TargetGroupDetector(new PegRetroreflective(), new Peg());

			Mat output = new Mat();
			Mat source = new Mat();
			CvSource outputStream = CameraServer.getInstance().putVideo("Camera", 160, 120);

			while (!Thread.interrupted()) {

				if (xbox.getPOV() != -1) {
					int selected = chooser2.getSelected();
					switch (xbox.getPOVDirection()) {
					case NORTH:
						hsv[selected][0] += 5;
						System.out.println(hsv[selected][0]);
						break;
					case SOUTH:
						hsv[selected][0] -= 5;
						System.out.println(hsv[selected][0]);
						break;
					case EAST:
						hsv[selected][1] += 5;
						System.out.println(hsv[selected][1]);
						break;
					case WEST:
						hsv[selected][1] -= 5;
						System.out.println(hsv[selected][1]);
						break;
					default:
						System.out.println("North and south control start; east and west control end");
					}
				}

				source = cam.getPicture();
				String choice = chooser.getSelected();
				if (choice.equals("threshold")) {
					Imgproc.cvtColor(source, output, Imgproc.COLOR_BGR2HSV);
					Core.inRange(output, new Scalar(hsv[0][0], hsv[1][0], hsv[2][0]),
							new Scalar(hsv[0][1], hsv[1][1], hsv[2][1]), output);
				} else if (choice.equals("target")) {
					List<TargetGroup> targets = pegDetect.detect(cam.getPicture());
					if (!targets.isEmpty()) {
						output = source;
						Rect boundary = new Rect((int) Math.round(targets.get(0).getPosition().x),
								(int) Math.round(targets.get(0).getPosition().y),
								(int) Math.round(targets.get(0).getWidth()),
								(int) Math.round(targets.get(0).getHeight()));
						Imgproc.rectangle(output, boundary.tl(), boundary.br(), new Scalar(0, 255, 0));
					}
				} else {
					output = source;
				}
				outputStream.putFrame(output);
			}
		}).start();

	}

	/**
	 * This autonomous (along with the chooser code above) shows how to select
	 * between different autonomous modes using the dashboard. The sendable
	 * chooser code works with the Java SmartDashboard. If you prefer the
	 * LabVIEW Dashboard, remove all of the chooser code and uncomment the
	 * getString line to get the auto name from the text box below the Gyro
	 *
	 * You can add additional auto modes by adding additional comparisons to the
	 * switch structure below with additional strings. If using the
	 * SendableChooser make sure to add them to the chooser code above as well.
	 */
	@Override
	public void autonomousInit() {

	}

	/**
	 * This function is called periodically during autonomous
	 */
	@Override
	public void autonomousPeriodic() {

	}

	/**
	 * This function is called periodically during operator control
	 */
	@Override
	public void teleopPeriodic() {
	}

	/**
	 * This function is called periodically during test mode
	 */
	@Override
	public void testPeriodic() {
	}
}
