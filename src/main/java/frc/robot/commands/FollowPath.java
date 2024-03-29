/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot.commands;

import java.io.File;

import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.robot.Robot;
import frc.robot.RobotMap;

import java.io.IOException;

import edu.wpi.first.wpilibj.*;

import frc.lib.pathfollowing.Trajectory;

public class FollowPath extends Command {

  private final String pathName;
  private final boolean reverse;
  private final boolean mirror;
  private Trajectory rightTrajectory;
  private Trajectory leftTrajectory;
<<<<<<< HEAD
  private double initialHeading;
=======
  private final double initialHeading;
>>>>>>> a4683c83daa2eda56a4a18c8324d709ecb9aabd2
  private double pathStartHeading;

  private double kV = 1 / RobotMap.MAX_VELOCITY; // Velocity
  private double kA = 0; //.035 // Acceleration
  private double kH = 0; //-.009; // Heading
  private double kP = 0; //.12; // Proportional
  private double kI = 0; // Integral
  private double kD = 0; // Derivative


  // Variables used in execute, declared here to avoid GC
  private double errorL;
  private double errorR;
  private double totalErrorL;
  private double totalErrorR ;
  private double lastErrorL;
  private double lastErrorR;
  private double errorH;


  private double startTime;

  private Trajectory.PathPoint nextLeftValues;
  private Trajectory.PathPoint nextRightValues;
  
  public FollowPath(String pathName) {
    this(pathName, new char[0]);
  }

  /**
   * 
   * @param pathName Name of the path in filesystem
   * @param args Character array of arguments: 
   * - 'R' or 'r' for reversed
   * - 'M' or 'm' for mirrored
   */
  public FollowPath(String pathName, char[] args) {
    requires(Robot.drivetrain);
    this.pathName = pathName;
    System.out.println(args.toString());
    System.out.println(reverse = new String(args).contains("r") || new String(args).contains("R"));
    mirror = new String(args).contains("m") || new String(args).contains("M");
    initialHeading = boundTo180(Robot.gyro.getYaw());
    startTime = Timer.getFPGATimestamp();
  }

  /**
   * 
   * @param pathName Name of the path in filesystem
   * @param args String of arguments: 
   * - 'R' or 'r' for reversed
   * - 'M' or 'm' for mirrored
   */
  public FollowPath(String pathName, String args) {
    this(pathName, args.toCharArray());
  }

  /**
   * Tries to read the path specified in the constructor. Sets both paths to null if file is not found
   */
  private void readTrajectory() {
    try {
<<<<<<< HEAD
      // System.out.println(Filesystem.getDeployDirectory().toString());
      File leftFile = new File("/home/lvuser/deploy/paths/" + pathName + "_left.csv");
      File rightFile = new File("/home/lvuser/deploy/paths/" + pathName + "_right.csv");
=======
      File leftFile = new File(Filesystem.getDeployDirectory() + "/paths/" + pathName + "_left.csv");
      File rightFile = new File(Filesystem.getDeployDirectory() + "/paths/" + pathName + "_right.csv");
>>>>>>> a4683c83daa2eda56a4a18c8324d709ecb9aabd2
      leftTrajectory = (mirror^reverse) ? new Trajectory(rightFile) : new Trajectory(leftFile);
      rightTrajectory = (mirror^reverse) ? new Trajectory(leftFile) : new Trajectory(rightFile);
      pathStartHeading = leftTrajectory.getStartHeading();
    } catch (IOException exc) {
      exc.printStackTrace();
      leftTrajectory = null;
      rightTrajectory = null;
    } finally { //in conclusion jayden bad
      // if (leftTrajectory != null) { System.out.println("TRAJECTORY EXISTS "); }
    }
  }

  private void shuffleboardSetup() {
    Robot.prefs = Preferences.getInstance();
    
    if (!Preferences.getInstance().containsKey("kV")) { Preferences.getInstance().putDouble("kV", kV); }
    if (!Preferences.getInstance().containsKey("kH")) { Preferences.getInstance().putDouble("kH", kH); }
    if (!Preferences.getInstance().containsKey("kA")) { Preferences.getInstance().putDouble("kA", kA); }
    if (!Preferences.getInstance().containsKey("kP")) { Preferences.getInstance().putDouble("kP", kP); }
    if (!Preferences.getInstance().containsKey("kI")) { Preferences.getInstance().putDouble("kI", kI); }
    if (!Preferences.getInstance().containsKey("kD")) { Preferences.getInstance().putDouble("kD", kD); }

    kV = Preferences.getInstance().getDouble("kV", kV);
    kH = Preferences.getInstance().getDouble("kH", kH);
    kA = Preferences.getInstance().getDouble("kA", kA);
    kP = Preferences.getInstance().getDouble("kP", kP);
    kI = Preferences.getInstance().getDouble("kI", kI);
    kD = Preferences.getInstance().getDouble("kD", kD);
    
  }

