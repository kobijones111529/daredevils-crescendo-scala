package org.daredevils2512.crescendo.robot.subsystems.arm.capabilities

trait SimpleActuate:
  def stop(): Unit
  def run(speed: Double): Unit
