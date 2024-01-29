package org.daredevils2512.crescendo.robot.subsystems.drivetrain.capabilities

import coulomb.*
import coulomb.units.si.*

trait EncoderDistance:
  def left: Quantity[Double, Meter]
  def right: Quantity[Double, Meter]
