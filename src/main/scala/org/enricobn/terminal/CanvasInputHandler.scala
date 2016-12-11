package org.enricobn.terminal

import org.scalajs.dom
import org.scalajs.dom.html._
import org.scalajs.dom.raw.HTMLElement
import scala.scalajs.js.annotation.{JSExport, JSExportAll}

/**
  * Created by enrico on 12/6/16.
  * @param canvasId the id of the HTML canvas
  */
@JSExportAll
@JSExport(name = "CanvasInputHandler")
class CanvasInputHandler(val canvasId: String) {
  private val canvas: HTMLElement = dom.document.getElementById(canvasId).asInstanceOf[Canvas]
  private val keyDownPub = new KeyDownPub //.Publisher[] {}//new ListBuffer[Int =>()]
  private val keyPressPub = new KeyPressPub //new ListBuffer[Char =>()]
  /*
   * onkeydown is called before onkeypress, and is called even on non char keys (ALT, CTRL ESC etc.).
   * So I check for non char keys onkeydown and char keys onkeypress
   */
  canvas.onkeydown = { (e: dom.KeyboardEvent) =>
    e.stopPropagation()
    val event = KeyDownEvent(e.keyCode, ctrlKey = e.ctrlKey, shiftKey = e.shiftKey)
    keyDownPub.publish(event)
    if (event.preventDefault) {
      e.preventDefault()
    }
  }

  canvas.onkeypress = { (e: dom.KeyboardEvent) =>
    e.preventDefault()
    e.stopPropagation()
    val char = e.key(0)
    keyPressPub.publish(char)
  }

  // to let the canvas has focus
  canvas.tabIndex = 1000

  def onKeyDown(subscriber: KeyDownPub#Sub) {
    keyDownPub.subscribe(subscriber)
  }

  def onKeyPress(subscriber: KeyPressPub#Sub) {
    keyPressPub.subscribe(subscriber)
  }

  def removeKeyDown(subscriber: KeyDownPub#Sub) {
    keyDownPub.removeSubscription(subscriber)
  }

  def removeKeyPress(subscriber: KeyPressPub#Sub) {
    keyPressPub.removeSubscription(subscriber)
  }

}
