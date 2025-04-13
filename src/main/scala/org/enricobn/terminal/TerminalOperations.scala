package org.enricobn.terminal

object TerminalOperations {

  def moveCursorLeft(terminal: Terminal, chars: Int): Unit = {
    terminal.add(Terminal.ESC + "[" + chars + "D")
  }

  def moveCursorRight(terminal: Terminal, chars: Int): Unit = {
    terminal.add(Terminal.ESC + "[" + chars + "C")
  }

  def eraseFromCursor(terminal: Terminal): Unit = {
    terminal.add(Terminal.ESC + "[K")
  }

}
