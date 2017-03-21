package demo.impl.basket

import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.InvalidCommandException
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import demo.api.basket.StockService
import demo.api.catalogue.ExtraTransportExceptions

import scala.concurrent.ExecutionContext

class StockServiceImpl(persistentEntities: PersistentEntityRegistry)(implicit ec: ExecutionContext)
  extends StockService with ExtraTransportExceptions {

  override def addStock(itemId: String): ServiceCall[Int, NotUsed] = ServiceCall { amount =>
    persistentEntities.refFor[StockEntity](itemId).ask(AddStock(amount))
      .map(_ => NotUsed)
      .recoverWith {
        case e: InvalidCommandException => throw BadRequest(e.message)
      }
  }
}


