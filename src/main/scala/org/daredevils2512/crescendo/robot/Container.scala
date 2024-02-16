package org.daredevils2512.crescendo.robot

import edu.wpi.first.math.MathUtil
import edu.wpi.first.networktables.{
  DoublePublisher,
  NetworkTable,
  NetworkTableEntry,
  NetworkTableInstance,
  Topic
}
import edu.wpi.first.wpilibj2.command.Command
import edu.wpi.first.wpilibj2.command.button.CommandXboxController
import org.daredevils2512.crescendo.robot.subsystems.drivetrain.Drivetrain
import org.daredevils2512.crescendo.robot.subsystems.extake.Extake

import algebra.instances.all.given
import coulomb.*
import coulomb.ops.algebra.all.given
import coulomb.policy.standard.given
import coulomb.syntax.*
import coulomb.units.si.{*, given}

class Container:
  object networkTables:
    val table: NetworkTable =
      NetworkTableInstance.getDefault().getTable("Robot container")
    object publishers
  end networkTables

  val xbox: CommandXboxController = CommandXboxController(
    config.controllers.xbox
  )

  val drivetrain: Option[Drivetrain] =
    None
    // Some(
    //   Drivetrain(
    //     config.drivetrain,
    //     NetworkTableInstance.getDefault().getTable("Drivetrain")
    //   )
    // )
  val extake: Option[Extake] =
    Some(
      Extake(config.extake)
    )

  def periodic(): Unit = ()

  configureBindings()

  private def configureBindings(): Unit =
    for {
      drivetrain <- drivetrain
      simpleDrive <- drivetrain.simpleDrive
    } yield
      def move = MathUtil.applyDeadband(-xbox.getLeftY(), 0.1)
      def turn = MathUtil.applyDeadband(xbox.getLeftX(), 0.1)
      drivetrain.setDefaultCommand(
        commands.drive.arcade(drivetrain, simpleDrive, move, turn)
      )
    end for

    for {
      drivetrain <- drivetrain
      simpleDrive <- drivetrain.simpleDrive
      encoderDistance <- drivetrain.encoderDistance
    } yield
      val command = commands.drive.driveDistance(drivetrain, simpleDrive, encoderDistance, 10.withUnit[Meter])
      xbox.b().onTrue(command)
    end for

    for {
      extake <- extake
    } yield
      def actuate = MathUtil.applyDeadband(-xbox.getRightY(), 0.1)
      extake.setDefaultCommand(
        extake.runOnce(() =>
          extake.simpleActuate.run(actuate)
        )
      )

      xbox.a().onTrue(
        extake.runOnce(() =>
          extake.simpleFeed.run(-1.0)
        )
      )
      xbox.a().onFalse(
        extake.runOnce(() => extake.simpleFeed.stop())
      )

      xbox.y().onTrue(
        extake.runOnce(() =>
          extake.simpleFeed.run(1.0)
        )
      )
      xbox.y().onFalse(
        extake.runOnce(() =>
          extake.simpleFeed.stop()
        )
      )
    end for
  end configureBindings

  def auto: Option[Command] = None
