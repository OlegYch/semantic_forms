package deductions.runtime.views

import java.net.URLEncoder

import deductions.runtime.core.FormModule
import deductions.runtime.html.BasicWidgets
import deductions.runtime.utils.RDFStoreLocalProvider
import deductions.runtime.utils.{Configuration, I18NMessages, RDFHelpers, RDFPrefixes}
import org.w3.banana.{OWLPrefix, PointedGraph, RDF}

import scala.xml.{NodeSeq, Text}

/** generic application: links on top of the form: Edit, Display, Download Links */
trait FormHeader[Rdf <: RDF, DATASET]
    extends FormModule[Rdf#Node, Rdf#URI]
    with RDFStoreLocalProvider[Rdf, DATASET]
    with RDFHelpers[Rdf]
    with BasicWidgets
    with RDFPrefixes[Rdf] {

//  self: ApplicationFacadeImpl[Rdf, _] =>

  val config: Configuration
  import config._
  import ops._

  /** title and links on top of the form: Edit, Display, Download Links */
  def titleEditDisplayDownloadLinksThumbnail(formSyntax: FormSyntax, lang: String,
      editable: Boolean = false)(implicit graph: Rdf#Graph): NodeSeq = {
    val uri = nodeToString(formSyntax.subject)
    implicit val _ = lang
    // show the button to change the current editable state
    val linkToShow =
      if (editable)
        hyperlinkForDisplayingURI(uri, lang)
      else
        hyperlinkForEditingURI(uri, lang)

    val expertLinks = (if (showExpertButtons) {
      Seq(makeBackLinkButton(uri),
        new Text("  "),
        makeDrawGraphLink(uri),
        makeNeighborhoodLink(uri))
    } else new Text(""))
    
    val expertLinksOWL = (if (showExpertButtons && isOWLURI(uri) ) {
      new Text("  ")
      makeDrawGraphLinkVOWL(uri)
    } else new Text(""))

    def downloadLink(syntax: String = "Turtle"): NodeSeq = {
      // col-xs-12
      <span class="sf-local-rdf-link">
        <a href={
          hrefDownloadPrefix +
            URLEncoder.encode(uri, "utf-8") +
            s"&syntax=$syntax"
        } title={ mess("Triples_tooltip") }>
          { mess("Triples") + " " + syntax}
        </a>
        -
      </span>
    }
    <div class="row">
        <div class="col-xs-12">
          <h3 id="subject">
            {
              formSyntax.title
            }
            <strong>
              { linkToShow }
              { expertLinks }
              { expertLinksOWL }
            </strong>
            {
              if (formSyntax.thumbnail.isDefined) {
                <a class="image-popup-vertical-fit" href={ formSyntax.thumbnail.get.toString() } title={ s"Image of ${formSyntax.title}: ${formSyntax.subject.toString()}" }>
                  <img src={ formSyntax.thumbnail.get.toString() } css="sf-thumbnail" height="40" alt={
                    s"Image of ${formSyntax.title}: ${formSyntax.subject.toString()}"
                  }/>
                </a>
              } else NodeSeq.Empty
            }
          </h3>
        </div>
      </div>
      <div class="row sf-links-row">
        <!--div class="col-md-6"-->
        <div class="col-xs-12 sf-local-rdf-link">
          {
            //          val message = if (uri.contains("/ldp/"))
            //            "Download local URI"
            //          else
            //            mess("Download_original_URI")
            //          <a href={ uri }>{ message }</a>
          }
        </div>
        { downloadLink() ++
//          Text(" - ") ++
          downloadLink("JSON-LD") ++
//          Text(" - ") ++
          downloadLink("RDF/XML")}
      </div>
  }

  def mess(m: String)(implicit lang: String) = I18NMessages.get(m, lang)

  private lazy val owl = OWLPrefix[Rdf]

  def isOWLURI(uri: String): Boolean = {
    val triples = find(allNamedGraph, URI(uri), ANY, ANY).toList
//    val v = URI(uri) / rdf.typ
    val pg = PointedGraph( URI(uri), allNamedGraph )
    val types = ( pg / rdf.typ ) . nodes . toList
    println( s"isOWLURI: $types" )
    types . contains( owl.Class) ||
    types . contains( owl.ObjectProperty) ||
    types . contains( owl.DatatypeProperty) ||
    types . contains( owl.Ontology)
  }

///** NON transactional */
//  private def labelForURI(uri: String, language: String)
//  (implicit graph: Rdf#Graph)
//    : String = {
//      instanceLabel(URI(uri), graph, language)
//  }
}
