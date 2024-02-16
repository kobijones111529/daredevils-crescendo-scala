package org.daredevils2512.util

import algebra.instances.all.given
import coulomb.*
import coulomb.ops.algebra.all.given
import coulomb.ops.{Neg, Ord}
import coulomb.policy.standard.given
import coulomb.syntax.*

import scala.language.implicitConversions

package object uom:
  def abs[V, U](
      quantity: Quantity[V, U]
  )(using neg: Neg[V, U], ord: Ord[V, U, V, U]): Quantity[V, U] =
    if quantity < -quantity
    then -quantity
    else quantity
