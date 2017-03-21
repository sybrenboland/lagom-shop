package demo.impl.basket

import akka.Done
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventTag, PersistentEntity}
import com.lightbend.lagom.scaladsl.playjson.JsonSerializer
import demo.api.basket.Basket
import demo.api.catalogue.Item
import play.api.libs.json.{Format, Json}

object BasketEntityFormats {
  implicit val addItemFormat: Format[AddItem] = Json.format
  implicit val removeItemFormat: Format[RemoveItem] = Json.format
  implicit val getBasketFormat: Format[GetBasket.type] = JsonSerializer.emptySingletonFormat(GetBasket)
  implicit val getTotalFormat: Format[GetTotal.type] = JsonSerializer.emptySingletonFormat(GetTotal)
  implicit val clearAllFormat: Format[ClearAll.type] = JsonSerializer.emptySingletonFormat(ClearAll)
  implicit val basketClearedFormat: Format[BasketCleared.type] = JsonSerializer.emptySingletonFormat(BasketCleared)
  implicit val itemAddedFormat: Format[ItemAdded] = Json.format
  implicit val itemRemovedFormat: Format[ItemRemoved] = Json.format
  implicit val basketEntityStateFormat: Format[BasketEntityState] = Json.format
  implicit val placeOrderFormat: Format[PlaceOrder.type] = JsonSerializer.emptySingletonFormat(PlaceOrder)
  implicit val orderPlacedFormat: Format[OrderPlaced] = Json.format
}

trait BasketEntityCommand

case class AddItem(item: Item) extends BasketEntityCommand with ReplyType[Done]

case class RemoveItem(item: Item) extends BasketEntityCommand with ReplyType[Done]

case object GetBasket extends BasketEntityCommand with ReplyType[Basket]

case object GetTotal extends BasketEntityCommand with ReplyType[Int]

case object ClearAll extends BasketEntityCommand with ReplyType[Done]

case object PlaceOrder extends BasketEntityCommand with ReplyType[Done]

case class BasketEntityState(currentBasket: Basket, ordered: Boolean)

object BasketEntityState {
  val initial = BasketEntityState(Basket(Seq(), 0), ordered = false)
}

sealed trait BasketEntityEvent extends AggregateEvent[BasketEntityEvent] {
  override def aggregateTag = BasketEntityEvent.Tag
}

object BasketEntityEvent {
  val Tag = AggregateEventTag.sharded[BasketEntityEvent](4)
}

case class ItemAdded(item: Item) extends BasketEntityEvent

case class ItemRemoved(item: Item) extends BasketEntityEvent

case object BasketCleared extends BasketEntityEvent

case class OrderPlaced(basketId: String, basket: Basket) extends BasketEntityEvent

final class BasketEntity extends PersistentEntity {
  override type Command = BasketEntityCommand
  override type Event = BasketEntityEvent
  override type State = BasketEntityState

  override def initialState = BasketEntityState.initial

  override def behavior: Behavior = {
    Actions()
      .onCommand[AddItem, Done] {
        case (AddItem(item), ctx, state) => {
          if (state.ordered) {
            ctx.invalidCommand("The order has been placed and cannot be modified")
            ctx.done
          } else {
            ctx.thenPersist(ItemAdded(item))(_ => ctx.reply(Done))
          }
        }
      }
      .onCommand[RemoveItem, Done] {
        case (RemoveItem(item), ctx, state) => {
          if (state.ordered) {
            ctx.invalidCommand("The order has been placed and cannot be modified")
            ctx.done
          } else if (!state.currentBasket.items.contains(item)) {
            ctx.invalidCommand("The basket does not contain this item")
            ctx.done
          } else {
            ctx.thenPersist(ItemRemoved(item))(_ => ctx.reply(Done))
          }
        }
      }
      .onCommand[ClearAll.type, Done] {
        case (ClearAll, ctx, state) =>
          if (state.ordered) {
            ctx.invalidCommand("The order has been placed and cannot be modified")
            ctx.done
          } else {
            ctx.thenPersist(BasketCleared)(_ => ctx.reply(Done))
          }
      }
      .onCommand[PlaceOrder.type, Done] {
        case (PlaceOrder, ctx, state) => {
          if (state.currentBasket.items.isEmpty) {
            ctx.invalidCommand("Can't place an order for an empty basket")
            ctx.done
          } else if (state.ordered) {
            ctx.invalidCommand("The order has been placed and cannot be modified")
            ctx.done
          } else {
            ctx.thenPersist(OrderPlaced(entityId, state.currentBasket))(_ => ctx.reply(Done))
          }
        }
      }

      .onReadOnlyCommand[GetBasket.type, Basket] {
        case (GetBasket, ctx, state) => ctx.reply(state.currentBasket)
      }
      .onReadOnlyCommand[GetTotal.type, Int] {
        case (GetTotal, ctx, state) => ctx.reply(state.currentBasket.total)
      }

      .onEvent {
        case (ItemAdded(item), state) => {
          val newItems = state.currentBasket.items :+ item
          state.copy(Basket(newItems, newItems.map(_.price).sum))
        }
        case (ItemRemoved(item), state) => {
          val parts = state.currentBasket.items.partition(currentItem => currentItem == item)
          val newItems = parts._1.drop(1) ++ parts._2
          state.copy(Basket(newItems, newItems.map(_.price).sum))
        }
        case (BasketCleared, state) => {
          BasketEntityState.initial
        }
        case (OrderPlaced(_, _), state) => {
          state.copy(ordered = true)
        }
      }
  }
}
