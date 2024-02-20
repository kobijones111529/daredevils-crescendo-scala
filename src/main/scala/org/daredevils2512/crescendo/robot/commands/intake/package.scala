package org.daredevils2512.crescendo.robot.commands

import edu.wpi.first.wpilibj2.command.Command
import org.daredevils2512.crescendo.robot.subsystems.intake.Intake
import org.daredevils2512.crescendo.robot.subsystems.intake.capabilities.SimpleIntake

package object intake:
  def run(
      intake: Intake,
      simpleIntake: SimpleIntake,
      speed: => Double
  ): Command =
    intake
      .run(() => simpleIntake.run(speed))
      .finallyDo(() => simpleIntake.run(0))
  end run
