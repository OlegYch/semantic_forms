package deductions.runtime.uri_classify

import org.scalatest.FunSuite
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Await
import scala.concurrent.duration._
 
class TestSemanticURIGuesser extends FunSuite {
  test("TestSemanticURIGuesser") {
    for (
      uri <- List(
        "http://jmvanel.free.fr/images/jmv_id.jpg"
//        ,"http://danbri.org/foaf.rdf#danbri"
//        ,"http://fcns.eu/people/andrei/card#me"
    		  )
    ) {
      val fut = SemanticURIGuesser.guessSemanticURIType(uri)
//      Thread.sleep( 6000 )
      Await.result(fut, 10000 millis)
      
      fut onSuccess{
        case t => println("SemanticURIType: "+t)
       	assert(true) // TODO
      }
      fut onFailure {
        case t => println("onFailure SemanticURIType: "+t)
       	fail()
      }
    }
  }
}