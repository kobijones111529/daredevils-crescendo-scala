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
  object intake:
    val primary: Int = 5
  object arm:
    val primary: Int = 7
  object extake:
    val primary: Int = 6
