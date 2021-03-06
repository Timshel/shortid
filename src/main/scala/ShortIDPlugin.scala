package io.github.timshel

import play.api._
import play.api.Play.current

class ShortIDPlugin(app: Application) extends Plugin {
  var shortId: Option[ShortID] = None

  override def onStart() = {
    val conf = app.configuration

    Logger.debug("ShortIDPlugin start")

    ( for {
        alphabet    <- conf.getString("application.shortId.alphabet")
        overflow    <- conf.getString("application.shortId.overflow")
        padLength   <- conf.getInt("application.shortId.padLength")
        reduceTime  <- conf.getLong("application.shortId.reduceTime")
        version     <- conf.getInt("application.shortId.version").filter( _ < alphabet.length )
        nodeId      <- conf.getInt("application.shortId.nodeId").filter( _ < alphabet.length )
      } yield shortId = Some( ShortID(alphabet, overflow.head, padLength, version, reduceTime, nodeId) )
    ).getOrElse {
      Logger.error("Invalid ShortId configuration")
      sys.exit(1)
    }
  }

}

object ShortIDPlugin {
  def generate(): String = {
    ( for {
        shortIDPlugin <- Play.application.plugin[ShortIDPlugin]
        shortId       <- shortIDPlugin.shortId
      } yield shortId.generate
    ).getOrElse( throw new RuntimeException("ShortIDPlugin is not loaded") )
  }
}
