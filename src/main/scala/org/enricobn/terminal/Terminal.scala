package org.enricobn.terminal

import scala.collection.mutable.ArrayBuffer
import scala.scalajs.js.annotation.{JSExport, JSExportAll}

/**
  * Created by enrico on 11/30/16.
  */
@JSExport(name = "Terminal")
@JSExportAll
class Terminal(val screen: TextScreenCanvas) extends JSLog {
  var state = 0
  var current = ""
  var csi_parameters = new ArrayBuffer[String]()
  var app_mode = false
  var icrnl = false

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
      var c = text.charAt(i)
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
/*
      } else if (this.state == 2) {
        if (c == '?') {
          this.csi_parameters.push('?');
        } else if (c == ';') {
          this.csi_parameters.push(this.current);
          this.current = '';
        } else if (c >= '0' && c <= '9') {
          this.current += c;
        } else {
          if (this.current != '') {
            this.csi_parameters.push(this.current);
          }
          if (c == 'J') {
            var par;
            if (this.csi_parameters && this.csi_parameters.length > 0) {
              par = this.csi_parameters[0];
            } else {
              par = '0';
            }
            if (par == '0') {
              log('unhandled CSI 0J', WARN);
            } else if (par == '1') {
              log('unhandled CSI 1J', WARN);
            } else if (par == '2') {
              this.screen.clear(false);
              log('CSI 2J', INFO);
            } else if (par == '3') {
              log('unhandled CSI 3J', WARN);
            }
          } else if (c == 'H') {
            var cursor_x = 0;
            var cursor_y = 0;
            if (this.csi_parameters && this.csi_parameters.length > 1) {
              cursor_y = parseInt(this.csi_parameters[0]) -1;
              cursor_x = parseInt(this.csi_parameters[1]) -1;
            }
            this.screen.set_cursor(cursor_x, cursor_y);
            log('CSI ' + cursor_y + ';' + cursor_x + 'H', INFO);
          } else if (c == 'K') {
            var par;
            if (this.csi_parameters && this.csi_parameters.length > 0) {
              par = this.csi_parameters[0];
            } else {
              par = '0';
            }
            if (par == '0') {
              this.screen.erase_line_from_cursor();
              this.screen.set_cursor(this.screen.cursor.x, this.screen.cursor.y);
              log('CSI ' + par + 'K', INFO);
            } else {
              log('unhandled CSI ' + par + 'K', WARN);
            }
          } else if (c == 'C') {
            var par;
            if (this.csi_parameters && this.csi_parameters.length > 0) {
              par = this.csi_parameters[0];
            } else {
              par = '1';
            }
            // TODO I don't like it, but the cursor position can be different after flush
            this.screen.flush();
            this.screen.set_cursor(this.screen.cursor.x + parseInt(par), this.screen.cursor.y);
            log('CSI ' + par + 'C', INFO);
            // Cursor Backward
          } else if (c == 'D') {
            var par;
            if (this.csi_parameters && this.csi_parameters.length > 0) {
              par = this.csi_parameters[0];
            } else {
              par = '1';
            }
            // TODO I don't like it, but the cursor position can be different after flush
            this.screen.flush();
            this.screen.set_cursor(this.screen.cursor.x - parseInt(par), this.screen.cursor.y);
            log('CSI ' + par + 'D', INFO);
            // goto column
          } else if (c == 'G') {
            var par;
            if (this.csi_parameters && this.csi_parameters.length > 0) {
              par = this.csi_parameters[0];
            } else {
              par = '1';
            }
            // TODO I don't like it, but the cursor position can be different after flush
            this.screen.flush();
            this.screen.set_cursor(this.screen.cursor.x, parseInt(par) -1);
            log('CSI ' + par + 'G', INFO);
          } else if (c == 'h' && this.csi_parameters && this.csi_parameters.length > 0) {
            if (this.csi_parameters[0] == '?' && this.csi_parameters.length == 2) {
              if (this.csi_parameters[1] == '1') {
                this.app_mode = true;
                log('CSI ?1h', INFO);
              } else if (this.csi_parameters[1] == '7') {
                this.screen.wrap_around = true;
                log('CSI ?7h', INFO);
              } else if (this.csi_parameters[1] == '25') {
                this.screen.show_cursor();
                log('CSI ?25h', INFO);
              } else {
                if (is_log(WARN)) {
                  log('unhandled CSI ' + string_array_to_string(this.csi_parameters) + 'h', WARN);
                }
              }
            } else {
              if (is_log(WARN)) {
                log('unhandled CSI ' + string_array_to_string(this.csi_parameters)+ 'h', WARN);
              }
            }
          } else if (c == 'l' && this.csi_parameters && this.csi_parameters.length > 0) {
            if (this.csi_parameters[0] == '?' && this.csi_parameters.length == 2) {
              if (this.csi_parameters[1] == '1') {
                this.app_mode = false;
                log('CSI ?1l', INFO);
              } else if (this.csi_parameters[1] == '7') {
                this.screen.wrap_around = false;
                log('CSI ?7l', INFO);
              } else if (this.csi_parameters[1] == '25') {
                this.screen.hide_cursor();
                log('CSI ?25l', INFO);
              } else {
                if (is_log(WARN)) {
                  log('unhandled CSI ' + string_array_to_string(this.csi_parameters)+ 'l', WARN);
                }
              }
            } else {
              if (is_log(WARN)) {
                log('unhandled CSI ' + string_array_to_string(this.csi_parameters) + 'l', WARN);
              }
            }
          } else if (c == 'P') {
            var par;
            if (this.csi_parameters && this.csi_parameters.length > 0) {
              par = this.csi_parameters[0];
            } else {
              par = '1';
            }
            this.screen.delete_chars(parseInt(par));
            log('CSI ' + par + 'P', INFO);
          } else if (c == 'A') {
            var par;
            if (this.csi_parameters && this.csi_parameters.length > 0) {
              par = this.csi_parameters[0];
            } else {
              par = '1';
            }
            this.screen.up(parseInt(par));
            log('CSI ' + par + 'A', INFO);
          } else if (c == 'r') {
            var par;
            if (this.csi_parameters && this.csi_parameters.length > 0) {
              par = this.csi_parameters[0];
            } else {
              par = '1';
            }
            // Restore DEC Private Mode Values. The value of P s previously saved is restored. P s values are the same as for DECSET.
            if (par == '?') {
              if (is_log(WARN)) {
                log('unhandled CSI ' + string_array_to_string(this.csi_parameters) + c, WARN);
              }
              // scroll region
            } else {
              if (!this.csi_parameters || this.csi_parameters.length == 0) {
                this.screen.scroll_region = {first:0, last:this.screen.height -1};
              } else {
                this.screen.scroll_region = {first:parseInt(this.csi_parameters[0]) -1,
                  last:parseInt(this. csi_parameters[1]) -1};
              }
              if (is_log(INFO)) {
                log('CSI ' + string_array_to_string(this.csi_parameters) + c, INFO);
              }
            }
          } else if (c == 'm') {
            if (this.csi_parameters.length > 0) {
              var ic;
              for (ic = 0; ic < this.csi_parameters.length; ic++) {
                var par = this.csi_parameters[ic];
                if (par == '0') {
                  this.screen.set_default_attributes();
                } else if (par == '1' || par == '01') {
                  this.screen.set_bold();
                } else if (par == '22') {
                  this.screen.set_normal();
                } else if (par == '30') {
                  this.screen.set_fg_color('blue'); // BLACK TODO restore when handled bg
                } else if (par == '31') {
                  this.screen.set_fg_color('red');
                } else if (par == '32') {
                  this.screen.set_fg_color('green');
                } else if (par == '33') {
                  this.screen.set_fg_color('yellow');
                } else if (par == '34') {
                  this.screen.set_fg_color('cyan'); // BLUE
                } else if (par == '35') {
                  this.screen.set_fg_color('magenta');
                } else if (par == '36') {
                  this.screen.set_fg_color('cyan');
                } else if (par == '37') {
                  this.screen.set_fg_color('white');
                } else if (par == '39') {
                  this.screen.set_default_fg_color();
                } else if (par == '40') {
                  this.screen.set_bg_color('blue'); // BLACK TODO restore when handled bg
                } else if (par == '41') {
                  this.screen.set_bg_color('red');
                } else if (par == '42') {
                  this.screen.set_bg_color('green');
                } else if (par == '43') {
                  this.screen.set_bg_color('yellow');
                } else if (par == '44') {
                  this.screen.set_bg_color('cyan'); // BLUE
                } else if (par == '45') {
                  this.screen.set_bg_color('magenta');
                } else if (par == '46') {
                  this.screen.set_bg_color('cyan');
                } else if (par == '47') {
                  this.screen.set_bg_color('white');
                } else if (par == '49') {
                  this.screen.set_default_bg_color();
                } else {
                  log('unhandled CSI: ' + par + c, WARN);
                }
              }
            } else {
              this.screen.set_default_attributes();
            }
            // insert lines
          } else if (c == 'L') {
            var par;
            if (this.csi_parameters && this.csi_parameters.length > 0) {
              par = this.csi_parameters[0];
            } else {
              par = '1';
            }
            this.screen.insert_lines(parseInt(par));
            log('CSI ' + par + 'L', INFO);
            // delete lines
          } else if (c == 'M') {
            var par;
            if (this.csi_parameters && this.csi_parameters.length > 0) {
              par = this.csi_parameters[0];
            } else {
              par = '1';
            }
            this.screen.delete_lines(parseInt(par));
            log('CSI ' + par + 'M', INFO);
            // insert chars
          } else if (c == '@') {
            var par;
            if (this.csi_parameters && this.csi_parameters.length > 0) {
              par = this.csi_parameters[0];
            } else {
              par = '1';
            }
            this.screen.insert_chars(parseInt(par));
            log('CSI ' + par + '@', INFO);
          } else {
            if (is_log(WARN)) {
              log('unhandled CSI: ' + string_array_to_string(this.csi_parameters) + c, WARN);
            }
          }
          this.current = '';
          this.csi_parameters = new Array();
          this.state = 0;
        }
        // operating system command
      } else if (this.state == 7) {
        if (c == '\\' || code == 7) {
          // title
          if (this.current.length > 2 && this.current.substr(0, 2) == '0;') {

          } else {
            log('unhandled OS command:"' + this.current + '"');
          }
          this.current = '';
          this.state = 0;
        } else {
          this.current += c;
        }
        // application command
      } else if (this.state == 8) {
        if (c == '\\' || code == 7) {
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
        } else {
          this.current += c;
        }
*/
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
          this.screen.tab();
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
