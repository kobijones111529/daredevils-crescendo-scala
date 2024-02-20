package org.daredevils2512.crescendo.robot.subsystems.intake

import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX
import edu.wpi.first.wpilibj2.command.SubsystemBase
import org.daredevils2512.crescendo.robot.subsystems.intake.capabilities.SimpleIntake

class Intake(config: Config) extends SubsystemBase:
  case class MotorGroup(primary: WPI_TalonSRX)
  object MotorGroup:
    def apply(config: Config.MotorGroup): MotorGroup =
      val primary = WPI_TalonSRX(config.primary)

      primary.configFactoryDefault()
      primary.setInverted(config.inverted)

      MotorGroup(primary)
    end apply

  private val motorGroup: MotorGroup = MotorGroup(
    primary = WPI_TalonSRX(config.motorGroup.primary)
  )

  private var output = () => ()

  val simpleIntake: SimpleIntake =
    new SimpleIntake {
      override def run(speed: Double): Unit =
        output = () => motorGroup.primary.set(speed)
    }
  end simpleIntake

  override def periodic(): Unit =
    output()
  end periodic
