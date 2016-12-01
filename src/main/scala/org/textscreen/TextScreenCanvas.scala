package org.textscreen

import org.scalajs.dom
import org.scalajs.dom._
import org.scalajs.dom.html._

import scala.collection.mutable.ArrayBuffer
import scala.scalajs.js.annotation.{JSExport, JSExportAll}

/**
  * Created by enrico on 11/29/16.
  */
@JSExportAll
@JSExport(name = "TextScreenCanvas")
class TextScreenCanvas(val canvasId: String) extends JSLog {
  private var cell_attributes: scala.Option[CellAttributes] = None
  private var selection: scala.Option[Selection] = None
  private var updated = true
  private var selected_text: scala.Option[String] = None
  private var cells = new ArrayBuffer[String]()

  private val default_cell_attributes = new CellAttributes(false, false, "white")

  private val canvas = dom.document.getElementById(canvasId).asInstanceOf[Canvas]
  private val ctx = canvas.getContext("2d").asInstanceOf[CanvasRenderingContext2D]
  private val font_base = "16px Courier New"

  // I must apply attributes (font basically) to calculate font size
  default_cell_attributes.apply(ctx, font_base)

  private val wrap_around = false
  private val cell_width = ctx.measureText("M").width
  private val cell_height = getFontHeight("Courier New", "16px", "Mg")

  private val width: Int = 120
  private val height: Int = 40

  private val cells_attributes = new ArrayBuffer[ArrayBuffer[scala.Option[CellAttributes]]]()

  initCellAttributes()

  private val empty_row = " " * width

  canvas.width = (cell_width * width).toInt
  canvas.height = cell_height.height * height

  ctx.fillStyle = default_cell_attributes.fg_color

  private val tmp_canvas = dom.document.createElement("canvas").asInstanceOf[Canvas]
  tmp_canvas.width = canvas.width
  tmp_canvas.height = canvas.height
  private val tmp_ctx = tmp_canvas.getContext("2d").asInstanceOf[CanvasRenderingContext2D]
  // TODO
  // tmp_canvas.visible = false

  // after resizing, I must reapply attributes
  default_cell_attributes.apply(ctx, font_base)

  // for cursor
  ctx.strokeStyle = default_cell_attributes.fg_color

  private var scroll_region = new ScrollRegion(0, height - 1)

  // TODO accesor method
  val cursor = new Coords(0, 0)

  private val default_bg_color = dom.document.body.style.backgroundColor

  private var chars_buffer = ""

  set_default_attributes()

  private var ctx_cell_attributes: scala.Option[CellAttributes] = None

  private val colors = true

  clear(true)

  private val scroll_back_buffer = new ArrayBuffer[String]()

  private var scroll_back_line = 0

  // TODO accessor method
  var scrolling = false

  private var cursor_visible = true


//  private def createContext() : CanvasRenderingContext2D = {
//    val canvas = dom.document.createElement("Canvas").asInstanceOf[Canvas]
//    canvas.style.zIndex = "0"
//    canvas.style.position = "absolute"
//    canvas.style.left = div.style.left// "0"
//    canvas.style.top = div.style.top //"0"
//    div.appendChild(canvas)
//
//    canvas.width = width
//    canvas.height = height
//
//    canvas.getContext("2d").asInstanceOf[CanvasRenderingContext2D]
//  }

  private def initCellAttributes() {
    cells_attributes.clear()

    for (y <- 0 to height) {
      val row = new ArrayBuffer[scala.Option[CellAttributes]]()
      cells_attributes += row
      for (x <- 0 to width + 1) {
        row += None
      }
    }
  }

  def get_coords_at_pixel(x: Int, y: Int) : Coords = {
    new Coords(
      Math.max(0, Math.min(width -1, Math.floor(x / this.cell_width).toInt)),
      Math.max(0, Math.min(height - 1, Math.floor(y / this.cell_height.height).toInt))
    )
  }

