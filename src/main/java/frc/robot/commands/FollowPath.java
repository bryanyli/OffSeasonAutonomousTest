/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot.commands;

import java.io.File;

import edu.wpi.first.wpilibj.command.Command;
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
  private final double initialHeading;
  private final double pathStartHeading;

  private double kV = 1 / RobotMap.MAX_VELOCITY; // Velocity
  private double kA = .035; // Acceleration
  private double kH = -.009; // Heading
  private double kP = .12; // Proportional
  private double kI = 0; // Integral
  private double kD = 0; // Derivative


  // Variables used in execute, declared here to avoid GC
  private double errorL;
  private double errorR;
  private double totalErrorL = 0d;
  private double totalErrorR = 0d;
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
    requires(Robot.driveTrain);
    this.pathName = pathName;
    reverse = args.toString().contains("r") || args.toString().contains("R");
    mirror = args.toString().contains("m") || args.toString().contains("M");
    initialHeading = boundTo180(Robot.gyro.getYaw()); 
    startTime = Timer.getFPGATimestamp();
    pathStartHeading = leftTrajectory.getStartHeading();
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
      File leftFile = new File(Filesystem.getDeployDirectory() + "paths/" + pathName + "_left.csv");
      File rightFile = new File(Filesystem.getDeployDirectory() + "paths/" + pathName + "_right.csv");
      leftTrajectory = (mirror^reverse) ? new Trajectory(rightFile) : new Trajectory(leftFile);
      rightTrajectory = (mirror^reverse) ? new Trajectory(leftFile) : new Trajectory(rightFile);
    } catch (IOException exc) {
      exc.printStackTrace();
      leftTrajectory = null;
      rightTrajectory = null;
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
    readTrajectory();
    shuffleboardSetup();
  } 

  // Called repeatedly when this Command is scheduled to run
  @Override
  protected void execute() {
    nextLeftValues = leftTrajectory.next();
    nextRightValues = rightTrajectory.next();
    errorL = (!reverse ? nextLeftValues.position : -nextLeftValues.position) - Robot.driveTrain.getLeftDistance();
    errorR = (!reverse ? nextRightValues.position : -nextLeftValues.position) - Robot.driveTrain.getRightDistance();
    totalErrorL += errorL;
    totalErrorR += errorR;
    errorH = (nextLeftValues.heading - pathStartHeading) - (boundTo180(Robot.gyro.getYaw()) - initialHeading);

    double leftOutput = reverse ?
                        kV * nextLeftValues.velocity +
                        kA * nextLeftValues.acceleration +
                        kP * errorL +
                        kI * totalErrorL +
                        kD * errorL - lastErrorL +
                        kH * errorH
                        :
                        -kV * nextLeftValues.velocity +
                        -kA * nextLeftValues.acceleration +
                        kP * errorL +
                        kI * totalErrorL +
                        kD * errorL - lastErrorL +
                        kH * errorH;
                        
    double rightOutput = reverse ?
                        kV * nextRightValues.velocity +
                        kA * nextRightValues.acceleration +
                        kP * errorR +
                        kI * totalErrorR +
                        kD * errorR - lastErrorR -
                        kH * errorH
                        :
                        -kV * nextRightValues.velocity +
                        -kA * nextRightValues.acceleration +
                        kP * errorR +
                        kI * totalErrorR +
                        kD * errorR - lastErrorR -
                        kH * errorH;
    
    Robot.driveTrain.drive(leftOutput, rightOutput);

    lastErrorL = errorL;
    lastErrorR = errorR;
  }

  // Make this return true when this Command no longer needs to run execute()
  @Override
  protected boolean isFinished() {
    if (
      !leftTrajectory.hasNext() || 
      !rightTrajectory.hasNext() ||
      leftTrajectory == null ||
      rightTrajectory == null ||
      Timer.getFPGATimestamp() - startTime >= leftTrajectory.getTotalTime() ||
      Timer.getFPGATimestamp() - startTime >= rightTrajectory.getTotalTime()
    ) { return true; }
    return false;
  }

  // Called once after isFinished returns true
  @Override
  protected void end() {
  }

  // Called when another command which requires one or more of the same
  // subsystems is scheduled to run
  @Override
  protected void interrupted() {
  }
}
