package org.daredevils2512.crescendo.robot.subsystems.climber

import com.ctre.phoenix.motorcontrol.can.TalonSRX
import com.ctre.phoenix.motorcontrol.{ControlMode, NeutralMode}
import edu.wpi.first.networktables.NetworkTable
import edu.wpi.first.wpilibj2.command.SubsystemBase
import org.daredevils2512.crescendo.robot.subsystems.climber.capabilities.SimpleClimber

class Climber(config: Config, networkTable: NetworkTable) extends SubsystemBase:
  object networkTables:
    object motors:
      val appliedOutput =
        networkTable.getDoubleTopic("Applied output (left)").publish()
      val statorCurrent =
        networkTable.getDoubleTopic("Stator current (left)").publish()
  end networkTables

  case class MotorGroup(primary: TalonSRX)
  object MotorGroup:
    def apply(config: Config.MotorGroup): MotorGroup =
      val primary = TalonSRX(config.primary.id)

      primary.configFactoryDefault()

      primary.setInverted(config.inverted)

      primary.setNeutralMode(NeutralMode.Brake)

      MotorGroup(primary)
    end apply

  val motorGroup: MotorGroup = MotorGroup(config.motorGroup)

  val simpleClimber: SimpleClimber =
    new SimpleClimber {
      override def stop(): Unit =
        motorGroup.primary.set(ControlMode.Disabled, 0)
      end stop

      override def run(speed: Double): Unit =
        motorGroup.primary.set(ControlMode.PercentOutput, speed)
      end run
    }
  end simpleClimber

  override def periodic(): Unit =
    locally {
      val motor = motorGroup.primary
      networkTables.motors.appliedOutput.set(motor.getMotorOutputPercent())
      networkTables.motors.statorCurrent.set(motor.getStatorCurrent())
    }
  end periodic
