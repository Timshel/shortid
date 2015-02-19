package io.github.timshel

import java.util.Date

import scala.collection.mutable.StringBuilder
import scala.concurrent.stm.Ref

/**
 * alphabet: The characters used for the encoding
 * overflow: This char will be added between the seconds and counter if we overflow.
 *           To prevent any duplicate this char should not be included in the alphabet.
 * padLength: The number of characters used to encode the seconds
 *            (with base64 and 5 chars it give us ~34years before we start to overflow).
 * version: Don't change unless you change the algo or reduceTime (Int < alphabet.length).
 * reduceTime: Ignore all milliseconds before a certain time to reduce the size of the date without sacrificing uniqueness.
 *             To regenerate `DateTime.now()` and bump the version. Always bump the version!
 * nodeId: If you are using multiple servers use this to make each instance has a unique value (Int < alphabet.length).
 */
case class ShortID(
  alphabet:   String,
  overflow:   Char,
  padLength:  Int,
  version:    Int,
  reduceTime: Long,
  nodeId:     Option[Int]
){
  import ShortID._

  val state     = Ref.apply( State(0, 0) ).single

  def generate(): String = {
    val seconds = (new Date().getTime() - reduceTime) / 1000;
    val count   = state.transformAndGet {
      case State(p, _) if seconds > p => State(seconds, 1)
      case s => s.copy(counter = s.counter + 1)
    }.counter

    encode(seconds, count-1)
  }

  /**
   * If the encoded seconds is longer than the padLength add the overflow char.
   */
  def encode(seconds: Long, count: Long): String = {
    val builder = StringBuilder.newBuilder
    val overflowed = padLength + ( if( nodeId.isDefined ) 2 else 1 )

    builder + alphabet.charAt(version)
    nodeId.foreach { i => builder + alphabet.charAt(i) }
    builder ++= ShortID.encode(alphabet, seconds).reverse.padTo(padLength, alphabet.head).reverse
    if( builder.length > overflowed ) builder += overflow
    if( count > 0 ) builder ++= ShortID.encode(alphabet, count)

    return builder.result
  }

}

object ShortID {

  case class State(seconds: Long, counter: Long)

  def apply(alphabet: String, overflow: Char, padLength: Int, version: Int, reduceTime: Long): ShortID =
    ShortID(alphabet, overflow, padLength, version, reduceTime, None)

  def apply(alphabet: String, overflow: Char, padLength: Int, version: Int, reduceTime: Long, nodeId: Int): ShortID =
    ShortID(alphabet, overflow, padLength, version, reduceTime, Some(nodeId))

  def encode(alphabet: String, value: Long): String = {
    def inner(alph: String, value: Long, tail: String = ""): String =
      if( value == 0 ) tail else inner(alph, value / alph.length, alph.charAt( (value % alph.length).toInt ) + tail)

    if( value == 0 ) alphabet.head.toString else inner(alphabet, value)
  }

}