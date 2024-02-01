package org.daredevils2512.crescendo.robot

import com.ctre.phoenix6.hardware.Pigeon2
import edu.wpi.first.math.MathUtil
import edu.wpi.first.networktables.{
  DoublePublisher,
  NetworkTable,
  NetworkTableEntry,
  NetworkTableInstance,
  Topic
}
import edu.wpi.first.wpilibj2.command.button.CommandXboxController
import edu.wpi.first.wpilibj2.command.{Command, RamseteCommand}
import org.daredevils2512.crescendo.robot.subsystems.drivetrain.Drivetrain

class Container:
  object networkTables:
    val table: NetworkTable =
      NetworkTableInstance.getDefault().getTable("Robot container")
    object publishers:
      val pigeonAngle: DoublePublisher =
        table.getDoubleTopic("Pigeon angle (deg)").publish()
    end publishers
  end networkTables

  val xbox: CommandXboxController = CommandXboxController(
    config.controllers.xbox
  )

  val pigeon: Option[Pigeon2] = None
  val drivetrain: Option[Drivetrain] = Some(
    Drivetrain(
      config.drivetrain,
      NetworkTableInstance.getDefault().getTable("Drivetrain")
    )
  )

  def periodic(): Unit =
    pigeon match
      case None => networkTables.publishers.pigeonAngle.set(0)
      case Some(pigeon) =>
        networkTables.publishers.pigeonAngle.set(pigeon.getAngle())
  end periodic

  configureBindings()

  private def configureBindings(): Unit =
    for {
      drivetrain <- drivetrain
    } yield
      drivetrain.setDefaultCommand(
        drivetrain.run(() =>
          drivetrain.simpleDrive.foreach(drive =>
            val move = MathUtil.applyDeadband(-xbox.getLeftY, 0.1)
            val turn = MathUtil.applyDeadband(xbox.getLeftX, 0.1)
            drive.arcadeDrive(move, turn)
          )
        )
      )

      // val command = RamseteCommand(???, ???, ???, ???, ???, drivetrain)
    end for
  end configureBindings

  def auto: Option[Command] = None
