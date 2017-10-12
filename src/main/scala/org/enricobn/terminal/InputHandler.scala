package org.enricobn.terminal

trait InputHandler {

  def onKeyDown(subscriber: KeyDownPub#Sub)

  def onKeyPress(subscriber: KeyPressPub#Sub)

  def removeKeyDown(subscriber: KeyDownPub#Sub)

  def removeKeyPress(subscriber: KeyPressPub#Sub)
}
