package demo.api.basket

import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}
import com.lightbend.lagom.scaladsl.api.transport.Method


trait StockService extends Service {
  def addStock(itemId: String): ServiceCall[Int, NotUsed]

  override def descriptor = {
    import Method._
    import Service._

    named("stock").withCalls(
      restCall(POST, "/stock/:itemId/stock", addStock _)
    )
  }
}
