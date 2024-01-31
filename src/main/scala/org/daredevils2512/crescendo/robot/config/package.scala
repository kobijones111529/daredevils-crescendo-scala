package org.daredevils2512.crescendo.robot

import org.daredevils2512.crescendo.robot.subsystems.drivetrain.Config as DrivetrainConfig

package object config:
  object controllers:
    val xbox: Int = 0
  end controllers

  val drivetrain: DrivetrainConfig = DrivetrainConfig(
    drive = DrivetrainConfig.Drive(
      left = DrivetrainConfig.Drive.Group(
        primary = 1,
        backups = Array(),
        rateLimit = Some(3),
        encoder = Some(
          DrivetrainConfig.Drive.Group.Encoder(
            distancePerUnit = None
          )
        )
      ),
      right = DrivetrainConfig.Drive.Group(
        primary = 2,
        backups = Array(),
        rateLimit = Some(3),
        encoder = Some(
          DrivetrainConfig.Drive.Group.Encoder(
            distancePerUnit = None
          )
        )
      )
    )
  )
