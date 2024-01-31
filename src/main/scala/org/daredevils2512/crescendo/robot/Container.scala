package org.daredevils2512.crescendo.robot

import com.ctre.phoenix6.hardware.Pigeon2
import edu.wpi.first.networktables.{
  DoublePublisher,
  NetworkTable,
  NetworkTableEntry,
  NetworkTableInstance,
  Topic
}
import edu.wpi.first.wpilibj2.command.Command

class Container:
  object networkTables:
    val table: NetworkTable =
      NetworkTableInstance.getDefault().getTable("Robot container")
    object publishers:
      val pigeonAngle: DoublePublisher =
        table.getDoubleTopic("Pigeon angle (deg)").publish()
    end publishers
  end networkTables

  val pigeon: Option[Pigeon2] = None

  def periodic(): Unit =
    pigeon match
      case None => networkTables.publishers.pigeonAngle.set(0)
      case Some(pigeon) =>
        networkTables.publishers.pigeonAngle.set(pigeon.getAngle())
  end periodic

  def auto: Option[Command] = None
