package org.enricobn.terminal

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.scalajs.js.annotation.{JSExport, JSExportAll}
import Terminal._

/**
  * Created by enrico on 11/30/16.
  */

@JSExport(name = "Terminal")
@JSExportAll
class TerminalImpl(val screen: TextScreen, val inputHandler: InputHandler, val logger: JSLogger, val soundResource: String = null,
                   val termColors: TermColors = new TermColors) extends Terminal {

  private val inputPub = new StringPub
  private var state = 0
  private var current = ""
  private var csi_parameters = new ArrayBuffer[String]()
  private var app_mode = false
  private var icrnl = false
  private val soundEffect: Option[SoundEffect] =
    if (soundResource == null)
      None
    else
      Some(new SoundEffect(soundResource))

  screen.setDefaultCellAttributes(new CellAttributes(false, false, termColors.get(ColorEnum.white),
    termColors.get(ColorEnum.black)))

  inputHandler.onKeyDown(new KeyDownPub#Sub() {
    override def notify(pub: mutable.Publisher[KeyDownEvent], event: KeyDownEvent) {
      val keyCode = event.keyCode
      val ctrlKey = event.ctrlKey
      if (keyCode == 9    // tab
        || keyCode == 27  // ESC
        || keyCode == 8   // backspace
      ) {
        inputPub.publish(fromCharCode(keyCode))
        event.preventDefault = true
        // UP
      } else if (keyCode == 38) {
        if (app_mode) {
          inputPub.publish(ESC + "OA")
        } else {
          inputPub.publish(ESC + "[A")
        }
        event.preventDefault = true
        // DOWN
      } else if (keyCode == 40) {
        if (app_mode) {
          inputPub.publish(ESC + "OB")
        } else {
          inputPub.publish(ESC + "[B")
        }
        event.preventDefault = true
        // LEFT
      } else if (keyCode == 37) {
        if (ctrlKey) {
          inputPub.publish(ESC + "[1;5D")
        } else {
          if (app_mode) {
            inputPub.publish(ESC + "OD")
          } else {
            inputPub.publish(ESC + "[D")
          }
        }
        event.preventDefault = true
        // RIGHT
      } else if (keyCode == 39) {
        if (ctrlKey) {
          inputPub.publish(ESC + "[1;5C")
        } else {
          if (app_mode) {
            inputPub.publish(ESC + "OC")
          } else {
            inputPub.publish(ESC  + "[C")
          }
        }
        event.preventDefault = true
        // HOME
      } else if (keyCode == 36) {
        if (app_mode) {
          inputPub.publish(ESC + "OH")
        } else {
          inputPub.publish(ESC + "[H")
        }
        event.preventDefault = true
        // END
      } else if (keyCode == 35) {
        if (app_mode) {
          inputPub.publish(ESC + "OF")
        } else {
          inputPub.publish(ESC + "[F")
        }
        event.preventDefault = true
        // Page up
      } else if (keyCode == 33) {
        if (event.shiftKey) {
          screen.scroll_back_page_up()
        } else {
          inputPub.publish(ESC + "[5~")
        }
        event.preventDefault = true
        // Page down
      } else if (keyCode == 34) {
        if (event.shiftKey) {
          screen.scroll_back_page_down()
        } else {
          inputPub.publish(ESC + "[6~")
        }
        event.preventDefault = true
        // Canc
      } else if (keyCode == 46) {
        inputPub.publish(ESC + "[3~")
        event.preventDefault = true
        // Enter
      } else if (keyCode == 13) {
        //            if (ter.icrnl) {
        //                inputPub.publish(fromCharCode(10));
        //            } else {
        if (soundEffect.isDefined) {
          soundEffect.get.play()
        }
        inputPub.publish(fromCharCode(13))
        //            }
        event.preventDefault = true
        // Ctrl-c Ctrl-C
      } else if (event.ctrlKey && (keyCode == 67 || keyCode == 99)) {
        inputPub.publish(fromCharCode(3))
        event.preventDefault = true
        // Ctrl-d Ctrl-D
      } else if (event.ctrlKey && (keyCode == 68 || keyCode == 100)) {
        inputPub.publish(fromCharCode(4))
        event.preventDefault = true
      } else if (logger.isLoggable(LogLevel.DEBUG)) {
        logger.log("keydown:" + keyCode, LogLevel.DEBUG)
      }
      //
      //
      //
      //
      //      } else if (keyCode == 13) {
      //        inputPub.publish("\n")
      //        event.preventDefault = true
      //      }
    }
  })

  inputHandler.onKeyPress(new KeyPressPub#Sub() {
    override def notify(pub: mutable.Publisher[Char], event: Char) {
      if (soundEffect.isDefined) {
        soundEffect.get.play()
      }
      inputPub.publish(event.toString)
    }
  })

  override def onInput(subscriber: StringPub#Sub) {
    inputPub.subscribe(subscriber)
  }

  override def removeOnInput(subscriber: StringPub#Sub) {
    inputPub.removeSubscription(subscriber)
  }

  override def removeOnInputs() {
    inputPub.removeSubscriptions()
  }

  // add string to cursor position
  override def add(text: String) {
    /*    if (text.substr(0,3) == '&c:') {
            if (text.substr(3) == 'icrnl=true') {
                this.icrnl = true;
            } else {
                this.icrnl = false;
            }
            return;
        } else {
            text = text.substr(3);
        }
    */
    if (this.screen.is_scrolling()) {
      screen.reset_scrolling()
    }

    for (i <- 0 until text.length) {
      val c = text.charAt(i)
      val code = text.charAt(i)
      // ESCAPE
      if (state == 1) {
        if (c == '[') {
          state = 2
        } else if (c == '%') {
          state = 3
        } else if (c == '#') {
          state = 4
        } else if (c == '(') {
          state = 5
        } else if (c == ')') {
          state = 5
        } else if (c == ']') {
          state = 7
        } else if (c == '_') {
          state = 8
        } else {
          if (c == 'D') {
            screen.add_char('\n')
          } else if (c == 'E') {
            screen.add_char('\r')
            // Reverse index (cursor up with scroll down when at margin)
          } else if (c == 'M') {
            // TODO false is for compatibility with wshell, but I don't know if I must take care of the back buffer
            screen.up(1, scroll_back = false)
          } else {
            logger.log("unhandled escape: " + c, LogLevel.WARN)
          }
          state = 0
        }
        // CSI
      } else if (this.state == 2) {
        if (c == '?') {
          csi_parameters += "?"
        } else if (c == ';') {
          csi_parameters += current
          current = ""
        } else if (c >= '0' && c <= '9') {
          current += c
        } else {
          if (current.nonEmpty) {
            csi_parameters += current
          }
          if (c == 'J') {
            val par =
              if (csi_parameters.nonEmpty) {
                csi_parameters(0)
              } else {
                "0"
              }
            if (par == "0") {
              logger.log("unhandled CSI 0J", LogLevel.WARN)
            } else if (par == "1") {
              logger.log("unhandled CSI 1J", LogLevel.WARN)
            } else if (par == "2") {
              screen.clear(false)
              logger.log("CSI 2J", LogLevel.INFO)
            } else if (par == "3") {
              logger.log("unhandled CSI 3J", LogLevel.WARN)
            }
          } else if (c == 'H') {
            var cursor_x = 0
            var cursor_y = 0
            if (csi_parameters.length > 1) {
              cursor_y = csi_parameters(0).toInt -1
              cursor_x = csi_parameters(1).toInt -1
            }
            screen.set_cursor(cursor_x, cursor_y)
            logger.log("CSI " + cursor_y + ";" + cursor_x + "H", LogLevel.INFO)
          } else if (c == 'K') {
            val par =
              if (csi_parameters.nonEmpty) {
                csi_parameters(0)
              } else {
                "0"
              }
            if (par == "0") {
              screen.erase_line_from_cursor()
              screen.set_cursor(screen.cursor.x, screen.cursor.y)
              logger.log("CSI " + par + "K", LogLevel.INFO)
            } else {
              logger.log("unhandled CSI " + par + "K", LogLevel.WARN)
            }
          } else if (c == 'C') {
            val par =
              if (csi_parameters.nonEmpty) {
                csi_parameters(0)
              } else {
                "1"
              }
            // TODO I don't like it, but the cursor position can be different after flush
            screen.flush()
            screen.set_cursor(screen.cursor.x + par.toInt, screen.cursor.y)
            logger.log("CSI " + par + "C", LogLevel.INFO)
            // Cursor Backward
          } else if (c == 'D') {
            val par =
              if (csi_parameters.nonEmpty) {
                csi_parameters(0)
              } else {
                "1"
              }
            // TODO I don't like it, but the cursor position can be different after flush
            screen.flush()
            screen.set_cursor(screen.cursor.x - par.toInt, screen.cursor.y)
            logger.log("CSI " + par + "D", LogLevel.INFO)
            // goto column
          } else if (c == 'G') {
            val par =
              if (csi_parameters.nonEmpty) {
                csi_parameters(0)
              } else {
                "1"
              }
            // TODO I don't like it, but the cursor position can be different after flush
            screen.flush()
            screen.set_cursor(screen.cursor.x, par.toInt -1)
            logger.log("CSI " + par + "G", LogLevel.INFO)
          } else if (c == 'h' && csi_parameters.nonEmpty) {
            if (csi_parameters(0) == "?" && csi_parameters.length == 2) {
              if (csi_parameters(1) == "1") {
                app_mode = true
                logger.log("CSI ?1h", LogLevel.INFO)
              } else if (csi_parameters(1) == "7") {
                screen.wrap_around = true
                logger.log("CSI ?7h", LogLevel.INFO)
              } else if (csi_parameters(1) == "25") {
                screen.show_cursor()
                logger.log("CSI ?25h", LogLevel.INFO)
              } else {
                if (logger.isLoggable(LogLevel.WARN)) {
                  logger.log("unhandled CSI " + csi_parameters + "h", LogLevel.WARN)
                }
              }
            } else {
              if (logger.isLoggable(LogLevel.WARN)) {
                logger.log("unhandled CSI " + csi_parameters + "h", LogLevel.WARN)
              }
            }
          } else if (c == 'l' && csi_parameters.nonEmpty) {
            if (csi_parameters(0) == "?" && csi_parameters.length == 2) {
              if (csi_parameters(1) == "1") {
                app_mode = false
                logger.log("CSI ?1l", LogLevel.INFO)
              } else if (csi_parameters(1) == "7") {
                screen.wrap_around = false
                logger.log("CSI ?7l", LogLevel.INFO)
              } else if (csi_parameters(1) == "25") {
                screen.hide_cursor()
                logger.log("CSI ?25l", LogLevel.INFO)
              } else {
                if (logger.isLoggable(LogLevel.WARN)) {
                  logger.log("unhandled CSI " + csi_parameters + "l", LogLevel.WARN)
                }
              }
            } else {
              if (logger.isLoggable(LogLevel.WARN)) {
                logger.log("unhandled CSI " + csi_parameters + "l", LogLevel.WARN)
              }
            }
          } else if (c == 'P') {
            val par =
              if (csi_parameters.nonEmpty) {
                csi_parameters(0)
              } else {
                "1"
              }
            screen.delete_chars(par.toInt)
            logger.log("CSI " + par + "P", LogLevel.INFO)
          } else if (c == 'A') {
            val par =
              if (csi_parameters.nonEmpty) {
                csi_parameters(0)
              } else {
                "1"
              }
            screen.up(par.toInt, scroll_back = false)
            logger.log("CSI " + par + "A", LogLevel.INFO)
          } else if (c == 'r') {
            val par =
              if (csi_parameters.nonEmpty) {
                csi_parameters(0)
              } else {
                "1"
              }
            // Restore DEC Private Mode Values. The value of P s previously saved is restored. P s values are the same as for DECSET.
            if (par == "?") {
              if (logger.isLoggable(LogLevel.WARN)) {
                logger.log("unhandled CSI " + csi_parameters + c, LogLevel.WARN)
              }
              // scroll region
            } else {
              if (csi_parameters.isEmpty) {
                screen.scroll_region = ScrollRegion(0, screen.height -1)
              } else {
                screen.scroll_region = ScrollRegion(csi_parameters(0).toInt -1,
                  csi_parameters(1).toInt -1)
              }
              if (logger.isLoggable(LogLevel.INFO)) {
                logger.log("CSI " + csi_parameters + c, LogLevel.INFO)
              }
            }
          } else if (c == 'm') {
            if (csi_parameters.nonEmpty) {
              for (par <- csi_parameters) {
                if (par == "0") {
                  screen.set_default_attributes()
                } else if (par == "1" || par == "01") {
                  screen.set_bold()
                } else if (par == "22") {
                  screen.set_normal()
                } else if (par == "30") {
                  screen.set_fg_color(termColors.get(ColorEnum.black))
                } else if (par == "31") {
                  screen.set_fg_color(termColors.get(ColorEnum.red))
                } else if (par == "32") {
                  screen.set_fg_color(termColors.get(ColorEnum.green))
                } else if (par == "33") {
                  screen.set_fg_color(termColors.get(ColorEnum.yellow))
                } else if (par == "34") {
                  screen.set_fg_color(termColors.get(ColorEnum.blue))
                } else if (par == "35") {
                  screen.set_fg_color(termColors.get(ColorEnum.magenta))
                } else if (par == "36") {
                  screen.set_fg_color(termColors.get(ColorEnum.cyan))
                } else if (par == "37") {
                  screen.set_fg_color(termColors.get(ColorEnum.white))
                } else if (par == "39") {
                  screen.set_default_fg_color()
                } else if (par == "40") {
                  screen.set_bg_color(termColors.get(ColorEnum.black))
                } else if (par == "41") {
                  screen.set_bg_color(termColors.get(ColorEnum.red))
                } else if (par == "42") {
                  screen.set_bg_color(termColors.get(ColorEnum.green))
                } else if (par == "43") {
                  screen.set_bg_color(termColors.get(ColorEnum.yellow))
                } else if (par == "44") {
                  screen.set_bg_color(termColors.get(ColorEnum.blue))
                } else if (par == "45") {
                  screen.set_bg_color(termColors.get(ColorEnum.magenta))
                } else if (par == "46") {
                  screen.set_bg_color(termColors.get(ColorEnum.cyan))
                } else if (par == "47") {
                  screen.set_bg_color(termColors.get(ColorEnum.white))
                } else if (par == "49") {
                  screen.set_default_bg_color()
                } else {
                  logger.log("unhandled CSI: " + par + c, LogLevel.WARN)
                }
              }
            } else {
              this.screen.set_default_attributes()
            }
            // insert lines
          } else if (c == 'L') {
            val par =
              if (csi_parameters.nonEmpty) {
                csi_parameters(0)
              } else {
                "1"
              }
            screen.insert_lines(par.toInt)
            logger.log("CSI " + par + "L", LogLevel.INFO)
            // delete lines
          } else if (c == 'M') {
            val par =
              if (csi_parameters.nonEmpty) {
                csi_parameters(0)
              } else {
                "1"
              }
            screen.delete_lines(par.toInt)
            logger.log("CSI " + par + "M", LogLevel.INFO)
            // insert chars
          } else if (c == '@') {
            val par =
              if (csi_parameters.nonEmpty) {
                csi_parameters(0)
              } else {
                "1"
              }
            screen.insert_chars(par.toInt)
            logger.log("CSI " + par + "@", LogLevel.INFO)
          } else {
            if (logger.isLoggable(LogLevel.WARN)) {
              logger.log("unhandled CSI: " + csi_parameters + c, LogLevel.WARN)
            }
          }
          current = ""
          csi_parameters.clear()
          state = 0
        }
        // operating system command
      } else if (this.state == 7) {
        if (c == '\\' || code == 7) {
          // title
          if (current.length > 2 && current.substring(0, 2) == "0;") {

          } else {
            logger.log("unhandled OS command:'" + current + "'", LogLevel.ERROR)
          }
          current = ""
          state = 0
        } else {
          current += c
        }
        // application command
      } else if (state == 8) {
        if (c == '\\' || code == 7) {
          /*
          try {
            var command = JSON.parse(this.current.substr(0, this.current.length-1));
            if (command.op == 'download') {
              $.fileDownload(command.resource, {
                preparingMessageHtml: "We are preparing the download, please wait...",
                failMessageHtml: "There was a problem downloading file " + command.name
              });
            }
          } catch(err) {
            log('unhandled APP command:"' + this.current + '"');
          }
          this.current = '';
          this.state = 0;
          */
          logger.log("unhandled APP command:'" + current + "'", LogLevel.ERROR)
        } else {
          current += c
        }
      } else if (state > 2) {
        // I don't handle them for now
        state = 0
      } else {
        // ESCAPE
        if (code == 27) {
          state = 1
          // BACKSPACE
        } else if (code == 8) {
          screen.backspace()
          // BELL
        } else if (code == 7) {
          logger.log("BELL", LogLevel.INFO)
          // LINE FEED
        } else if (code == 10) {
          screen.line_feed()
          logger.log("char:LF", LogLevel.INFO)
          // CARRIAGE RETURN
        } else if (code == 13) {
          //                if (this.icrnl) {
          //                    this.screen.line_feed();
          //                } else {
          screen.carriage_return()
          //                }
          logger.log("char:CR", LogLevel.INFO)
        } else if (c == '\t') {
          this.screen.tab()
          logger.log("char:TAB", LogLevel.INFO)
        } else if (code < 32) {
          logger.log("unknown char, code:" + code, LogLevel.INFO)
        } else {
          this.screen.add_char(c)
          // log('char:"' + c + '"', DEBUG);
        }
      }
    }
    screen.flush()
  }

  override def flush() {
    screen.update()
  }

}

object ColorEnum extends Enumeration {
  type ColorEnum = Value

  val black: ColorEnum = Value("black")
  val red: ColorEnum = Value("red")
  val green: ColorEnum = Value("green")
  val yellow: ColorEnum = Value("yellow")
  val blue: ColorEnum = Value("blue")
  val magenta: ColorEnum = Value("magenta")
  val cyan: ColorEnum = Value("cyan")
  val white: ColorEnum = Value("white")
}

class TermColors {

  private val colors = mutable.Map[ColorEnum.ColorEnum, String]()

  ColorEnum.values.foreach { color => colors.put(color, color.toString) }

  def get(color: ColorEnum.ColorEnum): String = colors(color)

  def set(color: ColorEnum.ColorEnum, value: String): Unit = colors(color) = value

}
