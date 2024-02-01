package org.daredevils2512.crescendo.robot.subsystems.drivetrain.capabilities

import coulomb.*
import coulomb.policy.standard.given
import coulomb.syntax.*
import coulomb.units.accepted.*
import coulomb.units.constants.*
import coulomb.units.si.*

trait Gyro {
  def angle: Quantity[Double, Degree]
  def rate: Quantity[Double, Degree / Second]
}
