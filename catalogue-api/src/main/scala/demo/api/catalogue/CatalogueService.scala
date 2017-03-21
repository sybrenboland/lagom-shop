package demo.api.catalogue

import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}
import com.lightbend.lagom.scaladsl.api.transport.Method
import play.api.libs.json.{Format, Json}

case class Item(name: String, price: Int)
object Item {
  implicit val itemFormat: Format[Item] = Json.format
}

trait CatalogueService extends Service {
  def getItems(catalogueId: String): ServiceCall[NotUsed, Seq[Item]]
  def addItem(catalogueId: String): ServiceCall[Item, NotUsed]
  def removeItem(catalogueId: String): ServiceCall[Item, NotUsed]

  override def descriptor = {
    import Method._
    import Service._

    named("stock").withCalls(
      restCall(GET, "/catalogue/:catalogueId/items", getItems _),
      restCall(POST, "/catalogue/:catalogueId/item", addItem _),
      restCall(DELETE, "/catalogue/:catalogueId/item", removeItem _)
    )
  }
}
