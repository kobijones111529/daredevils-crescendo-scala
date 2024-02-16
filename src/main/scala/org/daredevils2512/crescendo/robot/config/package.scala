package org.daredevils2512.crescendo.robot

import org.daredevils2512.crescendo.robot.subsystems.drivetrain.Config as DrivetrainConfig
import org.daredevils2512.crescendo.robot.subsystems.extake.Config as ExtakeConfig

package object config:
  object controllers:
    val xbox: Int = 0
  end controllers

  val drivetrain: DrivetrainConfig = DrivetrainConfig(
    drive = DrivetrainConfig.Drive(
      left = DrivetrainConfig.Drive.Group(
        primary = DrivetrainConfig.Drive.Group.Primary(can.drivetrain.left.primary, false),
        backups = can.drivetrain.left.backups,
        rateLimit = Some(3),
        feedforward = None,
        encoder = Some(
          DrivetrainConfig.Drive.Group.Encoder(
            distancePerUnit = None
          )
        )
      ),
      right = DrivetrainConfig.Drive.Group(
        primary = DrivetrainConfig.Drive.Group.Primary(can.drivetrain.right.primary, true),
        backups = can.drivetrain.right.backups,
        rateLimit = Some(3),
        feedforward = None,
        encoder = Some(
          DrivetrainConfig.Drive.Group.Encoder(
            distancePerUnit = None
          )
        )
      ),
      trackWidth = None
    ),
    pigeon = Some(DrivetrainConfig.Pigeon(can.drivetrain.pigeon))
  )

  val extake: ExtakeConfig = ExtakeConfig(
    actuator = ExtakeConfig.Actuator(
      motorGroup = ExtakeConfig.Actuator.MotorGroup(
        primary = ExtakeConfig.Actuator.MotorGroup.Primary(can.extake.actuator.primary)
      )
    ),
    feed = ExtakeConfig.Feed(
      motorGroup = ExtakeConfig.Feed.MotorGroup(
        primary = ExtakeConfig.Feed.MotorGroup.Primary(can.extake.feed.primary)
      )
    )
  )
