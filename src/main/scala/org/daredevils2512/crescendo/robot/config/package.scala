package org.daredevils2512.crescendo.robot

import org.daredevils2512.crescendo.robot.subsystems.arm.Config as ArmConfig
import org.daredevils2512.crescendo.robot.subsystems.climber.Config as ClimberConfig
import org.daredevils2512.crescendo.robot.subsystems.drivetrain.Config as DrivetrainConfig
import org.daredevils2512.crescendo.robot.subsystems.extake.Config as ExtakeConfig
import org.daredevils2512.crescendo.robot.subsystems.intake.Config as IntakeConfig

package object config:
  object control:
    val intakeSpeed: Double = 0.8
    val extakeSpeed: Double = 1.0
    val climberSpeed: Double = 1.0
  end control

  object controllers:
    val xbox: Int = 0
  end controllers

  val drivetrain: DrivetrainConfig = DrivetrainConfig(
    drive = DrivetrainConfig.Drive(
      left = DrivetrainConfig.Drive.Group(
        primary = DrivetrainConfig.Drive.Group
          .Primary(can.drivetrain.left.primary, false),
        backups = can.drivetrain.left.backups,
        rateLimit = Some(3),
        feedforward = None,
        encoder = Some(
          DrivetrainConfig.Drive.Group.Encoder(
            distancePerRevolution = None
          )
        )
      ),
      right = DrivetrainConfig.Drive.Group(
        primary = DrivetrainConfig.Drive.Group
          .Primary(can.drivetrain.right.primary, true),
        backups = can.drivetrain.right.backups,
        rateLimit = Some(3),
        feedforward = None,
        encoder = Some(
          DrivetrainConfig.Drive.Group.Encoder(
            distancePerRevolution = None
          )
        )
      ),
      trackWidth = None
    ),
    pigeon = Some(DrivetrainConfig.Pigeon(can.drivetrain.pigeon))
  )

  val intake: IntakeConfig = IntakeConfig(
    motorGroup = IntakeConfig.MotorGroup(
      primary = can.intake.primary,
      inverted = false
    )
  )

  val arm: ArmConfig = ArmConfig(
    motorGroup = ArmConfig.MotorGroup(
      primary = can.arm.primary,
      inverted = false
    )
  )

  val extake: ExtakeConfig = ExtakeConfig(
    motorGroup = ExtakeConfig.MotorGroup(
      primary = ExtakeConfig.MotorGroup.Primary(can.extake.primary),
      inverted = true
    )
  )

  val climberLeft: ClimberConfig = ClimberConfig(
    motorGroup = ClimberConfig.MotorGroup(
      primary = ClimberConfig.MotorGroup.Primary(can.climber.left.primary),
      inverted = false
    )
  )

  val climberRight: ClimberConfig = ClimberConfig(
    motorGroup = ClimberConfig.MotorGroup(
      primary = ClimberConfig.MotorGroup.Primary(can.climber.right.primary),
      inverted = false
    )
  )
