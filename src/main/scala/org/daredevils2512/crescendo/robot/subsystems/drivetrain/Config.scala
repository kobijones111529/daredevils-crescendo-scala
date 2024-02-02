package org.daredevils2512.crescendo.robot.subsystems.drivetrain

import coulomb.*
import coulomb.units.si.*
import edu.wpi.first.math.controller.SimpleMotorFeedforward

case class Config(drive: Config.Drive, pigeon: Option[Config.Pigeon])

object Config:
  case class Drive(
      left: Drive.Group,
      right: Drive.Group,
      trackWidth: Option[Quantity[Double, Meter]]
  )
  object Drive:
    case class Group(
        primary: Group.Primary,
        backups: Array[Int],
        rateLimit: Option[Double],
        feedforward: Option[SimpleMotorFeedforward],
        encoder: Option[Group.Encoder]
    )
    object Group:
      case class Primary(id: Int, inverted: Boolean)
      case class Encoder(distancePerUnit: Option[Quantity[Double, Meter]])
    end Group
  end Drive

  case class Pigeon(id: Int)
