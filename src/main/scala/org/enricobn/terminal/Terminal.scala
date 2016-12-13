package org.enricobn.terminal

/**
  * Created by enrico on 12/13/16.
  */
trait Terminal {

  def onInput(subscriber: _root_.org.enricobn.terminal.StringPub#Sub)

  def removeOnInput(subscriber: _root_.org.enricobn.terminal.StringPub#Sub)

  def removeOnInput()

  def scroll_back_page_up()

  def scroll_back_page_down()

  // add string to cursor position
  def add(text: String)

  def flush()
}
