package org.enricobn.terminal

import scala.collection.mutable

/**
  * Created by enrico on 12/6/16.
  */
class KeyPressPub extends mutable.Publisher[Char] {
  override type Pub = mutable.Publisher[Char]

  override def publish(event: Char) {
    super.publish(event)
  }
}
