package org.daredevils2512.crescendo.robot.subsystems.extake.capabilities

trait SimpleFeed:
  def stop(): Unit
  def run(speed: Double): Unit
