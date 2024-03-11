package org.daredevils2512.crescendo.robot.commands

import edu.wpi.first.wpilibj2.command.Command
import org.daredevils2512.crescendo.robot.subsystems.arm.Arm
import org.daredevils2512.crescendo.robot.subsystems.arm.capabilities.{
  Limit,
  SimpleActuate
}

package object arm:
  def run(arm: Arm, simpleActuate: SimpleActuate, speed: => Double): Command =
    arm
      .run(() => simpleActuate.run(speed))
      .finallyDo(() => simpleActuate.stop())
  end run

  def runToBottom(
      arm: Arm,
      simpleActuate: SimpleActuate,
      bottomLimit: Limit,
      speed: Double
  ): Command =
    run(arm, simpleActuate, -speed).until(() => bottomLimit.at)
  end runToBottom

  def runToTop(
      arm: Arm,
      simpleActuate: SimpleActuate,
      topLimit: Limit,
      speed: Double
  ): Command =
    run(arm, simpleActuate, speed).until(() => topLimit.at)
  end runToTop
