package deductions.runtime.html

import java.nio.file.{Files, Paths}

import deductions.runtime.abstract_syntax.FormSyntaxFactoryTest
import deductions.runtime.jena.{ImplementationSettings, RDFStoreLocalJena1Provider}
import deductions.runtime.services.html.Form2HTMLBanana
import deductions.runtime.utils.DefaultConfiguration
import org.apache.log4j.Logger
import org.junit.Assert
import org.scalatest.FunSuite

class Form2HTMLTest
    extends FunSuite
    with RDFStoreLocalJena1Provider
    with Form2HTMLBanana[ImplementationSettings.Rdf]
    with FormSyntaxFactoryTest[ImplementationSettings.Rdf, ImplementationSettings.DATASET] {
  val config = new DefaultConfiguration {
    override val useTextQuery = false
  }
  val logger = Logger.getRootLogger()

  import ops._
  println("Entering Form2HTMLTest")
  val nullURI1 = makeUri("")

  test("Test HTML") {
    val ops1 = ops
    val fh = this
    val form = createFormWithGivenProps()
    println("Form2HTMLTest: form")
    val xhtml = fh.generateHTML(form)
    Files.write(Paths.get("tmp.form.html"), xhtml.toString().getBytes);
    Assert.assertTrue(xhtml.toString().contains("Alexandre"))
  }
}
