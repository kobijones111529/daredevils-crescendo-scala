package org.daredevils2512.crescendo.robot

import edu.wpi.first.wpilibj.RobotBase
import org.daredevils2512.crescendo.robot.Instance

object Main:
  def main(args: Array[String]): Unit =
    RobotBase.startRobot(() => Instance)
