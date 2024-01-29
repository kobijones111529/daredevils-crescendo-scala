package org.daredevils2512.crescendo.robot.subsystems.drivetrain.capabilities

import algebra.instances.all.given
import coulomb.*
import coulomb.ops.algebra.all.given
import coulomb.policy.standard.given
import coulomb.syntax.*
import coulomb.units.si.{*, given}

import scala.language.implicitConversions

trait EncoderVelocity:
  def left: Quantity[Double, Meter / Second]
