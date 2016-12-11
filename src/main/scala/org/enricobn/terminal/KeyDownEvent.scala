package org.enricobn.terminal

/**
  * Created by enrico on 12/10/16.
  */
case class KeyDownEvent(keyCode: Int, ctrlKey: Boolean, shiftKey: Boolean, var preventDefault: Boolean = false)
