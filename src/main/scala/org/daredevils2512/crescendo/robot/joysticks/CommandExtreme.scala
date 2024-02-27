package org.daredevils2512.crescendo.robot.joysticks

import edu.wpi.first.wpilibj.Joystick
import edu.wpi.first.wpilibj2.command.button.Trigger

class CommandExtreme(port: Int):
  private object axes:
    val stickX: Int = 0
    val stickY: Int = 1
    val stickZ: Int = 2
    val slider: Int = 3

  private object buttons:
    val trigger: Int = 1
    val side: Int = 2
    val joystickBottomLeft: Int = 3
    val joystickBottomRight: Int = 4
    val joystickTopLeft: Int = 5
    val joystickTopRight: Int = 6
    val baseFrontLeft: Int = 7
    val baseFrontRight: Int = 8
    val baseMiddleLeft: Int = 9
    val baseMiddleRight: Int = 10
    val baseBackLeft: Int = 11
    val baseBackRight: Int = 12

  private val joystick: Joystick = Joystick(port)

  val trigger: Trigger = Trigger(() => joystick.getRawButton(buttons.trigger))
  val sideButton: Trigger = Trigger(() => joystick.getRawButton(buttons.side))
  val joystickBottomLeft: Trigger =
    Trigger(() => joystick.getRawButton(buttons.joystickBottomLeft))
  val joystickBottomRight: Trigger =
    Trigger(() => joystick.getRawButton(buttons.joystickBottomRight))
  val joystickTopLeft: Trigger =
    Trigger(() => joystick.getRawButton(buttons.joystickTopLeft))
  val joystickTopRight: Trigger =
    Trigger(() => joystick.getRawButton(buttons.joystickTopRight))
  val baseFrontLeft: Trigger =
    Trigger(() => joystick.getRawButton(buttons.baseFrontLeft))
  val baseFrontRight: Trigger =
    Trigger(() => joystick.getRawButton(buttons.baseFrontRight))
  val baseMiddleLeft: Trigger =
    Trigger(() => joystick.getRawButton(buttons.baseMiddleLeft))
  val baseMiddleRight: Trigger =
    Trigger(() => joystick.getRawButton(buttons.baseMiddleRight))
  val baseBackLeft: Trigger =
    Trigger(() => joystick.getRawButton(buttons.baseBackLeft))
  val baseBackRight: Trigger =
    Trigger(() => joystick.getRawButton(buttons.baseBackRight))
