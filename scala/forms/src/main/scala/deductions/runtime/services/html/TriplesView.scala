package deductions.runtime.services.html

import deductions.runtime.abstract_syntax.{FormSyntaxFactory, UserTraceability}
import deductions.runtime.html.{HtmlGeneratorInterface, logger}
import deductions.runtime.semlogs.TimeSeries
import deductions.runtime.sparql_cache.RDFCacheAlgo
import deductions.runtime.utils.{Configuration, HTTPrequest, RDFPrefixesInterface, Timer}
import deductions.runtime.views.TableFromListListRDFNodes
import org.apache.log4j.Logger
import org.w3.banana.RDF

import scala.util.{Failure, Success, Try}
import scala.xml.NodeSeq

/**
 * Form for a subject URI with existing triples;
 *  a facade that blends:
 *  - the RDF cache [[deductions.runtime.sparql_cache.RDFCacheAlgo]],
 *  - the generic Form Factory [[deductions.runtime.abstract_syntax.FormSyntaxFactory]],
 *  - the HTML renderer deductions.runtime.html.Form2HTML;
 *  transactional
 *
 * Was named TableView because originally it was an HTML table.
 */
trait TriplesViewModule[Rdf <: RDF, DATASET]
    extends RDFCacheAlgo[Rdf, DATASET]
    with FormSyntaxFactory[Rdf, DATASET]
    with TimeSeries[Rdf, DATASET]
    with TableFromListListRDFNodes[Rdf]
    with UserTraceability[Rdf, DATASET]
    with Timer {

  val config: Configuration
  val htmlGenerator: HtmlGeneratorInterface[Rdf#Node, Rdf#URI] // Form2HTMLBanana[Rdf]
  import htmlGenerator._
  import ops._

  /**
   * wrapper for htmlForm that shows Failure's ;
   *  non TRANSACTIONAL
   */
  def htmlFormElemRaw(uri: String, unionGraph: Rdf#Graph=allNamedGraph, hrefPrefix: String = config.hrefDisplayPrefix, blankNode: String = "",
    editable: Boolean = false,
    actionURI: String = "/save",
    lang: String = "en",
    graphURI: String = "",
    actionURI2: String = "/save",
    formGroup: String = fromUri(nullURI),
    formuri: String="",
    database: String = "TDB",
    request: HTTPrequest = HTTPrequest(),
    inputGraph: Try[Rdf#Graph] = Success(emptyGraph)
  ): ( NodeSeq, FormSyntax ) = {
    htmlFormRawTry(uri, unionGraph, hrefPrefix, blankNode, editable, actionURI,
      lang, graphURI, actionURI2, URI(formGroup), formuri, database, request, inputGraph) match {
        case Success(e) => e
        case Failure(e) => ( <p>htmlFormElem: Exception occured: { e }</p>, FormSyntax(nullURI, Seq() ) )
      }
  }
  
  /**
   * wrapper for htmlForm that shows Failure's ;
   *  TRANSACTIONAL
   */
  def htmlFormElem(uri: String, hrefPrefix: String = config.hrefDisplayPrefix, blankNode: String = "",
    editable: Boolean = false,
    actionURI: String = "/save",
    lang: String = "en",
    graphURI: String = "",
    actionURI2: String = "/save",
    formGroup: String = fromUri(nullURI)): NodeSeq = {

    htmlForm(uri, hrefPrefix, blankNode, editable, actionURI,
      lang, graphURI, actionURI2, URI(formGroup)) match {
        case Success(e) => e._1
        case Failure(e) => <p>htmlFormElem: Exception occured: { e }</p>
      }
  }

  /**
   * wrapper for htmlForm, but generates Just Fields; also shows Failure's;
   * see deductions.runtime.html.Form2HTML.generateHTMLJustFields() .
   * TRANSACTIONAL
   */
  def htmlFormElemJustFields(uri: String, hrefPrefix: String = config.hrefDisplayPrefix, blankNode: String = "",
    editable: Boolean = false,
    lang0: String = "en",
    graphURI: String = "",
    formGroup: String = fromUri(nullURI),
    formuri: String="" )
    : NodeSeq = {

    implicit val lang = lang0

    // TODO for comprehension like in htmlForm()

    val (graphURIActual, _) = doRetrieveURI(uri, blankNode, graphURI)
    val htmlFormTry = rdfStore.rw( dataset, {
      implicit val graph: Rdf#Graph = allNamedGraph
      val ops1 = ops
      val config1 = config
      val form = createAbstractForm(
          uri, editable, blankNode,
          URI(formGroup), formuri )

        generateHTMLJustFields(form,
          hrefPrefix, editable, graphURIActual, request=HTTPrequest() )
    })
    htmlFormTry match {
      case Success(e) => e
      case Failure(e) => <p class="error">htmlFormElemJustFields: Exception occured: {
        e.printStackTrace()
      }</p>
      throw e
    }
  }

  /** create HTML Form or view From SPARQL CONSTRUCT query */
  def createHTMLFormFromSPARQL(query: String,
                               editable: Boolean = false,
                               formuri: String = ""): NodeSeq = {
    val formSyntax = createFormFromSPARQL(query, editable, formuri)
    generateHTML(formSyntax, request=HTTPrequest(), hrefPrefix = config.hrefDisplayPrefix)
  }


  /**
   * create a form for given URI with background knowledge in RDFStoreObject.store;
   *  by default user inputs will be saved in named graph uri, except if given graphURI argument;
   *  NON TRANSACTIONAL;
   *  
   *  Note: first try to retrieve from Internet at given URI,
   *  then eventually save in TDB,
   *  then read again <uri> ?P ?O.	from TDB, in any named graph,
   *  to catch 1) triples downloaded from URI, 2) triples preloaded,
   *  3) triples coming from user edits
   *  @param blankNode if "true" given uri is a blanknode
   *  
   */
  private def htmlFormRawTry(uri: String, unionGraph: Rdf#Graph = allNamedGraph,
                          hrefPrefix: String = config.hrefDisplayPrefix, blankNode: String = "",
                          editable: Boolean = false,
                          actionURI: String = "/save",
                          lang: String = "en",
                          graphURI: String = "",
                          actionURI2: String = "/save",
                          formGroup: Rdf#URI = nullURI,
                          formuri: String="",
                          database: String = "TDB",
                          request: HTTPrequest = HTTPrequest(),
                          inputGraph: Try[Rdf#Graph] = Success(emptyGraph)
		  ): Try[( NodeSeq, FormSyntax)] = {

    logger.debug(
        s">>>> htmlFormRawTry: getMetadataAboutSubject($uri) = ${makeHtmlTable( getMetadataAboutSubject(URI(uri)) )}")
    logger.debug(
      s"htmlFormRawTry dataset $dataset, graphURI <$graphURI>")

    val graphURIActual = if (graphURI == "") uri else graphURI
    Success(graf2form(unionGraph, uri, hrefPrefix, blankNode, editable,
      actionURI, lang, graphURIActual, actionURI2, formGroup, formuri, request))
  }
  
  /**
   * create a form for given URI with background knowledge in RDFStoreObject.store;
   *  by default user inputs will be saved in named graph uri, except if given graphURI argument;
   *  @param blankNode if "true" given uri is a blanknode
   *  TRANSACTIONAL
   */
  private def htmlForm(uri: String, hrefPrefix: String = config.hrefDisplayPrefix, blankNode: String = "",
    editable: Boolean = false,
    actionURI: String = "/save",
    lang: String = "en",
    graphURI: String = "",
    actionURI2: String = "/save",
    formGroup: Rdf#URI = nullURI)
    : Try[( NodeSeq, FormSyntax )] = {

    println( s"htmlForm dataset $dataset" )

    for {
      (graphURIActual, tryGraph) <- Try { time("doRetrieveURI", doRetrieveURI(uri, blankNode, graphURI)) }

      // TODO find another way of reporting download failures: 
      //      graphDownloaded <- tryGraph
      
      form <- rdfStore.rw( dataset, {
        graf2form(allNamedGraph, uri, hrefPrefix, blankNode, editable,
          actionURI, lang, graphURIActual, actionURI2, formGroup)
      })
    } yield form
  }

  /** Retrieve URI from Internet or triples cache;
   * with transaction
   *
   *  @return Actual graph URI: given graph URI or if not specified given uri
   */
  private def doRetrieveURI(uri: String, blankNode: String, graphURI: String): (String, Try[Rdf#Graph]) = {
    val tryGraph = if (blankNode != "true") {
      val res = retrieveURI(makeUri(uri), dataset)
      Logger.getRootLogger().info(s"After retrieveURI(makeUri($uri), dataset) isSuccess ${res.isSuccess}")

//      println("Search duplicate graph rooted at blank node: size " + getTriples(res.get).size )
//      manageBlankNodesReload(res.getOrElse(emptyGraph), URI(uri), dataset: DATASET)
      res
    } else Success(emptyGraph)
    val graphURIActual = if (graphURI == "") uri else graphURI
    (graphURIActual, tryGraph)
  }

  /**
   * create a form for given URI resource (instance) with background knowledge
   *  in given graph
   *  TODO non blocking
   */
  private def graf2form(graphe: Rdf#Graph, uri: String,
    hrefPrefix: String = config.hrefDisplayPrefix, blankNode: String = "",
    editable: Boolean = false,
    actionURI: String = "/save",
    lang0: String = "en", graphURI: String,
    actionURI2: String = "/save",
    formGroup: Rdf#URI = nullURI,
    formuri: String="",
    request: HTTPrequest = HTTPrequest()
		  ) : ( NodeSeq , FormSyntax ) = {

    implicit val graph: Rdf#Graph = graphe
    implicit val lang = lang0

    try {
      // DANGEROUS with large database !
      //    	println(s"TableViewModule.graf2form(graph: graph first triple: ${getTriples(graph).headOption}, graphURI <$graphURI>")
      //    	println(s"TableViewModule.graf2form(graph: graph first triple: ${ops.graphSize(graph)}, graphURI <$graphURI>")
      logger.debug(s"TableViewModule.graf2form(graph: graph : ${graph}, graphURI <$graphURI>")
    } catch {
      case t: Throwable => "graf2form : getting graph.size" + t.getLocalizedMessage()
    }
    val form = time("createAbstractForm",
      createAbstractForm(uri, editable, blankNode, formGroup, formuri))

    // TODO call addUserInfoOnTriples() in package deductions.runtime.abstract_syntax
    val formWithInfo = addUserInfoOnTriples(form)

//    val htmlForm =
//      generateHTML(form, hrefPrefix, editable, actionURI, graphURI,
//        actionURI2, lang, request)
    val htmlForm =
      generateHTML(formWithInfo, hrefPrefix, editable, actionURI, graphURI,
        actionURI2, lang, request)
    ( htmlForm, formWithInfo )
  }

  private def createAbstractForm(
      uri: String, editable: Boolean,
      blankNode: String, formGroup: Rdf#URI, formuri: String="")
    (implicit graph: Rdf#Graph, lang: String )
    :  FormSyntax = {
    val subjectNode = if (blankNode == "true")
      /* Jena TDB specific:
           * Jena supports "concrete bnodes" in SPARQL syntax as pseudo URIs in the "_" URI scheme
           * (it's an illegal name for a URI scheme) */
      BNode(uri)
    else URI(uri)

    createForm(subjectNode, editable, formGroup, formuri)
  }

//  private def graf2formString(graph1: Rdf#Graph, uri: String, graphURI: String): String = {
//    graf2form(graph1, uri, graphURI = graphURI)._1.toString
//  }

}
