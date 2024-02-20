package org.daredevils2512.crescendo.robot.subsystems.extake

import com.ctre.phoenix.motorcontrol.NeutralMode
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX
import edu.wpi.first.networktables.{DoublePublisher, NetworkTable}
import edu.wpi.first.wpilibj2.command.SubsystemBase
import org.daredevils2512.crescendo.robot.subsystems.extake.capabilities.{SimpleActuate, SimpleFeed}

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

      val actuatorPrimary: Motor =
        Motor("actuator", config.actuator.motorGroup.primary.id)
      val feedPrimary: Motor = Motor("feed", config.feed.motorGroup.primary.id)

  private case class Actuator(
      motorGroup: Actuator.MotorGroup
  )
  private object Actuator:
    case class MotorGroup(primary: WPI_TalonSRX)
    object MotorGroup:
      def apply(config: Config.Actuator.MotorGroup): MotorGroup =
        val primary = WPI_TalonSRX(config.primary.id)

        primary.configFactoryDefault()
        primary.setInverted(false)
        primary.setNeutralMode(NeutralMode.Brake)

        MotorGroup(primary)
      end apply
    end MotorGroup

    def apply(config: Config.Actuator): Actuator = Actuator(
      motorGroup = MotorGroup(config.motorGroup)
    )
  end Actuator

  private case class Feed(motorGroup: Feed.MotorGroup)
  private object Feed:
    case class MotorGroup(primary: WPI_TalonSRX)
    object MotorGroup:
      def apply(config: Config.Feed.MotorGroup): MotorGroup =
        val primary = WPI_TalonSRX(config.primary.id)

        primary.configFactoryDefault()
        primary.setInverted(false)
        primary.setNeutralMode(NeutralMode.Coast)

        MotorGroup(primary)
      end apply
    end MotorGroup

    def apply(config: Config.Feed): Feed = Feed(
      motorGroup = MotorGroup(config.motorGroup)
    )
  end Feed

  private val actuator: Actuator =
    Actuator(motorGroup = Actuator.MotorGroup(config.actuator.motorGroup))
  private val feed: Feed =
    Feed(motorGroup = Feed.MotorGroup(config.feed.motorGroup))

  private var feedOutput = () => ()
  private var actuateOutput = () => ()

  val simpleFeed: SimpleFeed = new SimpleFeed {
    override def stop(): Unit =
      feedOutput = () => {
        feed.motorGroup.primary.disable()
      }

    override def run(speed: Double): Unit =
      feedOutput = () => {
        feed.motorGroup.primary.set(speed)
      }
    end run
  }

  val simpleActuate: SimpleActuate = new SimpleActuate {
    override def run(speed: Double): Unit =
      actuateOutput = () => {
        actuator.motorGroup.primary.set(speed)
      }
    end run
  }

  override def periodic(): Unit =
    feedOutput()
    actuateOutput()

    locally {
      val motor = networkTables.motors.actuatorPrimary
      motor.appliedOutput.set(
        actuator.motorGroup.primary.getMotorOutputPercent()
      )
      motor.outputCurrent.set(actuator.motorGroup.primary.getStatorCurrent())
      motor.temperature.set(actuator.motorGroup.primary.getTemperature())
    }
    locally {
      val motor = networkTables.motors.feedPrimary
      motor.appliedOutput.set(feed.motorGroup.primary.getMotorOutputPercent())
      motor.outputCurrent.set(feed.motorGroup.primary.getStatorCurrent())
      motor.temperature.set(feed.motorGroup.primary.getTemperature())
    }
  end periodic
