package org.daredevils2512.crescendo.robot.subsystems.drivetrain

import algebra.instances.all.given
import com.ctre.phoenix6.hardware.Pigeon2
import com.revrobotics.CANSparkLowLevel.MotorType
import com.revrobotics.CANSparkMax
import coulomb.*
import coulomb.policy.standard.given
import coulomb.syntax.*
import coulomb.units.accepted.Degree
import coulomb.units.constants.*
import coulomb.units.si.*
import edu.wpi.first.math.filter.SlewRateLimiter
import edu.wpi.first.networktables.{
  DoublePublisher,
  NetworkTable,
  NetworkTableInstance
}
import edu.wpi.first.wpilibj.drive.DifferentialDrive
import edu.wpi.first.wpilibj2.command.SubsystemBase
import org.daredevils2512.crescendo.robot.subsystems.drivetrain.capabilities.{EncoderDistance, EncoderDistanceRaw, EncoderVelocityRaw, Gyro, SimpleDrive}

import scala.language.implicitConversions
import scala.math.Numeric.*

class Drivetrain(config: Config, networkTable: NetworkTable)
    extends SubsystemBase:
  private case class DriveGroup(
      primary: CANSparkMax,
      backups: Array[CANSparkMax],
      rateLimiter: Option[SlewRateLimiter]
  )
  private object DriveGroup:
    def apply(config: Config.Drive.Group): DriveGroup =
      val primary =
        val motor = CANSparkMax(config.primary.id, MotorType.kBrushless)

        motor.restoreFactoryDefaults()

        motor.setInverted(config.primary.inverted)

        motor
      end primary

      DriveGroup(
        primary = primary,
        backups = config.backups.map(id =>
          val motor = CANSparkMax(id, MotorType.kBrushless)

          motor.restoreFactoryDefaults()

          motor.follow(primary)

          motor
        ),
        rateLimiter = config.rateLimit.map(limit => SlewRateLimiter(limit))
      )
    end apply
  end DriveGroup

  private object networkTables:
    private val table: NetworkTable = networkTable
    object publishers:
      object motors:
        private val table = networkTables.table.getSubTable("Motors")

        case class Motor(
            appliedOutput: DoublePublisher,
            outputCurrent: DoublePublisher,
            temperature: DoublePublisher
        )
        object Motor:
          def apply(isLeft: Boolean, isPrimary: Boolean, id: Int): Motor =
            val side = if isLeft then "left" else "right"
            val primary = if isPrimary then "primary" else "backup"
            val specifier = s"$side | $primary | $id"
            Motor(
              appliedOutput =
                table.getDoubleTopic(s"Applied output ($specifier)").publish(),
              outputCurrent =
                table.getDoubleTopic(s"Output current ($specifier)").publish(),
              temperature =
                table.getDoubleTopic(s"Temperature ($specifier)").publish()
            )
          end apply
        end Motor

        val leftOutput = table.getDoubleTopic("Output (left)").publish()
        val rightOutput = table.getDoubleTopic("Output (right)").publish()
        val leftPrimary = Motor(
          isLeft = true,
          isPrimary = true,
          id = drive.left.primary.getDeviceId()
        )
        val leftBackups = drive.left.backups.map(motor =>
          (
            motor,
            Motor(
              isLeft = true,
              isPrimary = false,
              id = motor.getDeviceId()
            )
          )
        )
        val rightPrimary = Motor(
          isLeft = false,
          isPrimary = true,
          id = drive.right.primary.getDeviceId()
        )
        end rightPrimary
        val rightBackups = drive.right.backups.map(motor =>
          (
            motor,
            Motor(
              isLeft = false,
              isPrimary = false,
              id = motor.getDeviceId()
            )
          )
        )
      object gyro:
        val angle: DoublePublisher =
          table.getDoubleTopic("Gyro angle (deg)").publish()
        val rate: DoublePublisher =
          table.getDoubleTopic("Gyro rate (deg/s)").publish()
    end publishers
  end networkTables

  private object drive:
    val left: DriveGroup = DriveGroup(config.drive.left)
    val right: DriveGroup = DriveGroup(config.drive.right)
  end drive

  private var driveOutput: () => Unit = () => ()

  private val pigeon: Option[Pigeon2] =
    for {
      config <- config.pigeon
    } yield
      Pigeon2(config.id)
    end for

  val simpleDrive: Option[SimpleDrive] =
    Some(new SimpleDrive {
      override def stop(): Unit =
        driveOutput = () => {
          drive.left.primary.stopMotor()
          drive.right.primary.stopMotor()

          networkTables.publishers.motors.leftOutput.set(0)
          networkTables.publishers.motors.rightOutput.set(0)
        }
      end stop

      override def tankDrive(left: Double, right: Double): Unit =
        driveOutput = () => {
          val leftRateLimited = drive.left.rateLimiter match
            case Some(rateLimiter) => rateLimiter.calculate(left)
            case None              => left
          val rightRateLimited = drive.right.rateLimiter match
            case Some(rateLimiter) => rateLimiter.calculate(right)
            case None              => right

          drive.left.primary.set(leftRateLimited)
          drive.right.primary.set(rightRateLimited)

          networkTables.publishers.motors.leftOutput.set(leftRateLimited)
          networkTables.publishers.motors.rightOutput.set(rightRateLimited)
        }
      end tankDrive

      override def arcadeDrive(move: Double, turn: Double): Unit =
        driveOutput = () => {
          val wheelSpeeds = DifferentialDrive.arcadeDriveIK(move, -turn, false)

          val leftRateLimited = drive.left.rateLimiter match
            case Some(rateLimiter) => rateLimiter.calculate(wheelSpeeds.left)
            case None              => wheelSpeeds.left
          val rightRateLimited = drive.right.rateLimiter match
            case Some(rateLimiter) => rateLimiter.calculate(wheelSpeeds.right)
            case None              => wheelSpeeds.right

          drive.left.primary.set(leftRateLimited)
          drive.right.primary.set(rightRateLimited)

          networkTables.publishers.motors.leftOutput.set(leftRateLimited)
          networkTables.publishers.motors.rightOutput.set(rightRateLimited)
        }
      end arcadeDrive
    })
  end simpleDrive

  val encoderDistanceRaw: Option[EncoderDistanceRaw[Double]] =
    Some(new EncoderDistanceRaw[Double] {
      override def left: Double = drive.left.primary.getEncoder.getPosition
      override def right: Double = drive.right.primary.getEncoder.getPosition
    })
  end encoderDistanceRaw

  val encoderDistance: Option[EncoderDistance] =
    for {
      leftEncoder <- config.drive.left.encoder
      rightEncoder <- config.drive.right.encoder
      leftDistancePerUnit <- leftEncoder.distancePerUnit
      rightDistancePerUnit <- rightEncoder.distancePerUnit
    } yield new EncoderDistance {
      override def left: Quantity[Double, Meter] =
        drive.left.primary
          .getEncoder()
          .getPosition
          .withUnit[1] * leftDistancePerUnit
      end left

      override def right: Quantity[Double, Meter] =
        drive.right.primary
          .getEncoder()
          .getPosition
          .withUnit[1] * rightDistancePerUnit
      end right
    }
  end encoderDistance

  val encoderVelocityRaw: Option[EncoderVelocityRaw[Double]] =
    Some(new EncoderVelocityRaw {
      override def left: Double = drive.left.primary.getEncoder.getVelocity
      override def right: Double = drive.right.primary.getEncoder.getVelocity
    })
  end encoderVelocityRaw

  val gyro: Option[Gyro] = pigeon.map(pigeon =>
    new Gyro {
      override def angle(): Quantity[Double, Degree] =
        pigeon.getAngle().withUnit[Degree]
      end angle
    }
  )

  override def periodic(): Unit =
    driveOutput()

    logPeriodic()
  end periodic

  private def logPeriodic(): Unit =
    // Left
    locally {
      val (motor, publisher) = (
        drive.left.primary,
        networkTables.publishers.motors.leftPrimary
      )
      publisher.appliedOutput.set(motor.getAppliedOutput())
      publisher.outputCurrent.set(motor.getOutputCurrent())
      publisher.temperature.set(motor.getMotorTemperature())
    }
    networkTables.publishers.motors.leftBackups.foreach((motor, publisher) =>
      publisher.appliedOutput.set(motor.getAppliedOutput())
      publisher.outputCurrent.set(motor.getOutputCurrent())
      publisher.temperature.set(motor.getMotorTemperature())
    )

    // Right
    locally {
      val (motor, publisher) = (
        drive.right.primary,
        networkTables.publishers.motors.rightPrimary
      )
      publisher.appliedOutput.set(motor.getAppliedOutput())
      publisher.outputCurrent.set(motor.getOutputCurrent())
      publisher.temperature.set(motor.getMotorTemperature())
    }
    networkTables.publishers.motors.rightBackups.foreach((motor, publisher) =>
      publisher.appliedOutput.set(motor.getAppliedOutput())
      publisher.outputCurrent.set(motor.getOutputCurrent())
      publisher.temperature.set(motor.getMotorTemperature())
    )

    gyro.map(gyro =>
      networkTables.publishers.gyro.angle.set(???)
      networkTables.publishers.gyro.rate.set(???)
    )
  end logPeriodic

end Drivetrain
