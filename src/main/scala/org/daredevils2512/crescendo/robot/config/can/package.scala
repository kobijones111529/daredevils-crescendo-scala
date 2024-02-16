package org.daredevils2512.crescendo.robot.config

package object can:
  object drivetrain:
    object left:
      val primary: Int = 0
      val backups: Array[Int] = Array(1)
    object right:
      val primary: Int = 2
      val backups: Array[Int] = Array(3)
    val pigeon: Int = 0
  object extake:
    object actuator:
      val primary: Int = 10
    object feed:
      val primary: Int = 11
