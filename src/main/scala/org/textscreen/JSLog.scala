package org.textscreen

import org.scalajs.dom

/**
  * Created by enrico on 11/30/16.
  */
trait JSLog {
  object Levels extends Enumeration {
    val ERROR, WARN, INFO, DEBUG = Value
  }

  // TODO where can I put it?
  val log_level = Levels.WARN

  def is_log(level: Levels.Value) {
    log_level >= level
  }

  def log(text: String, level: Levels.Value) {
    if (log_level >= level) {
      if (level == Levels.ERROR)
        dom.console.error(text)
      else if (level == Levels.WARN)
        dom.console.warn(text)
      if (level == Levels.INFO)
        dom.console.info(text)
      if (level == Levels.DEBUG)
        dom.console.log(text)
    }
  }
}
