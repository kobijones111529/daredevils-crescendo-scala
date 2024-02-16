package org.daredevils2512.crescendo.robot.commands.drive

import algebra.instances.all.given
import coulomb.*
import coulomb.ops.algebra.all.given
import coulomb.policy.standard.given
import coulomb.syntax.*
import coulomb.units.si.{*, given}
import edu.wpi.first.math.controller.PIDController
import edu.wpi.first.wpilibj2.command.Command
import org.daredevils2512.crescendo.robot.subsystems.drivetrain.Drivetrain
import org.daredevils2512.crescendo.robot.subsystems.drivetrain.capabilities.{EncoderDistance, SimpleDrive}
import org.daredevils2512.util.uom.abs

import scala.language.implicitConversions

def driveDistance(
    drivetrain: Drivetrain,
    simpleDrive: SimpleDrive,
    encoderDistance: EncoderDistance,
    dist: => Quantity[Double, Meter],
    tolerance: Option[Quantity[Double, Meter]],
    maxOutput: => Double = 1
): Command = new Command {
  case class Target(
      left: Quantity[Double, Meter],
      right: Quantity[Double, Meter]
  )

  var getTarget: () => Option[Target] = () => None
  val leftPID = PIDController(0, 0, 0)
  val rightPID = PIDController(0, 0, 0)

  override def initialize(): Unit =
    getTarget = () =>
      Some(
        Target(
          left = encoderDistance.left + dist,
          right = encoderDistance.right + dist
        )
      )
    leftPID.reset()
    rightPID.reset()
  end initialize

  override def execute(): Unit =
    for {
      target <- getTarget()
    } yield {
      val left = leftPID.calculate(
        encoderDistance.left.toUnit[Meter].value,
        target.left.toUnit[Meter].value
      )
      val right = rightPID.calculate(
        encoderDistance.right.toUnit[Meter].value,
        target.right.toUnit[Meter].value
      )
      simpleDrive.tank(left, right)
    }
  end execute

  override def end(interrupted: Boolean): Unit =
    simpleDrive.stop()

  override def isFinished(): Boolean =
    val res = for {
      target <- getTarget()
    } yield {
      tolerance match
        case None => false
        case Some(tolerance) =>
          val atLeft = abs(encoderDistance.left - target.left) <= tolerance
          val atRight = abs(encoderDistance.right - target.right) <= tolerance
          atLeft && atRight
    }
    res.getOrElse(true)
}
