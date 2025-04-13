package org.enricobn.terminal

import scala.collection.mutable.ListBuffer

case class Listeners[T]() {
  private var listeners = ListBuffer[T => Unit]()

  def subscribe(listener: T => Unit) : Unit =
    listeners.addOne(listener)

  def remove(listener: T => Unit) : Unit =
    listeners = listeners.filter(_ != listener)

  def publish(value: T) : Unit =
    listeners.foreach(_.apply(value))

  def removeAll() : Unit =
    listeners.clear()
}
