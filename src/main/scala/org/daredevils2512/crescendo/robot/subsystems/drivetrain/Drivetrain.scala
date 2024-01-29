package org.daredevils2512.crescendo.robot.subsystems.drivetrain

import algebra.instances.all.given
import com.revrobotics.CANSparkLowLevel.MotorType
import com.revrobotics.CANSparkMax
import coulomb.*
import coulomb.policy.standard.given
import coulomb.syntax.*
import coulomb.units.constants.*
import coulomb.units.si.*
import edu.wpi.first.math.filter.SlewRateLimiter
import edu.wpi.first.wpilibj.drive.DifferentialDrive
import edu.wpi.first.wpilibj2.command.SubsystemBase
import org.daredevils2512.crescendo.robot.subsystems.drivetrain.capabilities.{
  EncoderDistance,
  EncoderDistanceRaw,
  EncoderVelocityRaw,
  SimpleDrive
}

import scala.language.implicitConversions
import scala.math.Numeric.*

class Drivetrain(config: Config) extends SubsystemBase:
  private case class DriveGroup(
      primary: CANSparkMax,
      backups: Array[CANSparkMax],
      rateLimiter: Option[SlewRateLimiter]
  )
  private object DriveGroup:
    def apply(config: Config.Drive.Group): DriveGroup =
      val primary =
        val motor = CANSparkMax(config.primary, MotorType.kBrushless)

        motor.restoreFactoryDefaults()

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
  end DriveGroup

  private object drive:
    val left: DriveGroup = DriveGroup(config.drive.left)
    val right: DriveGroup = DriveGroup(config.drive.right)

  private var driveOutput: () => Unit = () => ()

  val simpleDrive: Option[SimpleDrive] =
    Some(new SimpleDrive {
      override def stop(): Unit =
        driveOutput = () => {
          drive.left.primary.stopMotor()
          drive.right.primary.stopMotor()
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
        }
      end tankDrive

      override def arcadeDrive(move: Double, turn: Double): Unit =
        driveOutput = () => {
          val wheelSpeeds = DifferentialDrive.arcadeDriveIK(move, turn, false)

          val leftRateLimited = drive.left.rateLimiter match
            case Some(rateLimiter) => rateLimiter.calculate(wheelSpeeds.left)
            case None              => wheelSpeeds.left
          val rightRateLimited = drive.right.rateLimiter match
            case Some(rateLimiter) => rateLimiter.calculate(wheelSpeeds.right)
            case None              => wheelSpeeds.right

          drive.left.primary.set(leftRateLimited)
          drive.right.primary.set(rightRateLimited)
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

  override def periodic(): Unit =
    driveOutput()

end Drivetrain
