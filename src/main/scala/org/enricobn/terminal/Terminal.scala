package org.enricobn.terminal

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.scalajs.js.annotation.{JSExport, JSExportAll}

/**
  * Created by enrico on 11/30/16.
  */

object Terminal {
  def fromCharCode(code: Int) = code.toChar.toString
  val ESC = fromCharCode(27)
}
@JSExport(name = "Terminal")
@JSExportAll
class Terminal(val screen: CanvasTextScreen, inputHandler: CanvasInputHandler, soundResource: String = null) extends JSLog {
  import Terminal._

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
          scroll_back_page_up()
        } else {
          inputPub.publish(ESC + "[5~")
        }
        event.preventDefault = true
        // Page down
      } else if (keyCode == 34) {
        if (event.shiftKey) {
          scroll_back_page_down()
        } else {
          inputPub.publish(ESC + "[6~")
        }
        event.preventDefault = true
        // Canc
      } else if (keyCode == 46) {
        inputPub.publish(ESC + "[3~")
        event.preventDefault = true
      } else if (keyCode == 13) {
        //            if (ter.icrnl) {
        //                inputPub.publish(fromCharCode(10));
        //            } else {
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
      } else if (is_log(Levels.DEBUG)) {
        log("keydown:" + keyCode, Levels.DEBUG)
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

  def onInput(subscriber: StringPub#Sub) {
    inputPub.subscribe(subscriber)
  }

  def removeOnInput(subscriber: StringPub#Sub) {
    inputPub.removeSubscription(subscriber)
  }

  def removeOnInput() {
    inputPub.removeSubscriptions()
  }

  def scroll_back_page_up() {
    screen.scroll_back_page_up()
  }

  def scroll_back_page_down() {
    screen.scroll_back_page_down()
  }

  // add string to cursor position
  def add(text: String) {
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
    if (this.screen.scrolling) {
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
            screen.up(1, false)
          } else {
            log("unhandled escape: " + c, Levels.WARN)
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
              log("unhandled CSI 0J", Levels.WARN)
            } else if (par == "1") {
              log("unhandled CSI 1J", Levels.WARN)
            } else if (par == "2") {
              screen.clear(false)
              log("CSI 2J", Levels.INFO)
            } else if (par == "3") {
              log("unhandled CSI 3J", Levels.WARN)
            }
          } else if (c == 'H') {
            var cursor_x = 0
            var cursor_y = 0
            if (csi_parameters.length > 1) {
              cursor_y = csi_parameters(0).toInt -1
              cursor_x = csi_parameters(1).toInt -1
            }
            screen.set_cursor(cursor_x, cursor_y)
            log("CSI " + cursor_y + ";" + cursor_x + "H", Levels.INFO)
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
              log("CSI " + par + "K", Levels.INFO)
            } else {
              log("unhandled CSI " + par + "K", Levels.WARN)
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
            log("CSI " + par + "C", Levels.INFO)
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
            log("CSI " + par + "D", Levels.INFO)
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
            log("CSI " + par + "G", Levels.INFO)
          } else if (c == 'h' && csi_parameters.nonEmpty) {
            if (csi_parameters(0) == "?" && csi_parameters.length == 2) {
              if (csi_parameters(1) == "1") {
                app_mode = true
                log("CSI ?1h", Levels.INFO)
              } else if (csi_parameters(1) == "7") {
                screen.wrap_around = true
                log("CSI ?7h", Levels.INFO)
              } else if (csi_parameters(1) == "25") {
                screen.show_cursor()
                log("CSI ?25h", Levels.INFO)
              } else {
                if (is_log(Levels.WARN)) {
                  log("unhandled CSI " + csi_parameters + "h", Levels.WARN)
                }
              }
            } else {
              if (is_log(Levels.WARN)) {
                log("unhandled CSI " + csi_parameters + "h", Levels.WARN)
              }
            }
          } else if (c == 'l' && csi_parameters.nonEmpty) {
            if (csi_parameters(0) == "?" && csi_parameters.length == 2) {
              if (csi_parameters(1) == "1") {
                app_mode = false
                log("CSI ?1l", Levels.INFO)
              } else if (csi_parameters(1) == "7") {
                screen.wrap_around = false
                log("CSI ?7l", Levels.INFO)
              } else if (csi_parameters(1) == "25") {
                screen.hide_cursor()
                log("CSI ?25l", Levels.INFO)
              } else {
                if (is_log(Levels.WARN)) {
                  log("unhandled CSI " + csi_parameters + "l", Levels.WARN)
                }
              }
            } else {
              if (is_log(Levels.WARN)) {
                log("unhandled CSI " + csi_parameters + "l", Levels.WARN)
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
            log("CSI " + par + "P", Levels.INFO)
          } else if (c == 'A') {
            val par =
              if (csi_parameters.nonEmpty) {
                csi_parameters(0)
              } else {
                "1"
              }
            screen.up(par.toInt, false)
            log("CSI " + par + "A", Levels.INFO)
          } else if (c == 'r') {
            val par =
            if (csi_parameters.nonEmpty) {
              csi_parameters(0)
            } else {
              "1"
            }
            // Restore DEC Private Mode Values. The value of P s previously saved is restored. P s values are the same as for DECSET.
            if (par == "?") {
              if (is_log(Levels.WARN)) {
                log("unhandled CSI " + csi_parameters + c, Levels.WARN)
              }
              // scroll region
            } else {
              if (csi_parameters.isEmpty) {
                screen.scroll_region = new ScrollRegion(0, screen.height -1)
              } else {
                screen.scroll_region = new ScrollRegion(csi_parameters(0).toInt -1,
                  csi_parameters(1).toInt -1)
              }
              if (is_log(Levels.INFO)) {
                log("CSI " + csi_parameters + c, Levels.INFO)
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
                  screen.set_fg_color("blue") // BLACK TODO restore when handled bg
                } else if (par == "31") {
                  screen.set_fg_color("red")
                } else if (par == "32") {
                  screen.set_fg_color("green")
                } else if (par == "33") {
                  screen.set_fg_color("yellow")
                } else if (par == "34") {
                  screen.set_fg_color("cyan") // BLUE
                } else if (par == "35") {
                  screen.set_fg_color("magenta")
                } else if (par == "36") {
                  screen.set_fg_color("cyan")
                } else if (par == "37") {
                  screen.set_fg_color("white")
                } else if (par == "39") {
                  screen.set_default_fg_color()
                } else if (par == "40") {
                  screen.set_bg_color("blue") // BLACK TODO restore when handled bg
                } else if (par == "41") {
                  screen.set_bg_color("red")
                } else if (par == "42") {
                  screen.set_bg_color("green")
                } else if (par == "43") {
                  screen.set_bg_color("yellow")
                } else if (par == "44") {
                  screen.set_bg_color("cyan") // BLUE
                } else if (par == "45") {
                  screen.set_bg_color("magenta")
                } else if (par == "46") {
                  screen.set_bg_color("cyan")
                } else if (par == "47") {
                  screen.set_bg_color("white")
                } else if (par == "49") {
                  screen.set_default_bg_color()
                } else {
                  log("unhandled CSI: " + par + c, Levels.WARN)
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
            log("CSI " + par + "L", Levels.INFO)
            // delete lines
          } else if (c == 'M') {
            val par =
              if (csi_parameters.nonEmpty) {
                csi_parameters(0)
              } else {
                "1"
              }
            screen.delete_lines(par.toInt)
            log("CSI " + par + "M", Levels.INFO)
            // insert chars
          } else if (c == '@') {
            val par =
              if (csi_parameters.nonEmpty) {
                csi_parameters(0)
              } else {
                "1"
              }
            screen.insert_chars(par.toInt)
            log("CSI " + par + "@", Levels.INFO)
          } else {
            if (is_log(Levels.WARN)) {
              log("unhandled CSI: " + csi_parameters + c, Levels.WARN)
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
            log("unhandled OS command:'" + current + "'", Levels.ERROR)
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
          log("unhandled APP command:'" + current + "'", Levels.ERROR)
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
          log("BELL", Levels.INFO)
          // LINE FEED
        } else if (code == 10) {
          screen.line_feed()
          log("char:LF", Levels.INFO)
          // CARRIAGE RETURN
        } else if (code == 13) {
          //                if (this.icrnl) {
          //                    this.screen.line_feed();
          //                } else {
          screen.carriage_return()
          //                }
          log("char:CR", Levels.INFO)
        } else if (c == '\t') {
          this.screen.tab()
          log("char:TAB", Levels.INFO)
        } else if (code < 32) {
          log("unknown char, code:" + code, Levels.INFO)
        } else {
          this.screen.add_char(c)
          // log('char:"' + c + '"', DEBUG);
        }
      }
    }
    screen.flush()
  }

  def flush() {
    screen.update()
  }

}
