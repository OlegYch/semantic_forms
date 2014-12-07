package deductions.runtime.html

import org.w3.banana.RDFModule
import org.w3.banana.jena.Jena
import deductions.runtime.sparql_cache.RDFCacheJena
import deductions.runtime.jena.RDFStoreObject
import scala.xml.Elem
import deductions.runtime.abstract_syntax.UnfilledFormFactory
import org.w3.banana.RDFOpsModule
import scala.util.Success
import scala.util.Failure
import scala.concurrent.Future
import scala.util.Try
import scala.concurrent.Await
import scala.concurrent.duration._
import deductions.runtime.utils.MonadicHelpers

trait CreationForm extends RDFOpsModule
  with Form2HTML[Jena#Node, Jena#URI]
  with RDFCacheJena // TODO depend on generic Rdf
{
  import Ops._
  val nullURI : Rdf#URI = Ops.URI( "" )
  var actionURI = "/save"
  
  /** create an XHTML input form from a class URI */
  def create( uri:String, lang:String="en" ) : Future[Elem] = {
    import scala.concurrent.ExecutionContext.Implicits.global
//        val r0 = rdfStore.r(dataset, {
//    //    store.readTransaction {
//          val allNamedGraphs : Future[Rdf#Graph] = rdfStore.getGraph(makeUri("urn:x-arq:UnionGraph"))
//          val factory = new UnfilledFormFactory[Rdf](allNamedGraphs, preferedLanguage=lang)
//          val form = factory.createFormFromClass(URI(uri))
//          println(form)
//          val htmlForm = generateHTML(form, hrefPrefix="", editable=true, actionURI )
//          htmlForm
//        })
    val dataset =  RDFStoreObject.dataset
    val r = rdfStore.r(dataset, {
      //    store.readTransaction {
      for (
        allNamedGraphs <- rdfStore.getGraph(makeUri("urn:x-arq:UnionGraph"))
      ) yield {
        val factory = new UnfilledFormFactory[Rdf](allNamedGraphs, preferedLanguage = lang)
        val form = factory.createFormFromClass(URI(uri))
        println(form)
        val htmlForm = generateHTML(form, hrefPrefix = "", editable = true, actionURI)
        htmlForm
      }
    })
    MonadicHelpers.tryToFutureFlat(r)
  }

  def createElem( uri:String, lang:String="en" ) : Elem = {
	  Await.result(
			  create( uri, lang),
			  5 seconds )
  }

}