package org.enricobn.terminal

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

class TerminalColorsSpec extends FlatSpec with MockFactory with Matchers {

  "it" should "work" in {
    val colors = new TerminalColors()
    colors.blue().add("is blue")

    val screen = mock[TextScreen]
    (screen.is_scrolling: () => Boolean).expects().returning(false)
    expectsAdd(screen, "hello ")
    (screen.set_fg_color _).expects("blue")
    expectsAdd(screen, "is blue")
    (screen.set_default_attributes: () => Unit).expects()
    expectsAdd(screen, " world")
    (screen.flush: () => Unit).expects()

    val inputHandler = stub[InputHandler]

    val logger = stub[JSLogger]

    val terminal = new TerminalImpl(screen, inputHandler, logger)

    terminal.add("hello " + colors.toString + " world")
  }

  private def expectsAdd(screen: TextScreen, s: String): Unit = {
    s.foreach(c => (screen.add_char _).expects(c.charValue()))
  }

}
