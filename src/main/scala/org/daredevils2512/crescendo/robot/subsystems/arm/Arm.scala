package org.daredevils2512.crescendo.robot.subsystems.arm

import com.ctre.phoenix.motorcontrol.NeutralMode
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX
import edu.wpi.first.wpilibj2.command.SubsystemBase
import org.daredevils2512.crescendo.robot.subsystems.arm.capabilities.SimpleActuate

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

  private var output = () => ()

  val simpleActuate: SimpleActuate =
    new SimpleActuate {
      override def stop(): Unit =
        output = () => motorGroup.primary.set(0)
      end stop

      override def run(speed: Double): Unit =
        output = () => motorGroup.primary.set(speed)
      end run
    }
  end simpleActuate

  override def periodic(): Unit =
    output()
  end periodic
