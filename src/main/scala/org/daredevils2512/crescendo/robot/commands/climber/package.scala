package org.daredevils2512.crescendo.robot.commands

import edu.wpi.first.wpilibj2.command.Command
import org.daredevils2512.crescendo.robot.subsystems.climber.Climber
import org.daredevils2512.crescendo.robot.subsystems.climber.capabilities.SimpleClimber

package object climber:
  def runClimber(
      climber: Climber,
      simpleClimber: SimpleClimber,
      leftSpeed: => Double,
      rightSpeed: => Double
  ): Command =
    climber
      .run(() => simpleClimber.run(leftSpeed, rightSpeed))
      .finallyDo(() => simpleClimber.stop())
  end runClimber

  def runClimber(
      climber: Climber,
      simpleClimber: SimpleClimber,
      speed: => Double
  ): Command = runClimber(
    climber = climber,
    simpleClimber = simpleClimber,
    leftSpeed = speed,
    rightSpeed = speed
  )

  end runClimber
