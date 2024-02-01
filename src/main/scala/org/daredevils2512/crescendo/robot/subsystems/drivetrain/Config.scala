package org.daredevils2512.crescendo.robot.subsystems.drivetrain

import coulomb.*
import coulomb.units.si.*

case class Config(drive: Config.Drive, pigeon: Option[Config.Pigeon])

object Config:
  case class Drive(left: Drive.Group, right: Drive.Group)
  object Drive:
    case class Group(
        primary: Group.Primary,
        backups: Array[Int],
        rateLimit: Option[Double],
        encoder: Option[Group.Encoder]
    )
    object Group:
      case class Primary(id: Int, inverted: Boolean)
      case class Encoder(distancePerUnit: Option[Quantity[Double, Meter]])
    end Group
  end Drive

  case class Pigeon(id: Int)
