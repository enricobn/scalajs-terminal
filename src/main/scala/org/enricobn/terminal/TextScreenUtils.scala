package org.enricobn.terminal

/**
  * Created by enrico on 11/29/16.
  */
case class FontMetrics(ascent: Int, height: Int, descent: Int)

case class ScrollRegion(first: Int, last: Int)

case class Coords(var x: Int, var y: Int)

case class Selection(from: Coords, to: Coords) {
  def plusY(diff: Int) = new Selection(new Coords(from.x, from.y + diff), new Coords(to.x, to.y + diff))
}
