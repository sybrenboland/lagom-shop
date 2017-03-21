package demo.impl.catalogue

import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.broker.kafka.LagomKafkaComponents
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}
import com.lightbend.lagom.scaladsl.server._
import com.softwaremill.macwire._
import demo.api.catalogue._
import play.api.libs.ws.ahc.AhcWSComponents

import scala.collection.immutable.Seq

abstract class CatalogueApplication(ctx: LagomApplicationContext) extends LagomApplication(ctx)
  with AhcWSComponents
  with LagomKafkaComponents
  with CassandraPersistenceComponents {
  override def lagomServer: LagomServer = LagomServer.forServices {
    bindService[CatalogueService].to(wire[CatalogueServiceImpl])
  }

  persistentEntityRegistry.register(wire[CatalogueEntity])
}

class CatalogueApplicationLoader extends LagomApplicationLoader {
  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new CatalogueApplication(context) with LagomDevModeComponents {
      override def jsonSerializerRegistry: JsonSerializerRegistry = CatalogueSerializerRegistry
    }

  override def load(context: LagomApplicationContext): LagomApplication = new CatalogueApplication(context) {
    override def serviceLocator: ServiceLocator = NoServiceLocator
    override def jsonSerializerRegistry: JsonSerializerRegistry = CatalogueSerializerRegistry
  }
}

object CatalogueSerializerRegistry extends JsonSerializerRegistry {
  import demo.impl.catalogue.CatalogueEntityFormats._
  override def serializers: Seq[JsonSerializer[_]] = Seq (
    JsonSerializer[GetItems.type],
    JsonSerializer[AddItem],
    JsonSerializer[RemoveItem],
    JsonSerializer[ItemAdded],
    JsonSerializer[ItemRemoved],
    JsonSerializer[CatalogueEntityState]
  )
}
