package org.enricobn.terminal

import scala.collection.mutable

/**
  * Created by enrico on 12/6/16.
  */
class KeyDownPub extends mutable.Publisher[Int] {
  override type Pub = mutable.Publisher[Int]

  override def publish(event: Int) {
    super.publish(event)
  }
}