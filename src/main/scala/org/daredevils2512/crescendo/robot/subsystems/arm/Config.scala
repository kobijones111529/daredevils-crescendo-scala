package org.daredevils2512.crescendo.robot.subsystems.arm

case class Config(
    motorGroup: Config.MotorGroup,
    limitSwitches: Config.LimitSwitches
)
object Config:
  case class MotorGroup(primary: Int, inverted: Boolean)
  case class LimitSwitches(bottom: Option[Int], top: Option[Int])
