package deductions.runtime.abstract_syntax

import org.w3.banana.RDF
import org.w3.banana.RDFOps
import org.w3.banana.URIOps
import org.w3.banana.RDFStore
import deductions.runtime.jena.RDFStoreObject
import org.w3.banana.OWLPrefix
import UnfilledFormFactory._
import org.w3.banana.RDFPrefix
import deductions.runtime.utils.RDFHelpers
import org.w3.banana.RDFStore
import scala.util.Try
import org.apache.log4j.Logger
import org.w3.banana.Prefix

/**
 * Factory for populating Form from graph
 */
class FormConfigurationFactory[Rdf <: RDF](graph: Rdf#Graph)(implicit ops: RDFOps[Rdf],
    uriOps: URIOps[Rdf]) {

  import ops._

  val formPrefix: Prefix[Rdf] = Prefix("form", FormSyntaxFactory.formVocabPrefix)
  val gr = graph
  val rdfh = new RDFHelpers[Rdf] { val graph = gr }
  import rdfh._
  import ops._

  /**
   * lookup for form:showProperties (ordered list of fields) in Form Configuration within RDF graph in this class
   *  usable for unfilled and filled Forms
   */
  def lookPropertieslistFormInConfiguration(classs: Rdf#URI): (Seq[Rdf#URI], Rdf#Node) = {
    val formSpecOption = lookFormSpecInConfiguration(classs)
    formSpecOption match {
      case None => (Seq(), URI(""))
      case Some(formConfiguration) =>
        val propertiesList = propertiesListFromFormConfiguration(formConfiguration)
        (propertiesList, formConfiguration)
    }
  }

  def propertiesListFromFormConfiguration(formConfiguration: Rdf#Node): Seq[Rdf#URI] = {
    //    val props = objectsQuery(formConfiguration, formPrefix("showProperties"))
    val props = getObjects(graph, formConfiguration, formPrefix("showProperties"))
    for (p <- props) { println("showProperties " + p) }
    val p = props.headOption
    val propertiesList = rdfh.nodeSeqToURISeq(rdfh.rdfListToSeq(p))
    propertiesList
  }

  /** lookup Form Spec from OWL class in In Configuration */
  private def lookFormSpecInConfiguration(classs: Rdf#URI): Option[Rdf#Node] = {
    val forms = getSubjects(graph, formPrefix("classDomain"), classs)
    val debugString = new StringBuilder; Logger.getRootLogger().debug("forms " + forms.addString(debugString, "; "))
    val formSpecOption = forms.flatMap {
      form => ops.foldNode(form)(uri => Some(uri), bn => Some(bn), lit => None)
    }.headOption
    Logger.getRootLogger().warn(s"WARNING: several form specs for $classs")
    Logger.getRootLogger().debug("formNodeOption " + formSpecOption)
    formSpecOption
  }

  /**
   * return e g :  <topic_interest>
   *  in :
   *  <pre>
   *  &lt;topic_interest&gt; :fieldAppliesToForm &lt;personForm> ;
   *   :fieldAppliesToProperty foaf:topic_interest ;
   *   :widgetClass form:DBPediaLookup .
   *  <pre>
   *  that is, query:
   *  ?S form:fieldAppliesToProperty prop .
   */
  def lookFieldSpecInConfiguration(
    //      classs: Rdf#URI, 
    prop: Rdf#URI) = {
    find(graph, ANY, formPrefix("fieldAppliesToProperty"), prop).toSeq
  }
}