  // {from:{x,y}, to{x,y}}
  def set_selection(optionalSelection: scala.Option[Selection]) {
    this.selection = optionalSelection
    this.updated = false

    //log('selection={from:' + this.selection.from.x + ',' + this.selection.from.y + ',to:' + this.selection.to.x + ',' + this.selection.to.y, WARN);

    optionalSelection match {
      case Some(selection) =>
        var selected_text = ""
        for (y <- selection.from.y to selection.to.y) {
          if (y >= 0 && y < height) {
            var from_x = 0
            var to_x = width - 1
            if (y == selection.from.y) {
              from_x = selection.from.x
            }
            if (y == selection.to.y) {
              to_x = selection.to.x
            }
            if (selected_text.length > 0) {
              selected_text += '\n'
            }
            if (scrolling) {
              var line = ""
              if (scroll_back_buffer.length > scroll_back_line + y) {
                line = scroll_back_buffer(scroll_back_line + y)
              } else {
                line = cells(y - scroll_back_buffer.length - scroll_back_line)
              }
              selected_text += line.substring(from_x, to_x - from_x + 1)
            } else {
              selected_text += cells(y).substring(from_x, to_x - from_x + 1)
            }
          }
        }
        if (selected_text.length == 0) {
          this.selected_text = None
          //        } else {
          //            log('selected text="' + this.selected_text + '"', WARN);
        } else {
          this.selected_text = Some(selected_text)
        }
      case None =>
        this.selected_text = None

    }
  }

  def get_selected_text() = selected_text


  def scroll_back_page_up() =
    scroll_back_up(this.height)


  def scroll_back_page_down() =
    scroll_back_down(this.height)


  def scroll_back_up(lines: Int) {
    var real_scroll = lines

    scroll_back_line -= lines

    if (scroll_back_line < 0) {
      real_scroll += scroll_back_line
      scroll_back_line = 0
    }

    // TODO pattern match
    if (selection.isDefined) {
      selection = Some(selection.get.plusY(real_scroll))
    }

    scrolling = true

    if (this.scroll_back_line == this.scroll_back_buffer.length) {
      reset_scrolling()
    }

    updated = false
  }

  def scroll_back_down(lines: Int) {
    var real_scroll = lines
    scroll_back_line += lines

    if (scroll_back_line > scroll_back_buffer.length) {
      real_scroll -= scroll_back_line - this.scroll_back_buffer.length
      scroll_back_line = this.scroll_back_buffer.length
    } else {
      scrolling = true
    }

    // TODO pattern match
    if (selection.isDefined) {
      selection = Some(selection.get.plusY(-real_scroll))
    }

    if (scroll_back_line == scroll_back_buffer.length) {
      reset_scrolling()
    }
    updated = false
  }

  def reset_scrolling() {
    flush()
    scrolling = false
    scroll_back_line = scroll_back_buffer.length
  }

  def update() {
    if (!updated) {
      //        this.canvas.width = this.canvas.width;
      ctx.clearRect(0, 0, canvas.width, canvas.height)

      if (scrolling) {
        for (y <- 0 to height) {
          var line = ""

          if (scroll_back_buffer.length > scroll_back_line + y) {
            line = scroll_back_buffer(scroll_back_line + y)
          } else {
            line = this.cells(y - (scroll_back_buffer.length - scroll_back_line))
          }
          ctx.fillText(line, 0, y * cell_height.height + cell_height.ascent)
        }
      } else {
        draw(ctx)
      }
      updated = true

      // TODO optimize
      ctx.globalCompositeOperation = "xor"

      if (cursor_visible && !this.scrolling) {
        ctx.fillRect(cursor.x * cell_width, cursor.y * cell_height.height,
          cell_width, this.cell_height.height)
      }

      // TODO pattern matching
      if (selection.isDefined) {
        for (y <- selection.get.from.y to selection.get.to.y) {
          if (y >= 0 && y < height) {
            var from_x = 0
            var to_x = width -1
            if (y == selection.get.from.y) {
              from_x = selection.get.from.x
            }
            if (y == selection.get.to.y) {
              to_x = selection.get.to.x
            }
            ctx.fillRect(from_x * cell_width,
              y * cell_height.height,
              (to_x - from_x + 1) * cell_width,
              cell_height.height)
          }
        }
      }
      ctx.globalCompositeOperation = "source-over"
    }
  }

