package org.enricobn.terminal

object Terminal {

  def fromCharCode(code: Int): String = code.toChar.toString

  val ESC: String = fromCharCode(27)
  val TAB: String = fromCharCode(9)
  val BACKSPACE: String = fromCharCode(8)
  val CR: String = fromCharCode(13)
  val LF: String = fromCharCode(10)
  val CRLF: String = CR + LF

}

/**
  * Created by enrico on 12/13/16.
  */
trait Terminal {

  def onInput(subscriber: _root_.org.enricobn.terminal.StringPub#Sub)

  def removeOnInput(subscriber: _root_.org.enricobn.terminal.StringPub#Sub)

  def removeOnInputs()

  /**
    * Add text to cursor position.
    */
  def add(text: String)

  def flush()

}
