package org.enricobn.terminal

import scala.collection.mutable

/**
  * Created by enrico on 12/6/16.
  */
class KeyDownPub extends mutable.Publisher[KeyDownEvent] {
  override type Pub = mutable.Publisher[KeyDownEvent]

  override def publish(event: KeyDownEvent) {
    super.publish(event)
  }
}