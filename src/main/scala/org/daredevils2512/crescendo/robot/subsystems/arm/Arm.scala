package org.daredevils2512.crescendo.robot.subsystems.arm

import com.ctre.phoenix.motorcontrol.NeutralMode
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX
import edu.wpi.first.wpilibj.DigitalInput
import edu.wpi.first.wpilibj2.command.SubsystemBase
import org.daredevils2512.crescendo.robot.subsystems.arm.capabilities.{
  Limit,
  SimpleActuate
}

import scala.math.*

class Arm(config: Config) extends SubsystemBase:
  case class MotorGroup(primary: WPI_TalonSRX)
  object MotorGroup:
    def apply(config: Config.MotorGroup): MotorGroup =
      val primary = WPI_TalonSRX(config.primary)

      primary.configFactoryDefault()
      primary.setInverted(config.inverted)
      primary.setNeutralMode(NeutralMode.Coast)

      MotorGroup(primary)
    end apply

  private val motorGroup: MotorGroup = MotorGroup(config.motorGroup)
  private object limitSwitches:
    val bottom: Option[DigitalInput] =
      config.limitSwitches.bottom.map(DigitalInput(_))
    val top: Option[DigitalInput] =
      config.limitSwitches.top.map(DigitalInput(_))

  private var output: () => Double = () => 0

  val simpleActuate: SimpleActuate =
    new SimpleActuate {
      override def stop(): Unit =
        output = () => 0
      end stop

      override def run(speed: Double): Unit =
        output = () => speed
      end run
    }
  end simpleActuate

  val bottomLimit: Option[Limit] =
    for { bottom <- limitSwitches.bottom } yield new Limit {
      override def at: Boolean = bottom.get()
    }
  end bottomLimit

  val topLimit: Option[Limit] =
    for { top <- limitSwitches.top } yield new Limit {
      override def at: Boolean = top.get()
    }
  end topLimit

  override def periodic(): Unit =
    applyOutput(output())
  end periodic

  private def applyOutput(speed: Double): Unit =
    var speed = output()
    for { bottom <- limitSwitches.bottom } yield
      if bottom.get() then speed = max(0, speed)
    end for
    for { top <- limitSwitches.top } yield
      if top.get() then speed = min(speed, 0)
    end for
    motorGroup.primary.set(speed)
