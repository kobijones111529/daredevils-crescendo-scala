package org.daredevils2512.crescendo.robot.subsystems.drivetrain.capabilities

trait SimpleDrive:
  def stop(): Unit
  def tankDrive(left: Double, right: Double): Unit
  def arcadeDrive(left: Double, right: Double): Unit
