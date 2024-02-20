package org.daredevils2512.crescendo.robot.commands

import edu.wpi.first.wpilibj2.command.Command
import org.daredevils2512.crescendo.robot.subsystems.arm.Arm
import org.daredevils2512.crescendo.robot.subsystems.arm.capabilities.SimpleActuate

package object arm:
  def run(arm: Arm, simpleActuate: SimpleActuate, speed: => Double): Command =
    arm
      .run(() => simpleActuate.run(speed))
      .finallyDo(() => simpleActuate.stop())
  end run
