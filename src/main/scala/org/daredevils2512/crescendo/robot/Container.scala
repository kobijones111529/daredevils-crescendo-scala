package org.daredevils2512.crescendo.robot

import edu.wpi.first.math.MathUtil
import edu.wpi.first.networktables.{
  DoublePublisher,
  NetworkTable,
  NetworkTableEntry,
  NetworkTableInstance,
  Topic
}
import edu.wpi.first.wpilibj2.command.button.CommandXboxController
import edu.wpi.first.wpilibj2.command.{Command, RunCommand}
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

  def auto: Option[Command] =
    for {
      drivetrain <- drivetrain
      simpleDrive <- drivetrain.simpleDrive
    } yield commands.drive
      .driveTime(drivetrain, simpleDrive, 1, 0, 1)
      .andThen(commands.drive.driveTime(drivetrain, simpleDrive, 0, 1, 1))
      .alongWith(
        new RunCommand(() => println("Doing a thing"))
          .finallyDo(() => println("Done"))
          .withTimeout(5)
      )
    end for
