package org.enricobn.terminal

import org.scalajs.dom._

/**
  * Created by enrico on 11/29/16.
  */
// TODO makes parameters as vals?
class CellAttributes(var bold: Boolean = false, var italic: Boolean = false, var fg_color: String = "white",
                     var bg_color: String = "black") {

  def apply(ctx: CanvasRenderingContext2D, fontBase: String) {
    applyFont(ctx, fontBase)
    ctx.fillStyle = fg_color
  }

  private def applyFont(ctx: CanvasRenderingContext2D, fontBase: String): Unit = {
    var font = fontBase

    if (this.bold) {
      font = "bold " + font
    }

    if (this.italic) {
      font = "italic " + font
    }

    ctx.font = font
  }

  def copy() : CellAttributes = {
    new CellAttributes(bold, italic, fg_color, bg_color)
  }

  /**
    * applies only changed attributes
    */
  def apply_diff(ctx: CanvasRenderingContext2D, fontBase: String, actualAttributes: scala.Option[CellAttributes]) {
    if (actualAttributes.isDefined) {
      if (this.bold != actualAttributes.get.bold || this.italic != actualAttributes.get.italic) {
        applyFont(ctx, fontBase)
      }

      if (this.fg_color != actualAttributes.get.fg_color) {
        ctx.fillStyle = this.fg_color
      }
    } else {
      this.apply(ctx, fontBase)
    }
  }

  def canEqual(other: Any): Boolean = other.isInstanceOf[CellAttributes]

  override def equals(other: Any): Boolean = other match {
    case that: CellAttributes =>
      (that canEqual this) &&
        bold == that.bold &&
        italic == that.italic &&
        fg_color == that.fg_color &&
        bg_color == that.bg_color
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(bold, italic, fg_color, bg_color)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }
}
