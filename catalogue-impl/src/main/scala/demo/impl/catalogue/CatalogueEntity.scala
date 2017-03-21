package demo.impl.catalogue

import akka.Done
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventTag, PersistentEntity}
import com.lightbend.lagom.scaladsl.playjson.JsonSerializer
import demo.api.catalogue.Item
import play.api.libs.json.{Format, Json}

object CatalogueEntityFormats {
  implicit val getItemsFormat: Format[GetItems.type] = JsonSerializer.emptySingletonFormat(GetItems)
  implicit val addItemFormat: Format[AddItem] = Json.format
  implicit val removeItemFormat: Format[RemoveItem] = Json.format
  implicit val itemAddedFormat: Format[ItemAdded] = Json.format
  implicit val itemRemovedFormat: Format[ItemRemoved] = Json.format
  implicit val catalogueEnityStateFormat: Format[CatalogueEntityState] = Json.format
}

trait CatalogueEntityCommand
case object GetItems extends CatalogueEntityCommand with ReplyType[Seq[Item]]
case class AddItem(item: Item) extends CatalogueEntityCommand with ReplyType[Done]
case class RemoveItem(item: Item) extends CatalogueEntityCommand with ReplyType[Done]

case class CatalogueEntityState(items: Seq[Item])
object CatalogueEntityState {
  val initial = CatalogueEntityState(Seq())
}

sealed trait CatalogueEntityEvent extends AggregateEvent[CatalogueEntityEvent] {
  override def aggregateTag = CatalogueEntityEvent.Tag
}
object CatalogueEntityEvent {
  val Tag = AggregateEventTag.sharded[CatalogueEntityEvent](4)
}
case class ItemAdded(item: Item) extends CatalogueEntityEvent
case class ItemRemoved(item: Item) extends CatalogueEntityEvent


final class CatalogueEntity extends PersistentEntity {
  override type Command = CatalogueEntityCommand
  override type Event = CatalogueEntityEvent
  override type State = CatalogueEntityState

  override def initialState = CatalogueEntityState.initial

  override def behavior: Behavior = {
    Actions()
      .onCommand[AddItem, Done] {
        case (AddItem(item), ctx, state) =>
          if(state.items.contains(item)) {
            ctx.invalidCommand("Item is already added")
            ctx.done
          } else {
            ctx.thenPersist(ItemAdded(item))(_ => ctx.reply(Done))
          }
      }
      .onCommand[RemoveItem, Done] {
        case (RemoveItem(item), ctx, state) =>
          if(!state.items.contains(item)) {
            ctx.invalidCommand("Item is not present in catalogue")
            ctx.done
          } else {
            ctx.thenPersist(ItemRemoved(item))(_ => ctx.reply(Done))
          }
      }

      .onReadOnlyCommand[GetItems.type, Seq[Item]] {
        case(GetItems, ctx, state) => ctx.reply(state.items)
      }

      .onEvent {
        case (ItemAdded(item), state) =>
          val newItems = state.items :+ item
          state.copy(newItems)
        case (ItemRemoved(item), state) =>
          val parts = state.items.partition(currentItem => currentItem == item)
          val newItems = parts._1.drop(1) ++ parts._2
          state.copy(newItems)
      }
    }
  }