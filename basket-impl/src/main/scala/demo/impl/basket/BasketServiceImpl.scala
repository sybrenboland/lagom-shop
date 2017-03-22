package demo.impl.basket

import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.broker.TopicProducer
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.InvalidCommandException
import com.lightbend.lagom.scaladsl.persistence.{EventStreamElement, PersistentEntityRegistry}
import demo.api
import demo.api.basket
import demo.api.basket.{Basket, BasketService}
import demo.api.catalogue.{ExtraTransportExceptions, Item}
import play.api.Logger

import scala.collection.immutable
import scala.concurrent.ExecutionContext

class BasketServiceImpl(persistentEntities: PersistentEntityRegistry)(implicit ec: ExecutionContext)
  extends BasketService with ExtraTransportExceptions{
  override def getBasket(basketId: String): ServiceCall[NotUsed, Basket] = ServiceCall { req =>
    persistentEntities.refFor[BasketEntity](basketId).ask(GetBasket)
  }

  override def addItem(basketId: String): ServiceCall[Item, NotUsed] = ServiceCall { item => {
    Logger.warn("Item being added: " + item.name)

    persistentEntities.refFor[BasketEntity](basketId).ask(AddItem(item))
      .map(_ => NotUsed)
      .recoverWith {
        case e: InvalidCommandException => throw BadRequest(e.message)
      }
  }
  }

  override def removeItem(basketId: String): ServiceCall[Item, NotUsed] = ServiceCall { item =>
    persistentEntities.refFor[BasketEntity](basketId).ask(RemoveItem(item))
      .map(_ => NotUsed)
      .recoverWith {
        case e: InvalidCommandException => throw BadRequest(e.message)
      }
  }

  override def getTotal(basketId: String): ServiceCall[NotUsed, Int] = ServiceCall { req =>
    persistentEntities.refFor[BasketEntity](basketId).ask(GetTotal)
  }

  override def clearAll(basketId: String): ServiceCall[NotUsed, NotUsed] = ServiceCall { req =>
    persistentEntities.refFor[BasketEntity](basketId).ask(ClearAll)
      .map(_ => NotUsed)
      .recoverWith {
        case e: InvalidCommandException => throw BadRequest(e.message)
      }
  }

  override def placeOrder(basketId: String): ServiceCall[NotUsed, NotUsed] = ServiceCall { req =>
    persistentEntities.refFor[BasketEntity](basketId).ask(PlaceOrder)
      .map(_ => NotUsed)
      .recoverWith {
        case e: InvalidCommandException => throw BadRequest(e.message)
      }
  }

  override def placedOrders: Topic[basket.OrderPlaced] =
      TopicProducer.taggedStreamWithOffset(BasketEntityEvent.Tag.allTags.to[immutable.Seq]) { (tag, offset) =>
        persistentEntities.eventStream(tag, offset).collect {
          case EventStreamElement(t, OrderPlaced(id, basket), o)  => {
            api.basket.OrderPlaced(id, basket) -> o
          }
        }
    }
}
