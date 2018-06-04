package org.enricobn.terminal

object TerminalOperations {

  def moveLeft(terminal: Terminal, chars: Int) {
    terminal.add(Terminal.ESC + "[" + chars + "D")
  }

  def eraseFromCursor(terminal: Terminal) {
    terminal.add(Terminal.ESC + "[K")
  }

}
