package org.daredevils2512.crescendo.robot.subsystems.climber.capabilities

trait SimpleClimber:
  def stop(): Unit
  def run(speed: Double): Unit
