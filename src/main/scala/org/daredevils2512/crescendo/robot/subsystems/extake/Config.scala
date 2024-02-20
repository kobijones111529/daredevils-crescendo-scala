package org.daredevils2512.crescendo.robot.subsystems.extake

import org.daredevils2512.crescendo.robot.subsystems.extake.Config.Actuator.MotorGroup

case class Config(
    actuator: Config.Actuator,
    feed: Config.Feed
)
object Config:
  case class Actuator(motorGroup: Actuator.MotorGroup)
  object Actuator:
    case class MotorGroup(primary: MotorGroup.Primary)
    object MotorGroup:
      case class Primary(id: Int)
  end Actuator

  case class Feed(motorGroup: Feed.MotorGroup)
  object Feed:
    case class MotorGroup(primary: MotorGroup.Primary)
    object MotorGroup:
      case class Primary(id: Int)
