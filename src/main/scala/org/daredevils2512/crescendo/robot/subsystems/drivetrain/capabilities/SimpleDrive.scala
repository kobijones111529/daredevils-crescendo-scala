package org.daredevils2512.crescendo.robot.subsystems.drivetrain.capabilities

trait SimpleDrive:
  def stop(): Unit
  def tank(left: Double, right: Double): Unit
  def arcade(left: Double, right: Double): Unit
