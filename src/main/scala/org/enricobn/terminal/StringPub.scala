package org.enricobn.terminal

import scala.collection.mutable

/**
  * Created by enrico on 12/6/16.
  */
class StringPub extends mutable.Publisher[String] {
  override type Pub = mutable.Publisher[String]

  override def publish(event: String) {
    super.publish(event)
  }
}