  private double boundTo180(double degrees) {
    while (degrees > 180 || degrees < -180) {
      if (degrees > 180) { degrees -= 360; }
      if (degrees < -180) { degrees += 360; }
    }
    return degrees;
  }

  // Called just before this Command runs the first time
  @Override
  protected void initialize() {
<<<<<<< HEAD
    Robot.drivetrain.setEncoders(0);
=======
    Robot.drivetrain.setEncoders();
>>>>>>> a4683c83daa2eda56a4a18c8324d709ecb9aabd2
    readTrajectory();
    shuffleboardSetup();

    totalErrorL = 0d;
    totalErrorR = 0d;

    if (reverse) {
      System.out.printf("%s is reverse", pathName);
    }
    pathStartHeading = leftTrajectory.getStartHeading();
    
  } 

  // Called repeatedly when this Command is scheduled to run
  @Override
  protected void execute() {
    nextLeftValues = leftTrajectory.next();
    nextRightValues = rightTrajectory.next();
    if (reverse) {
      nextLeftValues.position *= -1; nextRightValues.position *= -1; 
      nextLeftValues.acceleration *= -1; nextRightValues.acceleration *= -1;
      nextLeftValues.velocity *= -1; nextLeftValues.velocity *= -1;

    }

    errorL =/*(!reverse ? */nextLeftValues.position/* : -nextLeftValues.position)*/ - Robot.drivetrain.getLeftDistance();
    errorR =/*(!reverse ? */nextRightValues.position/* : -nextLeftValues.position)*/ - Robot.drivetrain.getRightDistance();
    totalErrorL += errorL;
    totalErrorR += errorR;
    errorH = (nextLeftValues.heading - pathStartHeading) - (Math.abs(boundTo180(Robot.gyro.getYaw())) - Math.abs(initialHeading));

    /*double leftOutput = !reverse ?
                        kV * nextLeftValues.velocity +
                        kA * nextLeftValues.acceleration +
                        kP * errorL +
                        kI * totalErrorL +
                        kD * (errorL - lastErrorL) + 
                        kH * errorH
                        :
                        -kV * nextLeftValues.velocity +
                        -kA * nextLeftValues.acceleration +
                        kP * errorL +
                        kI * totalErrorL +
                        kD * (errorL - lastErrorL) +
                        kH * errorH;
                        
    double rightOutput = !reverse ?
                         kV * nextRightValues.velocity +
                         kA * nextRightValues.acceleration +
                         kP * errorR +
                         kI * totalErrorR +
                         kD * (errorR - lastErrorR) -
                         kH * errorH
                         :
                         -kV * nextRightValues.velocity +
                         -kA * nextRightValues.acceleration +
                         kP * errorR +
                         kI * totalErrorR +
                         kD * (errorR - lastErrorR) -
                         kH * errorH;
*/

    double leftOutput =
                            kV * nextLeftValues.velocity +
                            kA * nextLeftValues.acceleration +
                            kP * errorL +
                            kI * totalErrorL +
                            kD * (errorL - lastErrorL) +
                            kH * errorH;
    double rightOutput = 
                            kV * nextRightValues.velocity +
                            kA * nextRightValues.acceleration +
                            kP * errorR +
                            kI * totalErrorR +
                            kD * (errorR - lastErrorR) -
                            kH * errorH;

    //REVERSE
    


    leftOutput /= 10;
    rightOutput /= 10;
    
    Robot.drivetrain.drive(-leftOutput, -rightOutput);
    //System.out.println(leftOutput);
    lastErrorL = errorL;
    lastErrorR = errorR;
    //System.out.println(initialHeading);
    
  }

  // Make this return true when this Command no longer needs to run execute()
  @Override
  protected boolean isFinished() {
    if (
      leftTrajectory.hasNext() || 
      rightTrajectory.hasNext() // ||
      // leftTrajectory == null ||
      // rightTrajectory == null // ||
      // Timer.getFPGATimestamp() - startTime >= leftTrajectory.getTotalTime() ||
      // Timer.getFPGATimestamp() - startTime >= rightTrajectory.getTotalTime()
    ) { return true; }
    return false;
  }

  // Called once after isFinished returns true
  @Override
  protected void end() {
    System.out.println(leftTrajectory.currentIndex);
    System.out.println(leftTrajectory.points.size());
  }

  // Called when another command which requires one or more of the same
  // subsystems is scheduled to run
  @Override
  protected void interrupted() {
  }
}