  def flush() {
    if (chars_buffer.length == 0) {
      return
    }
    var buffer = this.chars_buffer

    chars_buffer = ""

    var s = ""
    if (cursor.x > 0) {
      s = cells(cursor.y).substring(0, cursor.x)
    }
    s += buffer
    if ((cursor.x  + buffer.length) < width) {
      s += cells(cursor.y).substring(cursor.x + buffer.length)
    }
    cells(cursor.y) = s

    if (colors) {
      for (x <- cursor.x to cursor.x + buffer.length) {
        cells_attributes(cursor.y)(x) = cell_attributes
      }
    }
    set_cursor(cursor.x + buffer.length, cursor.y)
    updated = false
  }

  def clone_attributes() : CellAttributes = {
    if (cell_attributes.isDefined) {
      cell_attributes.get.copy()
    } else {
      default_cell_attributes.copy()
    }
  }

  def set_normal() {
    if (!colors) {
      return
    }
    flush()
    cell_attributes = Some(clone_attributes())
    cell_attributes.get.bold = false
  }

  def set_bold() {
    if (!colors) {
      return
    }
    flush()
    cell_attributes = Some(clone_attributes())
    cell_attributes.get.bold = true
  }

  def set_default_fg_color() {
    if (!colors) {
      return
    }
    flush()
    cell_attributes = Some(clone_attributes())
    cell_attributes.get.fg_color = default_cell_attributes.fg_color
  }

  def set_default_bg_color() {
    if (!colors) {
      return
    }
    flush()
    this.cell_attributes = Some(clone_attributes())
    this.cell_attributes.get.bg_color = default_cell_attributes.bg_color
  }

  def set_default_attributes() {
    flush()
    cell_attributes = None
  }

  def set_fg_color(color: String) {
    if (!colors) {
      return
    }
    flush()
    cell_attributes = Some(clone_attributes())
    cell_attributes.get.fg_color = color
  }

  def set_bg_color(color: String) {
    if (!colors) {
      return
    }
    flush()
    cell_attributes = Some(clone_attributes())
    cell_attributes.get.bg_color = color
  }

  // add char to cursor position and increment cursor
  def add_char(c: Char) {
    if (!equals_attributes(cell_attributes, ctx_cell_attributes)) {
      flush()
    }
    if ((cursor.x + chars_buffer.length) >= width) {
      // it calls flush
      set_cursor(0, cursor.y + 1)
    }
    chars_buffer += c
  }

  def is_valid_cursor(x: Int, y: Int) : Boolean =
    x >=0 && x <= width && y >=0 && y < height


  def apply_cell_attributes(context: CanvasRenderingContext2D, attributes: scala.Option[CellAttributes]) {
    flush()

    if (!equals_attributes(attributes, ctx_cell_attributes)) {
      if (attributes.isDefined) {
        attributes.get.apply_diff(context, font_base, ctx_cell_attributes)
      } else {
        default_cell_attributes.apply_diff(context, font_base, ctx_cell_attributes)
      }
    }
    ctx_cell_attributes = attributes
  }

  // set the char in cursor position, without affecting the cursor position
  def set_char(c: Char, attributes: scala.Option[CellAttributes]) {
    flush()
    // if a char is written outside the window I go to the next line, it happens within vim and bash itself
    if (cursor.x >= width) {
      set_cursor(0, cursor.y +1)
    }

    if (cursor.x <= width) {// && this.cursor.y < this.height) {
      var s = ""
      if (cursor.x > 0) {
        s = cells(cursor.y).substring(0, cursor.x)
      }
      s += c
      if (cursor.x < width) {
        s += cells(cursor.y).substring(cursor.x +1)
      }
      cells(cursor.y) = s

      if (cursor.x == width) {
        log("Screen: attempt to write '" + c + "' to (" + cursor.x + ", " + cursor.y + ")", Levels.INFO)
      }
      if (colors) {
        cells_attributes(cursor.y)(cursor.x) = attributes
      }
    } else {
      log("Screen: attempt to write '" + c + "' to (" + cursor.x + ", " + cursor.y + ")", Levels.INFO)
    }
  }

  def tab() {
    flush()
    set_cursor(cursor.x - cursor.x % 8 + 8, cursor.y)
  }

