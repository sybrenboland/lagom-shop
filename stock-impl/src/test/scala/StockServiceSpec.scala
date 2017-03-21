import akka.NotUsed
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import demo.api.basket.StockService
import demo.impl.stock.{StockApplication, StockSerializerRegistry}
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}

class StockServiceSpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll  {
  lazy val service = ServiceTest.startServer(ServiceTest.defaultSetup.withCassandra(true)) { ctx =>
    new StockApplication(ctx) with LocalServiceLocator {
      override def jsonSerializerRegistry: JsonSerializerRegistry = StockSerializerRegistry
    }
  }

  override protected def beforeAll() = service
  override protected def afterAll() = service.stop()


  val client = service.serviceClient.implement[StockService]

  "Stock Service" should {
    "add stock for an item" in {
      client.addStock("item").invoke(1).flatMap { response =>
        response should ===(NotUsed)
      }
    }
  }
}
