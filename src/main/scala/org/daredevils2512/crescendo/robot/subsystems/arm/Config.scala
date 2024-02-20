package org.daredevils2512.crescendo.robot.subsystems.arm

case class Config(motorGroup: Config.MotorGroup)
object Config:
  case class MotorGroup(primary: Int, inverted: Boolean)
