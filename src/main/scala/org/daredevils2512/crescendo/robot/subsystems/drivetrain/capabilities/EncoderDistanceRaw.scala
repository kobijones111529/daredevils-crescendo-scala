package org.daredevils2512.crescendo.robot.subsystems.drivetrain.capabilities

import scala.annotation.unused

trait EncoderDistanceRaw[U](implicit val numeric: Numeric[U]):
  def left: U
  def right: U
