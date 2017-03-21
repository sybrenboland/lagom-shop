package demo.impl.stock

import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.broker.kafka.LagomKafkaComponents
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}
import com.lightbend.lagom.scaladsl.server._
import com.softwaremill.macwire._
import demo.api.basket.StockService
import demo.impl.basket._
import play.api.libs.ws.ahc.AhcWSComponents

import scala.collection.immutable.Seq

abstract class StockApplication(ctx: LagomApplicationContext) extends LagomApplication(ctx)
  with AhcWSComponents
  with LagomKafkaComponents
  with CassandraPersistenceComponents {
  override def lagomServer: LagomServer = LagomServer.forServices {
    bindService[StockService].to(wire[StockServiceImpl])
  }


  persistentEntityRegistry.register(wire[StockEntity])
}

class StockApplicationLoader extends LagomApplicationLoader {
  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new StockApplication(context) with LagomDevModeComponents {
      override def jsonSerializerRegistry: JsonSerializerRegistry = StockSerializerRegistry
    }

  override def load(context: LagomApplicationContext): LagomApplication = new StockApplication(context) {
    override def serviceLocator: ServiceLocator = NoServiceLocator
    override def jsonSerializerRegistry: JsonSerializerRegistry = StockSerializerRegistry
  }
}

object StockSerializerRegistry extends JsonSerializerRegistry {
  import StockEntityFormats._
  override def serializers: Seq[JsonSerializer[_]] = Seq (
    JsonSerializer[AddStock],
    JsonSerializer[NewStockAmount],
    JsonSerializer[StockEntityState]
  )
}