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
import edu.wpi.first.math.geometry.struct.Pose2dStruct
import edu.wpi.first.math.geometry.{Pose2d, Rotation2d}
import edu.wpi.first.math.kinematics.{
  ChassisSpeeds,
  DifferentialDriveKinematics,
  DifferentialDriveOdometry
}
import edu.wpi.first.networktables.{
  DoublePublisher,
  NetworkTable,
  NetworkTableInstance,
  StructPublisher
}
import edu.wpi.first.wpilibj.drive.DifferentialDrive
import edu.wpi.first.wpilibj2.command.SubsystemBase
import org.daredevils2512.crescendo.robot.subsystems.drivetrain.capabilities.{
  EncoderDistance,
  EncoderDistanceRaw,
  EncoderVelocityRaw,
  Gyro,
  Kinematics,
  Pose,
  SimpleDrive,
  VelocityDrive
}

import java.util.concurrent.Flow.Publisher
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

      val pose: StructPublisher[Pose2d] = networkTables.table
        .getStructTopic[Pose2d]("Pose", Pose2dStruct())
        .publish()
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
    } yield Pigeon2(config.id)
  end pigeon

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

      override def tank(left: Double, right: Double): Unit =
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
      end tank

      override def arcade(move: Double, turn: Double): Unit =
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
      end arcade
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
      leftDistancePerRevolution <- leftEncoder.distancePerRevolution
      rightDistancePerRevolution <- rightEncoder.distancePerRevolution
    } yield new EncoderDistance {
      override def left: Quantity[Double, Meter] =
        drive.left.primary
          .getEncoder()
          .getPosition
          .withUnit[1] * leftDistancePerRevolution
      end left

      override def right: Quantity[Double, Meter] =
        drive.right.primary
          .getEncoder()
          .getPosition
          .withUnit[1] * rightDistancePerRevolution
      end right
    }
  end encoderDistance

  val encoderVelocityRaw: Option[EncoderVelocityRaw[Double]] =
    Some(new EncoderVelocityRaw {
      override def left: Double = drive.left.primary.getEncoder.getVelocity
      override def right: Double = drive.right.primary.getEncoder.getVelocity
    })
  end encoderVelocityRaw

  private val _kinematics: Option[DifferentialDriveKinematics] =
    config.drive.trackWidth.map(trackWidth =>
      DifferentialDriveKinematics(trackWidth.toUnit[Meter].value)
    )

  val kinematics: Option[Kinematics] = _kinematics.map(_kinematics =>
    new Kinematics {
      override def kinematics: DifferentialDriveKinematics = _kinematics
    }
  )

  val velocityDrive: Option[VelocityDrive] =
    for {
      leftFeedforward <- config.drive.left.feedforward
      rightFeedforward <- config.drive.right.feedforward
      kinematics <- kinematics
    } yield new VelocityDrive {
      override def tank(
          left: Quantity[Double, Meter / Second],
          right: Quantity[Double, Meter / Second]
      ): Unit =
        driveOutput = () => {
          val leftOut =
            leftFeedforward.calculate(left.toUnit[Meter / Second].value)
          val rightOut =
            rightFeedforward.calculate(right.toUnit[Meter / Second].value)

          drive.left.primary.set(leftOut)
          drive.right.primary.set(rightOut)

          networkTables.publishers.motors.leftOutput.set(leftOut)
          networkTables.publishers.motors.rightOutput.set(rightOut)
        }

      override def arcade(
          move: Quantity[Double, Meter / Second],
          turn: Quantity[Double, Degree / Second]
      ): Unit =
        val chassisSpeeds = ChassisSpeeds(
          move.toUnit[Meter / Second].value,
          0,
          turn.toUnit[Radian / Second].value
        )
        val wheelSpeeds = kinematics.kinematics.toWheelSpeeds(chassisSpeeds)
        val leftOut = leftFeedforward.calculate(wheelSpeeds.leftMetersPerSecond)
        val rightOut =
          rightFeedforward.calculate(wheelSpeeds.rightMetersPerSecond)

        drive.left.primary.set(leftOut)
        drive.right.primary.set(rightOut)

        networkTables.publishers.motors.leftOutput.set(leftOut)
        networkTables.publishers.motors.rightOutput.set(rightOut)
    }
  end velocityDrive

  val gyro: Option[Gyro] = pigeon.map(pigeon =>
    new Gyro {
      override def angle: Quantity[Double, Degree] =
        pigeon.getAngle().withUnit[Degree]
      override def rate: Quantity[Double, Degree / Second] =
        pigeon.getRate().withUnit[Degree / Second]
    }
  )

  private val odometry: Option[DifferentialDriveOdometry] =
    for {
      encoderDistance <- encoderDistance
      gyro <- gyro
    } yield DifferentialDriveOdometry(
      Rotation2d(gyro.angle.toUnit[Radian].value),
      encoderDistance.left.toUnit[Meter].value,
      encoderDistance.right.toUnit[Meter].value
    )

  val pose: Option[Pose] =
    odometry.map(odometry =>
      new Pose {
        override def pose: Pose2d = odometry.getPoseMeters()
      }
    )
  end pose

  override def periodic(): Unit =
    driveOutput()

    for {
      odometry <- odometry
      encoderDistance <- encoderDistance
      gyro <- gyro
    } yield odometry.update(
      Rotation2d(gyro.angle.toUnit[Radian].value),
      encoderDistance.left.toUnit[Meter].value,
      encoderDistance.right.toUnit[Meter].value
    )

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

    // Gyro
    gyro.map(gyro =>
      networkTables.publishers.gyro.angle.set(
        gyro.angle.toUnit[Degree].value
      )
      networkTables.publishers.gyro.rate.set(
        gyro.rate.toUnit[Degree / Second].value
      )
    )
    pose.map(pose => networkTables.publishers.pose.set(pose.pose))
  end logPeriodic

end Drivetrain
