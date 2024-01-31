package org.daredevils2512.crescendo.robot

import edu.wpi.first.wpilibj.TimedRobot
import edu.wpi.first.wpilibj2.command.CommandScheduler

object Instance extends TimedRobot:
  private var container: Option[Container] = None

  override def robotInit(): Unit =
    container = Some(Container())

  override def robotPeriodic(): Unit =
    container.foreach(container => container.periodic())

    CommandScheduler.getInstance().run()
  end robotPeriodic

  override def disabledInit(): Unit = ()
  override def disabledPeriodic(): Unit = ()

  override def autonomousInit(): Unit =
    for {
      robot <- container
      auto <- robot.auto
    } yield auto.schedule()

  override def teleopInit(): Unit =
    for {
      robot <- container
      auto <- robot.auto
    } yield auto.cancel()

  override def autonomousPeriodic(): Unit = ()

  override def testInit(): Unit =
    CommandScheduler.getInstance().cancelAll()

  override def testPeriodic(): Unit = ()
  override def simulationInit(): Unit = ()
  override def simulationPeriodic(): Unit = ()

end Instance
