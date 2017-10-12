package org.enricobn.terminal
import org.scalajs.dom.CanvasRenderingContext2D

trait TextScreen {

  var wrap_around: Boolean
  val width: Int
  val height: Int
  var scroll_region: ScrollRegion
  val cursor: Coords
  def is_scrolling() : Boolean

  def get_coords_at_pixel(x: Int, y: Int): Coords

  // {from:{x,y}, to{x,y}}
  def set_selection(optionalSelection: Option[Selection])

  def get_selected_text() : Option[String]

  def scroll_back_page_up()

  def scroll_back_page_down()

  def scroll_back_up(lines: Int)

  def scroll_back_down(lines: Int)

  def reset_scrolling()

  def update()

  def flush()

  def clone_attributes(): CellAttributes

  def set_normal()

  def set_bold()

  def set_default_fg_color()

  def set_default_bg_color()

  def set_default_attributes() : Unit

  def set_fg_color(color: String)

  def set_bg_color(color: String)

  // add char to cursor position and increment cursor
  def add_char(c: Char)

  def is_valid_cursor(x: Int, y: Int): Boolean

  def apply_cell_attributes(context: CanvasRenderingContext2D, attributes: Option[CellAttributes])

  // set the char in cursor position, without affecting the cursor position
  def set_char(c: Char, attributes: Option[CellAttributes])

  def tab()

  // delete char in cursor position
  def delete_chars(count: Int)

  def redraw(context: CanvasRenderingContext2D)

  def draw(context: CanvasRenderingContext2D)

  def insert_lines(count: Int)

  def delete_lines(count: Int)

  def redraw_line(context: CanvasRenderingContext2D, line: Int)

  def equals_attributes(a1: Option[CellAttributes], a2: Option[CellAttributes]): Boolean

  def draw_line(context: CanvasRenderingContext2D, line: Int)

  def insert_chars(count: Int)

  /**
    * scroll_back true if it must put the scrolled region to the scroll buffer
    */
  def scroll_up(count: Int, scroll_back: Boolean)

  /**
    * scroll_back true if it must get lines from scroll buffer
    */
  def scroll_down(count: Int, scroll_back: Boolean)

  def hide_cursor()

  def show_cursor()

  def set_cursor(_x: Int, _y: Int)

  def cursor_right()

  def carriage_return()

  def line_feed()

  def clear(reset_cursor: Boolean)

  def erase_line_from_cursor()

  def backspace()

  /**
    * scroll_back true if it must get lines from scroll buffer
    */
  def up(count: Int, scroll_back: Boolean)
}
