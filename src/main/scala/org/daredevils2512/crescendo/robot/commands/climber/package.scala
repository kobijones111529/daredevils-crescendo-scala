package org.daredevils2512.crescendo.robot.commands

import edu.wpi.first.wpilibj2.command.Command
import org.daredevils2512.crescendo.robot.subsystems.climber.Climber
import org.daredevils2512.crescendo.robot.subsystems.climber.capabilities.SimpleClimber

package object climber:
  def runClimber(
      left: Climber,
      right: Climber,
      simpleClimberLeft: SimpleClimber,
      simpleClimberRight: SimpleClimber,
      leftSpeed: => Double,
      rightSpeed: => Double
  ): Command =
    val runLeft = left
      .run(() => simpleClimberLeft.run(leftSpeed))
      .finallyDo(() => simpleClimberLeft.stop())
    val runRight = right
      .run(() => simpleClimberRight.run(rightSpeed))
      .finallyDo(() => simpleClimberRight.stop())

    runLeft.alongWith(runRight)
  end runClimber

  def runClimber(
      left: Climber,
      right: Climber,
      simpleClimberLeft: SimpleClimber,
      simpleClimberRight: SimpleClimber,
      speed: => Double
  ): Command = runClimber(
    left = left,
    right = right,
    simpleClimberLeft = simpleClimberLeft,
    simpleClimberRight = simpleClimberRight,
    leftSpeed = speed,
    rightSpeed = speed
  )
  end runClimber
