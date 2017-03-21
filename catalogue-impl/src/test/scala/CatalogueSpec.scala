import akka.Done
import akka.actor.ActorSystem
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.InvalidCommandException
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import com.lightbend.lagom.scaladsl.testkit.PersistentEntityTestDriver
import demo.api.catalogue.Item
import demo.impl.catalogue._
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.Await
import scala.concurrent.duration._

class CatalogueSpec extends WordSpecLike with Matchers with BeforeAndAfterAll with TypeCheckedTripleEquals {
  val system = ActorSystem("PostSpec", JsonSerializerRegistry.actorSystemSetupFor(CatalogueSerializerRegistry))

  override protected def afterAll() {
    Await.ready(system.terminate(), 20.seconds)
  }

  "Catalogue" must {
    "Add item" in {
      val driver = new PersistentEntityTestDriver(system, new CatalogueEntity, "CatalogueAdd")
      val addItemOutcome = driver.run(AddItem(Item("Apple", 50)))
      addItemOutcome.events should ===(List(ItemAdded(Item("Apple", 50))))
      addItemOutcome.state.items should ===(Seq(Item("Apple", 50)))
      addItemOutcome.replies should ===(List(Done))
      addItemOutcome.issues should ===(Nil)

      val getItemsOutcome = driver.run(GetItems)
      getItemsOutcome.replies should contain (Seq(Item("Apple", 50)))
      getItemsOutcome.replies.size should ===(1)
    }

    "Remove an item" in {
      val driver = new PersistentEntityTestDriver(system, new CatalogueEntity, "CatalogueRemove")
      val addItemOutcome = driver.run(AddItem(Item("Apple", 50)))

      val removeOutcome = driver.run(RemoveItem(Item("Apple", 50)))
      removeOutcome.replies should ===(List(Done))

      val getItemsOutcome = driver.run(GetItems)
      getItemsOutcome.replies should contain(Seq())
    }

    "Return InvalidCommand if add item is added that is already in the catalogue" in {
      val driver = new PersistentEntityTestDriver(system, new CatalogueEntity, "CatalogueAddError")
      driver.run(AddItem(Item("Apple", 50)))
      val outcome = driver.run(AddItem(Item("Apple", 50)))
      outcome.replies should ===(List(InvalidCommandException("Item is already added")))
    }

    "Return InvalidCommand if add item is removed that is not in the catalogue" in {
      val driver = new PersistentEntityTestDriver(system, new CatalogueEntity, "CatalogueRemoveError")
      val outcome = driver.run(RemoveItem(Item("Apple", 50)))
      outcome.replies should ===(List(InvalidCommandException("Item is not present in catalogue")))
    }
  }
}
