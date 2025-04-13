package org.enricobn.terminal

trait InputHandler {

  def onKeyDown(listener: KeyDownEvent => Unit): Unit

  def onKeyPress(listener: Char => Unit): Unit

  def removeKeyDown(listener: KeyDownEvent => Unit): Unit

  def removeKeyPress(listener: Char => Unit): Unit
}
