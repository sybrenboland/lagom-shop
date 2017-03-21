package demo.impl.basket

import akka.Done
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventTag, PersistentEntity}
import play.api.libs.json.{Format, Json}

object StockEntityFormats {
  implicit val addStockFormat: Format[AddStock] = Json.format
  implicit val newStockAmountFormat: Format[NewStockAmount] = Json.format
  implicit val stockEntityStateFormat: Format[StockEntityState] = Json.format
}

trait StockEntityCommand
case class AddStock(amount: Int) extends StockEntityCommand with ReplyType[Done]

case class StockEntityState(amount: Int)
object StockEntityState {
  val initial = StockEntityState(0)
}

sealed trait StockEntityEvent extends AggregateEvent[StockEntityEvent] {
  override def aggregateTag = StockEntityEvent.Tag
}
object StockEntityEvent {
  val Tag = AggregateEventTag.sharded[StockEntityEvent](4)
}
case class NewStockAmount(amount: Int) extends StockEntityEvent


final class StockEntity extends PersistentEntity {
  override type Command = StockEntityCommand
  override type Event = StockEntityEvent
  override type State = StockEntityState

  override def initialState = StockEntityState.initial

  override def behavior: Behavior = {
    Actions()
      .onCommand[AddStock, Done] {
      case (AddStock(item), ctx, state) => {
          ctx.thenPersist(NewStockAmount(item))(_ => ctx.reply(Done))
      }
    }
  }
}