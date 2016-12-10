package org.enricobn.terminal

import org.scalajs.dom._
import org.scalajs.dom.raw.{Event, XMLHttpRequest}

import scala.scalajs.js.typedarray.ArrayBuffer


/**
  * Created by enrico on 12/7/16.
  *
  * @param resourcePath the path of the resource of the wav file
  */
class SoundEffect(resourcePath: String) {
  private lazy val audioContext = new AudioContext

  private var audioBuffer: AudioBuffer = null

  private val request = new XMLHttpRequest
  //val urlToMp3File = "typewriter-key-1.wav" // "http://sciss.de/noises2/staircase.mp3"
  request.open("GET", url = resourcePath, async = true)
  request.responseType = "arraybuffer"

  request.onload = onLoad _
  request.send()

  private def onLoad(e: Event): Unit = {
    val audioData = request.response.asInstanceOf[ArrayBuffer]  // ja?!

    def gotBuffer(buffer: AudioBuffer): Unit = {
      audioBuffer = buffer
    }

    /* val promiseBuffer = */ audioContext.decodeAudioData(audioData, gotBuffer _)

    // promiseBuffer.andThen(gotBuffer _)
  }

  def play() {
    val n = audioContext.createBufferSource()
    n.buffer = audioBuffer
    n.connect(audioContext.destination)
    n.start()
  }

}
