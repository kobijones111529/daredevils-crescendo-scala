package org.daredevils2512.crescendo.robot

import org.daredevils2512.crescendo.robot.subsystems.drivetrain.Config as DrivetrainConfig

package object config:
  object controllers:
    val xbox: Int = 0
  end controllers

  val drivetrain: DrivetrainConfig = DrivetrainConfig(
    drive = DrivetrainConfig.Drive(
      left = DrivetrainConfig.Drive.Group(
        primary = DrivetrainConfig.Drive.Group.Primary(1, false),
        backups = Array(2),
        rateLimit = Some(3),
        encoder = Some(
          DrivetrainConfig.Drive.Group.Encoder(
            distancePerUnit = None
          )
        )
      ),
      right = DrivetrainConfig.Drive.Group(
        primary = DrivetrainConfig.Drive.Group.Primary(3, true),
        backups = Array(4),
        rateLimit = Some(3),
        encoder = Some(
          DrivetrainConfig.Drive.Group.Encoder(
            distancePerUnit = None
          )
        )
      )
    )
  )