  // delete char in cursor position
  def delete_chars(count: Int) {
    flush()
    var cursor_x = cursor.x
    var cursor_y = cursor.y

    for (x <- cursor.x until width-count) {
      val c = cells(cursor.y).charAt(x + count)
      if (colors) {
        val attributes = cells_attributes(cursor.y)(x + count)
        set_cursor(x, cursor.y)
        set_char(c, attributes)
      } else {
        set_cursor(x, cursor.y)
        set_char(c, None)
      }
    }

    // TODO check since in JS it starts from the value of the other loop
    for (x <- width-count until width) {
      set_cursor(x, cursor.y)
      set_char(' ', None)
    }

    set_cursor(cursor_x, cursor_y)
  }

  def redraw(context: CanvasRenderingContext2D) {
    flush()
    updated = false
  }

  def draw(context: CanvasRenderingContext2D) {
    flush()
    for (y <- 0 until height) {
      draw_line(context, y)
    }
  }

  def insert_lines(count: Int) {
    flush()
    val scroll_region = new ScrollRegion(this.scroll_region.first, this.scroll_region.last)

    this.scroll_region = new ScrollRegion(cursor.y, this.scroll_region.last)
    scroll_down(count, false)
    this.scroll_region = scroll_region
  }

  def delete_lines(count: Int) {
    flush()
    val scroll_region = new ScrollRegion(this.scroll_region.first, this.scroll_region.last)

    this.scroll_region = new ScrollRegion(cursor.y, this.scroll_region.last)
    scroll_up(count, false)
    this.scroll_region = scroll_region
  }

  def redraw_line(context: CanvasRenderingContext2D, line: Int) {
    flush()
    context.clearRect(0, line * cell_height.height, canvas.width, (line + 1) * cell_height.height)
    draw_line(context, line)
  }

  def equals_attributes(a1: scala.Option[CellAttributes], a2: scala.Option[CellAttributes]) : Boolean = {
    if (a1.isDefined) {
      if (a2.isDefined) {
        a1.equals(a2)
      } else {
        a1.get.equals(default_cell_attributes)
      }
    } else if (a2.isDefined) {
      a2.get.equals(default_cell_attributes)
    } else {
      true
    }
  }

  def draw_line(context: CanvasRenderingContext2D, line: Int) {
    flush()
    if (colors) {
      // I try to optimize, rendering text in chunks of equal attributes
      var current_x = 0
      for (x <- 0 until width) {
        val attributes = cells_attributes(line)(x)
        if (!equals_attributes(attributes, ctx_cell_attributes)) {
          context.fillText(cells(line).substring(current_x, x -current_x), current_x * cell_width, line * cell_height.height +
            cell_height.ascent)
          apply_cell_attributes(context, attributes)
          current_x = x
        }
      }

      // TODO check since in JS x is the value of the other loop, here I use width
      if (width - 1 > current_x) {
        context.fillText(cells(line).substring(current_x, width -current_x), current_x * cell_width, line * cell_height.height +
          cell_height.ascent)
      }
    } else {
      context.fillText(cells(line), 0, line * cell_height.height + cell_height.ascent)
    }
  }

  def insert_chars(count: Int) {
    flush()
    var s = ""
    if (cursor.x > 0) {
      s += cells(cursor.y).substring(0, cursor.x)
    }
    for (i <- 0 until count) {
      s += ' '
    }
    if (cursor.x + count <= width) {
      s += cells(cursor.y).substring(cursor.x, width - (cursor.x + count) + 1)
    }

    cells(cursor.y) = s

    // TODO colors
    if (colors) {
      log("insert_chars, colors not handled", Levels.ERROR)
    }

    // I don't move the cursor
    //this.set_cursor(this.cursor.x + count, this.cursor.y);
  }

