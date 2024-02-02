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

class Container:
  object networkTables:
    val table: NetworkTable =
      NetworkTableInstance.getDefault().getTable("Robot container")
    object publishers
  end networkTables

  val xbox: CommandXboxController = CommandXboxController(
    config.controllers.xbox
  )

  val drivetrain: Option[Drivetrain] = Some(
    Drivetrain(
      config.drivetrain,
      NetworkTableInstance.getDefault().getTable("Drivetrain")
    )
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
  end configureBindings

  def auto: Option[Command] = None
