package deductions.runtime.utils

import org.w3.banana.RDF
import java.net.URLEncoder
import org.w3.banana.RDFOps
import org.w3.banana.RDFPrefix
import org.w3.banana.RDFSPrefix
import org.w3.banana._
  import java.net.{ URI => jURI }
  import scala.util.Try
  import scala.util.Success
  import scala.util.Failure

trait RDFPrefixes[Rdf <: RDF] {

  implicit val ops: RDFOps[Rdf]
  import ops._

  val commonSchemes = List("http", "https", "url")
  val prefixes = List(
    RDFPrefix[Rdf], RDFSPrefix[Rdf],
    XSDPrefix[Rdf],
    DCPrefix[Rdf],
    DCTPrefix[Rdf],
    FOAFPrefix[Rdf],
    LDPPrefix[Rdf],
    IANALinkPrefix[Rdf],
    WebACLPrefix[Rdf],
    CertPrefix[Rdf],
    OWLPrefix[Rdf],
    Prefix[Rdf]("dc", "http://purl.org/dc/elements/1.1/"),
    Prefix[Rdf]("schema", "http://schema.org/"),
    Prefix[Rdf]("doap", "http://usefulinc.com/ns/doap#"),
    Prefix[Rdf]("sioc", "http://rdfs.org/sioc/ns#")
    )
  val prefixesMap: Map[String, Rdf#URI] =
    prefixes.map{ pf => pf.prefixName -> URI(pf.prefixIri) }.toMap
  
  /** expand possibly Prefixed URI (like foaf:name),
   *  and then output Some(URI("http://xmlns.com/foaf/0.1/name")),
   *  or output None */
  def expand(possiblyPrefixedURI: String): Option[Rdf#URI] = {
    val uri_string = possiblyPrefixedURI // URLEncoder.encode(possiblyPrefixedURI, "UTF-8")
    val tr = Try{
    val uri = new jURI(uri_string)
    if (uri.isAbsolute() && !commonSchemes.contains(uri.getScheme)) {
      // then it's possibly a Prefixed URI like foaf:name
      val prefix = uri.getScheme
      val prefixAsURI = prefixesMap.get(prefix)
      prefixAsURI match {
        case Some(prefixIri) =>
          Some( URI( fromUri(prefixIri) + possiblyPrefixedURI.substring( prefix.length() + 1 ) ))
        case None => None
      }
    } else None
    }
    tr match {
      case Success(r) => r
      case Failure(e) => None
    }
  }
}
