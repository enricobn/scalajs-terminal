package org.enricobn.terminal

import scala.scalajs.js.annotation.{JSExport, JSExportAll}

/**
  * Created by enrico on 12/11/16.
  */
@JSExportAll
@JSExport(name = "TerminalColors")
class TerminalColors {
  private val sb: StringBuilder = new StringBuilder
  private val stack = new scala.collection.mutable.Stack[String]
  private var _length: Int = 0

  def length() : Int = _length

  def m(value: Int): TerminalColors = {
    stack.push("\u001B[" + value + "m")
    sb.append(stack.head)
    this
  }

  def m(value: Int, s: String): TerminalColors = {
    m(value).add(s).end()
  }

  def bold(): TerminalColors = {
    m(1)
  }

  def bold(s: String): TerminalColors = {
    m(1, s)
  }

  def green(): TerminalColors = {
    m(32)
  }

  def green(s: String): TerminalColors = {
    m(32, s)
  }

  def yellow(): TerminalColors = {
    m(33)
  }

  def yellow(s: String): TerminalColors = {
    m(33, s)
  }

  def blue(): TerminalColors = {
    m(34)
  }

  def blue(s: String): TerminalColors = {
    m(34, s)
  }

  def end(): TerminalColors = {
    stack.pop
    sb.append("\u001B[0m")
    for (s <- stack) {
      sb.append(s)
    }
    this
  }

  def endAll(): TerminalColors = {
    stack.clear
    sb.append("\u001B[0m")
    this
  }

  def add(s: String): TerminalColors = {
    sb.append(s)
    _length += s.length
    this
  }

  override def toString: String = {
    endAll()
    sb.toString
  }

}
