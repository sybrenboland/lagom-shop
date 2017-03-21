package demo.api.basket

import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.api.{Descriptor, Service, ServiceCall}
import com.lightbend.lagom.scaladsl.api.transport._
import demo.api.catalogue.ExtraTransportExceptions.CustomExceptionSerializer
import demo.api.catalogue.Item
import play.api.Environment
import play.api.libs.json.{Format, Json}

case class Basket(items: Seq[Item], total: Int)
object Basket {
  implicit val basketFormat: Format[Basket] = Json.format
}

case class OrderPlaced(basketId: String, basket: Basket)
object OrderPlaced {
  implicit val orderPlacedFormat: Format[OrderPlaced] = Json.format
}

trait BasketService extends Service {
  def getBasket(basketId: String): ServiceCall[NotUsed, Basket]
  def getTotal(basketId: String): ServiceCall[NotUsed, Int]
  def addItem(basketId: String): ServiceCall[Item, NotUsed]
  def removeItem(basketId: String): ServiceCall[Item, NotUsed]
  def clearAll(basketId: String): ServiceCall[NotUsed, NotUsed]
  def placeOrder(basketId: String): ServiceCall[NotUsed, NotUsed]
  def placedOrders: Topic[OrderPlaced]

  override def descriptor: Descriptor = {
    import Method._
    import Service._

    named("basket").withCalls(
      restCall(GET, "/basket/:basketId", getBasket _),
      restCall(GET, "/basket/:basketId/total", getTotal _),
      restCall(POST, "/basket/:basketId/item", addItem _),
      restCall(DELETE, "/basket/:basketId/item", removeItem _),
      restCall(DELETE, "/basket/:basketId/items", clearAll _),
      restCall(POST, "/basket/:basketId/order", placeOrder _)
    ).withTopics(
      topic("placed-orders", placedOrders)
    ).withExceptionSerializer(new CustomExceptionSerializer(Environment.simple()))
  }
}
