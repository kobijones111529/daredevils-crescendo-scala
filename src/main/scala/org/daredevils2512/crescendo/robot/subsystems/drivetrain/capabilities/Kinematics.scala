package org.daredevils2512.crescendo.robot.subsystems.drivetrain.capabilities

import edu.wpi.first.math.kinematics.DifferentialDriveKinematics

trait Kinematics:
  def kinematics: DifferentialDriveKinematics
