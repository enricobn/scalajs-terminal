package org.enricobn.terminal

object TerminalOperations {

  def moveCursorLeft(terminal: Terminal, chars: Int) {
    terminal.add(Terminal.ESC + "[" + chars + "D")
  }

  def moveCursorRight(terminal: Terminal, chars: Int) {
    terminal.add(Terminal.ESC + "[" + chars + "C")
  }

  def eraseFromCursor(terminal: Terminal) {
    terminal.add(Terminal.ESC + "[K")
  }

}
