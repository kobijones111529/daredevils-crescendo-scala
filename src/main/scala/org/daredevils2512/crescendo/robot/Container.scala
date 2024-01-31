package org.daredevils2512.crescendo.robot

import com.ctre.phoenix6.hardware.Pigeon2
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
  val drivetrain: Drivetrain =
    Drivetrain(
      config.drivetrain,
      NetworkTableInstance.getDefault().getTable("Drivetrain")
    )

  def periodic(): Unit =
    pigeon match
      case None => networkTables.publishers.pigeonAngle.set(0)
      case Some(pigeon) =>
        networkTables.publishers.pigeonAngle.set(pigeon.getAngle())
  end periodic

  configureBindings()

  private def configureBindings(): Unit =
    drivetrain.setDefaultCommand(
      drivetrain.run(() =>
        drivetrain.simpleDrive.foreach(drive =>
          drive.arcadeDrive(-xbox.getLeftY(), xbox.getLeftX())
        )
      )
    )
  end configureBindings

  def auto: Option[Command] = None
