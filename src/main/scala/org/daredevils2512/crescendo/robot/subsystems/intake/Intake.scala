package org.daredevils2512.crescendo.robot.subsystems.intake

import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX

class Intake:
  case class MotorGroup(primary: WPI_TalonSRX)

  private val motorGroup: MotorGroup = MotorGroup(primary = WPI_TalonSRX(10))
