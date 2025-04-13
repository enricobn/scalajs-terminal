package org.enricobn.terminal

import org.scalajs.dom
import org.scalajs.dom.html.*

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

/**
  * Created by enrico on 12/6/16.
  *
  * @param canvasId the id of the HTML canvas
  */
@JSExportAll
@JSExportTopLevel(name = "CanvasInputHandler")
class CanvasInputHandler(val canvasId: String) extends InputHandler {
  private val canvas: dom.HTMLElement = dom.document.getElementById(canvasId).asInstanceOf[Canvas]
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
    // TODO IntelliJ shows an error, but not if I compile nor in build.
    val char = e.key(0)
    keyPressPub.publish(char)
  }

  // to let the canvas has focus
  canvas.tabIndex = 1000

  override def onKeyDown(subscriber: KeyDownEvent => Unit): Unit = {
    keyDownPub.subscribe(subscriber)
  }

  override def onKeyPress(subscriber: Char => Unit): Unit = {
    keyPressPub.subscribe(subscriber)
  }

  override def removeKeyDown(subscriber: KeyDownEvent => Unit): Unit = {
    keyDownPub.remove(subscriber)
  }

  override def removeKeyPress(subscriber: Char => Unit): Unit = {
    keyPressPub.remove(subscriber)
  }

}
