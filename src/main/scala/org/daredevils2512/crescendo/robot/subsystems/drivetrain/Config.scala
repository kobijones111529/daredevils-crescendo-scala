package org.daredevils2512.crescendo.robot.subsystems.drivetrain

import coulomb.*
import coulomb.units.si.*

case class Config(drive: Config.Drive)

object Config:
  case class Drive(left: Drive.Group, right: Drive.Group)
  object Drive:
    case class Group(
        primary: Int,
        backups: Array[Int],
        rateLimit: Option[Double],
        encoder: Option[Group.Encoder]
    )
    object Group:
      case class Encoder(distancePerUnit: Option[Quantity[Double, Meter]])
