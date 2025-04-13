package org.enricobn.terminal

object Terminal {

  def fromCharCode(code: Int): String = code.toChar.toString

  val TAB_CODE = 9
  val BACKSPACE_CODE = 8
  val ESC_CODE = 27
  val CR_CODE = 13
  private val LF_CODE = 10
  val ESC: String = fromCharCode(ESC_CODE)
  val TAB: String = fromCharCode(TAB_CODE)
  val BACKSPACE: String = fromCharCode(BACKSPACE_CODE)
  val CR: String = fromCharCode(CR_CODE)
  val LF: String = fromCharCode(LF_CODE)
  val CRLF: String = CR + LF

}

/**
  * Created by enrico on 12/13/16.
  */
trait Terminal {

  def onInput(subscriber: String => Unit): Unit

  def removeOnInput(subscriber: String => Unit): Unit

  def removeOnInputs(): Unit

  /**
    * Add text to cursor position.
    */
  def add(text: String): Unit

  def flush(): Unit

}
