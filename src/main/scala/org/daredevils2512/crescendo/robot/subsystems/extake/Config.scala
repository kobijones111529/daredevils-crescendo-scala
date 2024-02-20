package org.daredevils2512.crescendo.robot.subsystems.extake

case class Config(
    motorGroup: Config.MotorGroup
)
object Config:
  case class MotorGroup(primary: MotorGroup.Primary, inverted: Boolean)
  object MotorGroup:
    case class Primary(id: Int)
