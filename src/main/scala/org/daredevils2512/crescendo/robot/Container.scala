package org.daredevils2512.crescendo.robot

import algebra.instances.all.given
import coulomb.*
import coulomb.ops.algebra.all.given
import coulomb.policy.standard.given
import coulomb.syntax.*
import coulomb.units.si.{*, given}
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
import org.daredevils2512.crescendo.robot.subsystems.arm.Arm
import org.daredevils2512.crescendo.robot.subsystems.climber.Climber
import org.daredevils2512.crescendo.robot.subsystems.drivetrain.Drivetrain
import org.daredevils2512.crescendo.robot.subsystems.extake.Extake
import org.daredevils2512.crescendo.robot.subsystems.intake.Intake

class Container:
  object networkTables:
    val table: NetworkTable =
      NetworkTableInstance.getDefault().getTable("Robot")
    object publishers
  end networkTables

  val xbox: CommandXboxController = CommandXboxController(
    config.controllers.xbox
  )

  val drivetrain: Option[Drivetrain] =
    None
    Some(
      Drivetrain(
        config.drivetrain,
        networkTables.table.getSubTable("Drivetrain")
      )
    )
  val intake: Option[Intake] =
    Some(
      Intake(config.intake)
    )
  val arm: Option[Arm] =
    Some(
      Arm(config.arm)
    )
  val extake: Option[Extake] =
    Some(
      Extake(
        config.extake,
        networkTables.table.getSubTable("Extake")
      )
    )
  val climberLeft: Option[Climber] =
    Some(
      Climber(
        config.climberLeft,
        networkTables.table.getSubTable("Climber left")
      )
    )
  val climberRight: Option[Climber] =
    Some(
      Climber(
        config.climberRight,
        networkTables.table.getSubTable("Climber right")
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
    end for

    for {
      drivetrain <- drivetrain
      simpleDrive <- drivetrain.simpleDrive
      encoderDistance <- drivetrain.encoderDistance
    } yield
      val command = commands.drive.driveDistance(
        drivetrain = drivetrain,
        simpleDrive = simpleDrive,
        encoderDistance = encoderDistance,
        dist = 10.withUnit[Meter],
        tolerance = Some(10.withUnit[Meter / 100]),
        maxOutput = 0.5
      )
      // TODO: Bind command
    end for

    for {
      intake <- intake
    } yield
      val command = commands.intake.run(
        intake = intake,
        simpleIntake = intake.simpleIntake,
        speed = config.control.intakeSpeed
      )
      xbox.rightBumper().whileTrue(command)
    end for

    for {
      arm <- arm
    } yield
      def speed = MathUtil.applyDeadband(-xbox.getRightY(), 0.1)
      arm.setDefaultCommand(
        commands.arm.run(
          arm = arm,
          simpleActuate = arm.simpleActuate,
          speed = speed
        )
      )
    end for

    for {
      extake <- extake
    } yield
      xbox
        .a()
        .whileTrue(
          commands.extake.run(
            extake = extake,
            simpleFeed = extake.simpleFeed,
            speed = config.control.extakeSpeed
          )
        )
      xbox
        .b()
        .whileTrue(
          commands.extake.run(
            extake = extake,
            simpleFeed = extake.simpleFeed,
            speed = -config.control.extakeSpeed
          )
        )
    end for

    for {
      intake <- intake
      extake <- extake
    } yield xbox
      .rightTrigger()
      .whileTrue(
        commands.intake
          .run(
            intake = intake,
            simpleIntake = intake.simpleIntake,
            speed = config.control.intakeSpeed
          )
          .alongWith(
            commands.extake.run(
              extake = extake,
              simpleFeed = extake.simpleFeed,
              speed = config.control.extakeSpeed
            )
          )
      )
    end for

    for {
      climberLeft <- climberLeft
      climberRight <- climberRight
    } yield xbox
      .x()
      .whileTrue(
        commands.climber.runClimber(
          left = climberLeft,
          right = climberRight,
          simpleClimberLeft = climberLeft.simpleClimber,
          simpleClimberRight = climberRight.simpleClimber,
          speed = config.control.climberSpeed
        )
      )
    end for
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