  /**
    * scroll_back true if it must put the scrolled region to the scroll buffer
    */
  def scroll_up(count: Int, scroll_back: Boolean) {
    flush()

    if (selection.isDefined) {
      selection.get.from.y -= count
      selection.get.to.y -= count
    }

    val cursor_visible = this.cursor_visible

    if (cursor_visible) {
      hide_cursor()
    }

    for (y <- scroll_region.first until this.scroll_region.last + 1 - count) {
      if (scroll_back && (y - scroll_region.first) < count) {
        scroll_back_buffer += cells(y)
        if (scroll_back_buffer.length > 1000) {
          scroll_back_buffer.remove(0) // shift
        }
        scroll_back_line = scroll_back_buffer.length
      }
      cells(y) = cells(y + count)
      if (colors) {
        cells_attributes(y) = cells_attributes(y + count)
      }
    }

    for (y <- scroll_region.last + 1 - count until scroll_region.last + 1) {
      cells(y) = empty_row
      if (colors) {
        val row = new ArrayBuffer[scala.Option[CellAttributes]]()
        for (x <- 0 until width + 1) {
          row += None
        }
        cells_attributes(y) = row
      }
    }
    /*
        var sr_height = this.scroll_region.last - this.scroll_region.first + 1;

        //this.tmp_canvas.width = this.tmp_canvas.width;
        this.tmp_ctx.clearRect(0, 0, this.tmp_canvas.width, this.back_canvas.height);

        this.tmp_ctx.drawImage(this.back_canvas, 0, 0);

        this.back_ctx.clearRect(0, (this.scroll_region.first) * this.cell_height.height, this.back_canvas.width,
            this.cell_height.height * (this.scroll_region.last - this.scroll_region.first +1) -1);

        this.back_ctx.drawImage(this.tmp_canvas, //img
            0, // sx
            (this.scroll_region.first + count) * this.cell_height.height, // sy
            this.back_canvas.width, // swidth
            (this.scroll_region.last - this.scroll_region.first + 1 -count) *  this.cell_height.height, // sheight
            0, // x
            this.scroll_region.first * this.cell_height.height, // y
            this.back_canvas.width, // width
            (this.scroll_region.last - this.scroll_region.first + 1 -count) *  this.cell_height.height // height
        );
        if (cursor_visible) {
            this.show_cursor();
        }
    */
    this.cursor_visible = cursor_visible
  }

  /**
    * scroll_back true if it must get lines from scroll buffer
    */
  def scroll_down(count: Int, scroll_back: Boolean) {
    flush()

    if (selection.isDefined) {
      selection.get.from.y -= count
      selection.get.to.y -= count
    }

    val cursor_visible = this.cursor_visible

    if (cursor_visible) {
      hide_cursor()
    }

    for (y <- scroll_region.last to count by -1) {
      cells(y) = cells(y - count)
      if (colors) {
        cells_attributes(y) = cells_attributes(y - count)
      }
    }

    for (y <- scroll_region.first + count until scroll_region.first by -1) {
      if (scroll_back) {
        if (scroll_back_buffer.nonEmpty) {
          cells(y-1) = scroll_back_buffer.remove(scroll_back_line -1)
          // TODO colors
//          if (colors) {
//            cells_attributes(y) = cells_attributes(y - count)
//          }
          scroll_back_line -= 1
        }
      } else {
        cells(y) = empty_row
        if (colors) {
          val row = new ArrayBuffer[scala.Option[CellAttributes]]()
          for (x <- 0 until width + 1) {
            row += None
          }
          cells_attributes(y) = row
        }
      }
    }

    /*
        var sr_height = this.scroll_region.last - this.scroll_region.first + 1;

        //this.tmp_canvas.width = this.tmp_canvas.width;
        this.tmp_ctx.clearRect(0, 0, this.tmp_canvas.width, this.back_canvas.height);

        this.tmp_ctx.drawImage(this.back_canvas, 0, 0);

        this.back_ctx.clearRect(0, (this.scroll_region.first) * this.cell_height.height, this.back_canvas.width,
            this.cell_height.height * (this.scroll_region.last - this.scroll_region.first +1) -1);

        this.back_ctx.drawImage(this.tmp_canvas, //img
            0, // sx
            (this.scroll_region.first) * this.cell_height.height, // sy
            this.back_canvas.width, // swidth
            (this.scroll_region.last - this.scroll_region.first + 1 -count) *  this.cell_height.height, // sheight
            0, // x
            (this.scroll_region.first + count) * this.cell_height.height, // y
            this.back_canvas.width, // width
            (this.scroll_region.last - this.scroll_region.first + 1 -count) *  this.cell_height.height // height
        );
        if (cursor_visible) {
            this.show_cursor();
        }
    */
    this.cursor_visible = cursor_visible
  }

