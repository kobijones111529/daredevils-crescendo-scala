package org.daredevils2512.crescendo.robot.commands

import edu.wpi.first.wpilibj2.command.Command
import org.daredevils2512.crescendo.robot.subsystems.extake.Extake
import org.daredevils2512.crescendo.robot.subsystems.extake.capabilities.SimpleFeed

package object extake:
  def run(extake: Extake, simpleFeed: SimpleFeed, speed: => Double): Command =
    extake
      .run(() => simpleFeed.run(speed))
      .finallyDo(() => simpleFeed.stop())
  end run
