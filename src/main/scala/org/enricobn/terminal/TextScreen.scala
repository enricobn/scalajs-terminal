package org.enricobn.terminal
import org.scalajs.dom.CanvasRenderingContext2D

trait TextScreen {

  val width: Int
  val height: Int
  val cursor: Coords

  def set_wrap_around(wrap_around: Boolean) : Unit

  def set_scroll_region(scroll_region: ScrollRegion) : Unit

  def is_scrolling(): Boolean

  def get_coords_at_pixel(x: Int, y: Int): Coords

  // {from:{x,y}, to{x,y}}
  def set_selection(optionalSelection: scala.Option[Selection]): Unit

  def get_selected_text(): Option[String]

  def scroll_back_page_up(): Unit

  def scroll_back_page_down(): Unit

  def scroll_back_up(lines: Int): Unit

  def scroll_back_down(lines: Int): Unit

  def reset_scrolling(): Unit

  def update(): Unit

  def flush(): Unit

  def clone_attributes(): CellAttributes

  def set_normal(): Unit

  def set_bold(): Unit

  def set_default_fg_color(): Unit

  def set_default_bg_color(): Unit

  def set_default_attributes(): Unit

  def set_fg_color(color: String): Unit

  def set_bg_color(color: String): Unit

  // add char to cursor position and increment cursor
  def add_char(c: Char): Unit

  def is_valid_cursor(x: Int, y: Int): Boolean

  def apply_cell_attributes(context: CanvasRenderingContext2D, attributes: Option[CellAttributes]): Unit

  // set the char in cursor position, without affecting the cursor position
  def set_char(c: Char, attributes: Option[CellAttributes]): Unit

  def tab(): Unit

  // delete char in cursor position
  def delete_chars(count: Int): Unit

  def redraw(context: CanvasRenderingContext2D): Unit

  def draw(context: CanvasRenderingContext2D): Unit

  def insert_lines(count: Int): Unit

  def delete_lines(count: Int): Unit

  def redraw_line(context: CanvasRenderingContext2D, line: Int): Unit

  def equals_attributes(a1: Option[CellAttributes], a2: Option[CellAttributes]): Boolean

  def draw_line(context: CanvasRenderingContext2D, line: Int): Unit

  def insert_chars(count: Int): Unit

  /**
    * scroll_back true if it must put the scrolled region to the scroll buffer
    */
  def scroll_up(count: Int, scroll_back: Boolean): Unit

  /**
    * scroll_back true if it must get lines from scroll buffer
    */
  def scroll_down(count: Int, scroll_back: Boolean): Unit

  def hide_cursor(): Unit

  def show_cursor(): Unit

  def set_cursor(_x: Int, _y: Int): Unit

  def cursor_right(): Unit

  def carriage_return(): Unit

  def line_feed(): Unit

  def clear(reset_cursor: Boolean): Unit

  def erase_line_from_cursor(): Unit

  def backspace(): Unit

  /**
    * scroll_back true if it must get lines from scroll buffer
    */
  def up(count: Int, scroll_back: Boolean): Unit

  def setDefaultCellAttributes(cellAttributes: CellAttributes): Unit
}
