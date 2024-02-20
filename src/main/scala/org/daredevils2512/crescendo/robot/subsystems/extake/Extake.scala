package org.daredevils2512.crescendo.robot.subsystems.extake

import com.ctre.phoenix.motorcontrol.NeutralMode
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX
import edu.wpi.first.networktables.{DoublePublisher, NetworkTable}
import edu.wpi.first.wpilibj2.command.SubsystemBase
import org.daredevils2512.crescendo.robot.subsystems.extake.capabilities.{
  SimpleFeed
}

class Extake(config: Config, networkTable: NetworkTable) extends SubsystemBase:
  private object networkTables:
    private val table: NetworkTable = networkTable
    object motors:
      private val table: NetworkTable = networkTable.getSubTable("Motors")

      case class Motor(
          appliedOutput: DoublePublisher,
          outputCurrent: DoublePublisher,
          temperature: DoublePublisher
      )
      object Motor:
        def apply(part: String, id: Int): Motor =
          Motor(
            appliedOutput = table
              .getDoubleTopic(s"Applied output ($part | primary | $id)")
              .publish(),
            outputCurrent = table
              .getDoubleTopic(s"Output current ($part | primary | $id)")
              .publish(),
            temperature = table
              .getDoubleTopic(s"Temperature ($part | primary | $id)")
              .publish()
          )
        end apply

      val primary: Motor =
        Motor("feed", config.motorGroup.primary.id)

  case class MotorGroup(primary: WPI_TalonSRX)
  object MotorGroup:
    def apply(config: Config.MotorGroup): MotorGroup =
      val primary = WPI_TalonSRX(config.primary.id)

      primary.configFactoryDefault()
      primary.setInverted(false)
      primary.setNeutralMode(NeutralMode.Brake)

      MotorGroup(primary)
    end apply
  end MotorGroup

  private val motorGroup: MotorGroup = MotorGroup(config.motorGroup)

  private var output = () => ()

  val simpleFeed: SimpleFeed = new SimpleFeed {
    override def stop(): Unit =
      output = () => {
        motorGroup.primary.disable()
      }

    override def run(speed: Double): Unit =
      output = () => {
        motorGroup.primary.set(speed)
      }
    end run
  }

  override def periodic(): Unit =
    output()

    locally {
      val motor = networkTables.motors.primary
      motor.appliedOutput.set(motorGroup.primary.getMotorOutputPercent())
      motor.outputCurrent.set(motorGroup.primary.getStatorCurrent())
      motor.temperature.set(motorGroup.primary.getTemperature())
    }
  end periodic
