package org.daredevils2512.crescendo.robot.subsystems.drivetrain.capabilities

import algebra.instances.all.given
import coulomb.*
import coulomb.ops.algebra.all.given
import coulomb.policy.standard.given
import coulomb.syntax.*
import coulomb.units.accepted.{*, given}
import coulomb.units.si.{*, given}

import scala.language.implicitConversions

trait VelocityDrive:
  def tank(
      left: Quantity[Double, Meter / Second],
      right: Quantity[Double, Meter / Second]
  ): Unit
  def arcade(
      move: Quantity[Double, Meter / Second],
      turn: Quantity[Double, Degree / Second]
  ): Unit
