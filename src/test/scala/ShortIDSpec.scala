package io.github.timshel

import org.joda.time.DateTime
import org.scalatest._

import scala.util.Random
import scala.collection.mutable.Map

class ShortIDSpec extends FlatSpec with Matchers {
  val alph  = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
  val overf = '_'

  it should "encode" in {
    val octal = "01234567"
    val hexa  = "0123456789ABCDEF"

    for( _ <- 1 to 1000000 ){
      val v = math.abs(Random.nextLong)
      assert(BigInt(ShortID.encode(octal, v), 8) == v)
    }

    for( _ <- 1 to 1000000 ){
      val v = math.abs(Random.nextLong)
      assert(BigInt(ShortID.encode(hexa, v), 16) == v)
    }
  }

  it should "overflow" in {
    val generator = ShortID(alph, overf, padLength = 5, 1, DateTime.now().getMillis)
    val overflow  = DateTime.now().plusYears(100).getMillis

    assert( !generator.encode(10000l, 0).contains(overf) )
    assert( generator.encode(overflow, 0).contains(overf) )
  }

  it should "encode with no collision" in {
    val reduce = DateTime.now().minusYears(10).getMillis
    val check = Map.empty[String, (Int, Int, Int, Int)]

    for( _ <- 1 to 1000000 ){
      val seed = (
        math.abs(Random.nextInt) % alph.length,
        math.abs(Random.nextInt) % alph.length,
        math.abs(Random.nextInt),
        math.abs(Random.nextInt)
      )
      val id   = ShortID(alph, overf, padLength = 5, seed._1, reduce, seed._2).encode(seed._3, seed._4)

      assert( check.get(id).fold(true)(_ == seed) )
      check.put(id, seed)
    }
  }

  it should "generate no duplicate" in {
    val generator = ShortID(alph, overf, padLength = 5, 12, DateTime.now().minusYears(10).getMillis, 1)
    val builder   = Set.newBuilder[String]

    for( _ <- 1 to 1000000 ){
      builder += generator.generate()
    }

    assert( builder.result.size == 1000000 )
  }

}