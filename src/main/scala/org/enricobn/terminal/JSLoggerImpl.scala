package org.enricobn.terminal

import org.scalajs.dom

import scala.scalajs.js.annotation.{JSExport, JSExportAll, JSExportTopLevel}

/**
  * Created by enrico on 11/30/16.
  */
@JSExportTopLevel(name = "LogLevel")
@JSExportAll
object LogLevel extends Enumeration {
  val ERROR, WARN, INFO, DEBUG = Value
}

@JSExportTopLevel(name = "JSLoggerImpl")
@JSExportAll
final class JSLoggerImpl(val level: LogLevel.Value = LogLevel.WARN) extends JSLogger {

  override def isLoggable(level: LogLevel.Value): Boolean = this.level >= level

  override def log(text: String, level: LogLevel.Value): Unit = {
    if (isLoggable(level)) {
      if (level == LogLevel.ERROR)
        dom.console.error(text)
      else if (level == LogLevel.WARN)
        dom.console.warn(text)
      else if (level == LogLevel.INFO)
        dom.console.info(text)
      else if (level == LogLevel.DEBUG)
        dom.console.log(text)
    }
  }
}
