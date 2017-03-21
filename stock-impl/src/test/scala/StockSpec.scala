import akka.Done
import akka.actor.ActorSystem
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import com.lightbend.lagom.scaladsl.testkit.PersistentEntityTestDriver
import demo.impl.basket.{AddStock, NewStockAmount, StockEntity}
import demo.impl.stock.StockSerializerRegistry
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.Await
import scala.concurrent.duration._

class StockSpec extends WordSpecLike with Matchers with BeforeAndAfterAll with TypeCheckedTripleEquals {
  val system = ActorSystem("PostSpec", JsonSerializerRegistry.actorSystemSetupFor(StockSerializerRegistry))

  override protected def afterAll() {
    Await.ready(system.terminate(), 20.seconds)
  }

  "Stock" must {
    "Add stock" in {
      val driver = new PersistentEntityTestDriver(system, new StockEntity, "StockAdd")
      val addItemOutcome = driver.run(AddStock(1))

      addItemOutcome.events should ===(List(NewStockAmount(1)))
      addItemOutcome.replies should ===(List(Done))
    }
  }
}