  def hide_cursor() {
    if (cursor_visible) {
      flush()
      if (cursor.x <= width) {
        updated = false
      }
      cursor_visible = false
    }
  }

  def show_cursor() {
    if (!cursor_visible) {
      flush()
      if (cursor.x <= width) {
        updated = false;
      }
      cursor_visible = true
    }
  }

  def set_cursor(_x: Int, _y: Int) {
    flush()

    var x = _x
    var y = _y

    if (x >= width && wrap_around) {
      x -= width
      y += 1
    }

    val cursor_visible = this.cursor_visible

    if (cursor_visible) {
      hide_cursor()
    }

    cursor.x = x
    cursor.y = y

    if (cursor_visible) {
      show_cursor()
    }

    this.cursor_visible = cursor_visible

    if (y > this.scroll_region.last) {
      // to handle infinite loop, since scroll_up can make a set_cursor
      cursor.y = scroll_region.last
      scroll_up(y - scroll_region.last, true)
      set_cursor(x, scroll_region.last)
    }
    //        console.warn('Screen: attempt to set cursor to (' + cell.x + ', ' + cell.y + ')');

  }

  def cursor_right() {
    flush()
    set_cursor(cursor.x + 1, cursor.y)
  }

  def carriage_return() {
    flush()
    set_cursor(0, cursor.y)
  }

  def line_feed() {
    flush()
    // TODO I don't know if it's correct to got tho first column
    set_cursor(0, cursor.y + 1)
  }

  def clear(reset_cursor: Boolean) {
    // TODO it's a trick to clear selection when running a program which clears the screen (vim, less, etc.)
    set_selection(None)

    flush()
    cells.clear()
    if (colors) {
      initCellAttributes()
    }
    for (y <- 0 until height) {
      /*
          var s = '';

          for (var x=0; x <= this.width; x++) {
              s += ' ';
          }
      */
      cells += empty_row
//      if (colors) {
//        cells_attributes.push(new Array(this.width +1));
//      }
    }

    if (reset_cursor) {
      set_cursor(0, 0)
    } else {
      set_cursor(cursor.x, cursor.y)
    }
  }

  def erase_line_from_cursor() {
    flush()
    // TODO optimize with substr
    var s = ""
    for (x <- 0 until width) {
      if (x < cursor.x) {
        s += cells(cursor.y).charAt(x)
      } else {
        s += ' '
        if (colors) {
          cells_attributes(cursor.y)(x) = None
        }
      }
    }
    cells(cursor.y) = s

  }

  def backspace() {
    flush()
    if (cursor.x > 0) {
      set_cursor(cursor.x - 1, cursor.y)
      // TODO must it go to prev line or the terminal does it?
    } else if (cursor.y > 0) {
      set_cursor(width - 1, cursor.y -1)
    }
  }

  /**
    * scroll_back true if it must get lines from scroll buffer
    */
  def up(count: Int, scroll_back: Boolean) {
    flush()
    // TODO optimize without a loop
    for (i <- 0 until count) {
      if (cursor.y > scroll_region.first) {
        set_cursor(cursor.x, cursor.y - 1)
      } else {
        scroll_down(1, scroll_back)
      }
    }
  }

  /*
 * from http://stackoverflow.com/questions/1134586/how-can-you-find-the-height-of-text-on-an-html-canvas
 */
  private def getFontHeight(fontFamily: String, size: String, t: String) : FontMetrics = {
    val text: Span = dom.document.createElement("span").asInstanceOf[Span]
    text.style.fontFamily = fontFamily
    text.style.fontSize = size

    val block: Div = dom.document.createElement("div").asInstanceOf[Div]
    block.style.display = "inline-block"
    block.style.width = "1px"
    block.style.height = "0px"

    val div: Div = dom.document.createElement("div").asInstanceOf[Div]
    div.appendChild(text)
    div.appendChild(block)


//    var body = $('body');
    dom.document.body.appendChild(div)

    block.style.verticalAlign = "baseline"
    val ascent = (block.offsetTop - text.offsetTop).toInt

    block.style.verticalAlign = "bottom"
    val height = (block.offsetTop - text.offsetTop).toInt

    val descent = height - ascent

    dom.document.body.removeChild(div)
    new FontMetrics(ascent, height, descent)
  }

}

