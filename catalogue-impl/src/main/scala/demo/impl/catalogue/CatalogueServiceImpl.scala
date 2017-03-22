package demo.impl.catalogue

import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.InvalidCommandException
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import demo.api.catalogue.{CatalogueService, ExtraTransportExceptions, Item}

import scala.concurrent.ExecutionContext

class CatalogueServiceImpl(persistentEntities: PersistentEntityRegistry)(implicit ec: ExecutionContext)
  extends CatalogueService with ExtraTransportExceptions {

  override def getItems(catalogueId: String): ServiceCall[NotUsed, Seq[Item]] = ServiceCall { item =>
    persistentEntities.refFor[CatalogueEntity](catalogueId).ask(GetItems)
  }

  override def addItem(catalogueId: String): ServiceCall[Item, NotUsed] = ServiceCall { item =>
      persistentEntities.refFor[CatalogueEntity](catalogueId).ask(AddItem(item))
        .map(_ => NotUsed)
  }

  override def removeItem(catalogueId: String): ServiceCall[Item, NotUsed] = ServiceCall { item =>
    persistentEntities.refFor[CatalogueEntity](catalogueId).ask(RemoveItem(item))
      .map(_ => NotUsed)
      .recoverWith {
        case e: InvalidCommandException => throw BadRequest(e.message)
      }
  }
}


