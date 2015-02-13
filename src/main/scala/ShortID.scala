package io.github.timshel

import java.util.Date

import scala.collection.mutable.StringBuilder
import scala.concurrent.stm.Ref

/**
 * alphabet: The characters used for the encoding
 * limit: The ceiling value after which the seconds will overflow (2^30 give us ~30years since reduceTime and an encoding length of 5chars with base64).
 * version: Don't change unless you change the algo or reduceTime (Int < alphabet.length).
 * reduceTime: Ignore all milliseconds before a certain time to reduce the size of the date without sacrificing uniqueness.
 *             To regenerate `DateTime.now()` and bump the version. Always bump the version!
 * nodeId: If you are using multiple servers use this to make each instance has a unique value (Int < alphabet.length).
 */
case class ShortID(alphabet: String, limit: Long, version: Int, reduceTime: Long, nodeId: Int){
  import ShortID._

  val state     = Ref.apply( State(reduceTime, 0) ).single
  val padLength = ShortID.padLength(limit, alphabet)

  def generate(): String = {
    val seconds = (new Date().getTime() - reduceTime) / 1000;
    val count   = state.transformAndGet {
      case s@State(p, count) if p == seconds => s.copy(counter = count + 1)
      case _ => State(seconds, 1)
    }.counter

    encode(seconds, count-1)
  }

  def encode(seconds: Long, count: Long): String = {
    val builder = StringBuilder.newBuilder

    builder + alphabet.charAt(version)
    builder + alphabet.charAt(nodeId)
    builder ++= ShortID.encode(alphabet, seconds).reverse.padTo(padLength, alphabet.head).reverse
    if( count > 0 ) builder ++= ShortID.encode(alphabet, count)

    return builder.result
  }

}

object ShortID {

  case class State(seconds: Long, counter: Long)

  def encode(alphabet: String, value: Long): String = {
    def inner(alph: String, value: Long, tail: String = ""): String =
      if( value == 0 ) tail else inner(alph, value / alph.length, alph.charAt( (value % alph.length).toInt ) + tail)

    if( value == 0 ) alphabet.head.toString else inner(alphabet, value)
  }

  def padLength(limit: Long, alphabet: String) =
    math.ceil(math.log(limit) / math.log(alphabet.length)).toInt

}