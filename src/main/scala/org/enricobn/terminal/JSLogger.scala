package org.enricobn.terminal

trait JSLogger {

  def isLoggable(level: LogLevel.Value): Boolean

  def log(text: String, level: LogLevel.Value): Unit
}
