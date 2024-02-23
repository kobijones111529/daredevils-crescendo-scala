package org.daredevils2512.crescendo.robot.subsystems.climber

case class Config(left: Config.MotorGroup, right: Config.MotorGroup)
object Config:
  case class MotorGroup(primary: MotorGroup.Primary, inverted: Boolean)
  object MotorGroup:
    case class Primary(id: Int)
