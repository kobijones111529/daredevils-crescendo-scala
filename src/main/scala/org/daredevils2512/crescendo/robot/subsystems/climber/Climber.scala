package org.daredevils2512.crescendo.robot.subsystems.climber

import com.ctre.phoenix.motorcontrol.can.TalonSRX
import com.ctre.phoenix.motorcontrol.{ControlMode, NeutralMode}
import edu.wpi.first.networktables.NetworkTable
import edu.wpi.first.wpilibj2.command.SubsystemBase
import org.daredevils2512.crescendo.robot.subsystems.climber.capabilities.SimpleClimber

class Climber(config: Config, networkTable: NetworkTable) extends SubsystemBase:
  object networkTables:
    object motors:
      object left:
        val appliedOutput =
          networkTable.getDoubleTopic("Applied output (left)").publish()
        val statorCurrent =
          networkTable.getDoubleTopic("Stator current (left)").publish()
      object right:
        val appliedOutput =
          networkTable.getDoubleTopic("Applied output (right)").publish()
        val statorCurrent =
          networkTable.getDoubleTopic("Stator current (right)").publish()
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

  object drive:
    val left: MotorGroup = MotorGroup(config.left)
    val right: MotorGroup = MotorGroup(config.right)

  val simpleClimber: SimpleClimber =
    new SimpleClimber {
      override def stop(): Unit =
        drive.left.primary.set(ControlMode.Disabled, 0)
        drive.right.primary.set(ControlMode.Disabled, 0)
      end stop

      override def run(left: Double, right: Double): Unit =
        drive.left.primary.set(ControlMode.PercentOutput, left)
        drive.right.primary.set(ControlMode.PercentOutput, right)
      end run
    }
  end simpleClimber

  override def periodic(): Unit =
    locally {
      val motor = drive.left.primary
      val group = networkTables.motors.left
      group.appliedOutput.set(motor.getMotorOutputPercent())
      group.statorCurrent.set(motor.getStatorCurrent())
    }

    locally {
      val motor = drive.right.primary
      val group = networkTables.motors.right
      group.appliedOutput.set(motor.getMotorOutputPercent())
      group.statorCurrent.set(motor.getStatorCurrent())
    }
  end periodic
