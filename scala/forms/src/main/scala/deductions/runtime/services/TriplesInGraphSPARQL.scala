package deductions.runtime.services

import org.w3.banana.RDF

/** Show named graphs */
trait TriplesInGraphSPARQL[Rdf <: RDF, DATASET]
    extends ParameterizedSPARQL[Rdf, DATASET] {

  private implicit val searchStringQueryMaker = new SPARQLQueryMaker[Rdf] {
    override def makeQueryString( graphURI: String*): String =
      /* LIMIT 500 because of computed labels Graph urn:/semforms/labelsGraphUri/ 
       * in the case of a large database , e.g. dbPedia mirror */
    s"""
         |SELECT DISTINCT ?thing ?p ?o WHERE {
         |  graph <${graphURI(0)}> {
         |    ?thing ?p ?o .
         |  }
         |}
         |LIMIT 500""".stripMargin
         
    override def variables = Seq("thing", "p", "o")
    
//    override def columnsForURI( node: Rdf#Node, label: String): NodeSeq =
//      Text("test")    
  }

  def showTriplesInGraph(graphURI: String, lang: String = "") //  : Future[Elem]
  = {
//		  println(s"showTriplesInGraph: hrefDisplayPrefix ${config.hrefDisplayPrefix}")
    <p>
      <p> Triples in Graph &lt;{ graphURI }> </p>
      { search2(graphURI, config.hrefDisplayPrefix, lang) }
    </p>
  }

}