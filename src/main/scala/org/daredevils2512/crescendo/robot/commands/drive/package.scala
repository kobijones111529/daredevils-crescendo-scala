package org.daredevils2512.crescendo.robot.commands

import algebra.instances.all.given
import coulomb.*
import coulomb.ops.algebra.all.given
import coulomb.policy.standard.given
import coulomb.syntax.*
import coulomb.units.si.{*, given}
import edu.wpi.first.math.controller.RamseteController
import edu.wpi.first.math.geometry.Pose2d
import edu.wpi.first.math.kinematics.DifferentialDriveKinematics
import edu.wpi.first.math.trajectory.Trajectory
import edu.wpi.first.wpilibj2.command.{Command, RamseteCommand}
import org.daredevils2512.crescendo.robot.subsystems.drivetrain.Drivetrain
import org.daredevils2512.crescendo.robot.subsystems.drivetrain.capabilities.{EncoderDistance, SimpleDrive, VelocityDrive}

import scala.language.implicitConversions

package object drive:
  def arcade(
      drivetrain: Drivetrain,
      simpleDrive: SimpleDrive,
      move: => Double,
      turn: => Double
  ): Command =
    drivetrain
      .run(() => simpleDrive.arcade(move, turn))
      .finallyDo(() => simpleDrive.stop())
  end arcade

  def driveDistance(
      drivetrain: Drivetrain,
      simpleDrive: SimpleDrive,
      encoderDistance: EncoderDistance,
      distance: Quantity[Double, Meter]
  ): Command = ???

  def ramsete(
      drivetrain: Drivetrain,
      velocityDrive: VelocityDrive,
      trajectory: Trajectory,
      pose: => Pose2d,
      kinematics: DifferentialDriveKinematics
  ): Command =
    def drive(left: Double, right: Double): Unit =
      velocityDrive.tank(
        left.withUnit[Meter / Second],
        right.withUnit[Meter / Second]
      )
    end drive

    RamseteCommand(
      trajectory,
      () => pose,
      RamseteController(),
      kinematics,
      (left, right) => drive(left.toDouble, right.toDouble),
      drivetrain
    )
  end ramsete
