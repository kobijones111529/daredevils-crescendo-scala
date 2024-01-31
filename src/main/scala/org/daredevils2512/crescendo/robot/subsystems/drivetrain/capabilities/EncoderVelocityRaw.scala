package org.daredevils2512.crescendo.robot.subsystems.drivetrain.capabilities

import algebra.instances.all.given
import coulomb.*
import coulomb.ops.algebra.all.given
import coulomb.policy.standard.given
import coulomb.syntax.*
import coulomb.units.constants.*

import scala.language.implicitConversions

trait EncoderVelocityRaw[U](implicit val numeric: Numeric[U]):
  def left: U
  def right: U
