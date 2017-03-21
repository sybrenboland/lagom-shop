import akka.NotUsed
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import demo.api.catalogue.{CatalogueService, ExtraTransportExceptions, Item}
import demo.impl.catalogue.{CatalogueApplication, CatalogueSerializerRegistry}
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}

class CatalogueServiceSpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll with ExtraTransportExceptions {
  lazy val service = ServiceTest.startServer(ServiceTest.defaultSetup.withCassandra(true)) { ctx =>
    new CatalogueApplication(ctx) with LocalServiceLocator {
      override def jsonSerializerRegistry: JsonSerializerRegistry = CatalogueSerializerRegistry
    }
  }

  override protected def beforeAll() = service
  override protected def afterAll() = service.stop()
  val client: CatalogueService = service.serviceClient.implement[CatalogueService]

  "Catalogue Service" should {
    "add an item" in {
      client.addItem("catalogueAdd").invoke(Item("Apple", 50)).flatMap { response =>
        response should ===(NotUsed)

         client.getItems("catalogueAdd").invoke().map { getItemsResponse =>
           getItemsResponse should ===(Seq(Item("Apple", 50)))
         }
      }
    }

    "remove an item" in {
      client.addItem("catalogueRemove").invoke(Item("Apple", 50)).flatMap { response =>
        response should ===(NotUsed)

        client.removeItem("catalogueRemove").invoke(Item("Apple", 50)).flatMap { response =>
          response should ===(NotUsed)

          client.getItems("catalogueRemove").invoke().map { getItemsResponse =>
            getItemsResponse should ===(Seq())
          }
        }
      }
    }

//    "return an error if an item is added that is already in the catalogue" in {
//      recoverToSucceededIf[BadRequest] {
//        for (a <- client.addItem("basketAddError").invoke(Item("Apple", 50));
//             b <- client.addItem("basketAddError").invoke(Item("Apple", 50)))
//          yield b should ===(NotUsed)
//      }
//    }
//
//    "return an error if an item is removed that is not in the catalogue" in {
//      recoverToSucceededIf[BadRequest] {
//        for (a <- client.removeItem("basketRemoveEmpty").invoke(Item("Apple", 50)))
//          yield a should ===(NotUsed)
//      }
//    }
  }
}
