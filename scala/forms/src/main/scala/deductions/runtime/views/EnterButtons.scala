package deductions.runtime.views

import deductions.runtime.utils.I18NMessages
import scala.xml.NodeSeq
import deductions.runtime.utils.RDFPrefixes
import deductions.runtime.jena.ImplementationSettings
import deductions.runtime.services.DefaultConfiguration
import org.w3.banana.jena.JenaModule

/**
 * Buttons for loading/display/edit, search, and create;
 * this the default HTML UI before the form
 */
trait EnterButtons {

  private lazy val prefixes = new ImplementationSettings.RDFModule with RDFPrefixes[ImplementationSettings.Rdf] with DefaultConfiguration {}
  import prefixes._

  protected def messageI18N(key: String)(implicit lang: String) = I18NMessages.get(key, lang)

  def enterURItoDownloadAndDisplay()(implicit lang: String = "en") = {
    <div class="row">
      <div class="col-md-12">
        <form role="form" action="/display">
          <div class="form-group">
            <label class="col-md-2 control-label" for="Display">{ messageI18N("Display") }</label>
            <div class="col-md-6">
              <input class="form-control" type="text" name="displayuri" list="start_uris" dropzone="copy string:text/plain"/>
              <datalist id="start_uris">
                <option label="J.M. Vanel FOAF profile"> http://jmvanel.free.fr/jmv.rdf#me </option>
                <option label="Paris dbpedia.org"> http://dbpedia.org/resource/Paris </option>
                <option label="H. Story FOAF profile"> http://bblfish.net/people/henry/card#me </option>
              </datalist>
            </div>
            <div class="col-md-4">
              <input class="btn btn-primary" type="submit" name="Display" value={ messageI18N("Display") }/>
              <input class="btn btn-primary" type="submit" name="Edit" value={ messageI18N("Edit") }/>
             <label class="checkbox-inline"> <input type="checkbox" name="tabs" value="true" />Group field</label>
            </div>
            <input type="submit" style="display:none"/>
          </div>
        </form>
      </div>
    </div>
  }

  def enterSearchTerm()(implicit lang: String = "en") = {
    <div class="row">
      <div class="col-md-12">
        <form role="form" action="/wordsearch">
          <div class="form-group">
            <label class="col-md-2 control-label" for="q" title="Search URI whose value (object triple) matches (Lucene search) or (known RDF class)">
              { messageI18N("String_to_search") }
            </label>
            <div class="col-md-6">
              <input class="form-control" type="text" name="q" placeholder={
                messageI18N("Search_placeholder")
              } dropzone="copy"/>
              <input class="form-control" type="text" name="clas" placeholder={ messageI18N("Class_placeholder") }/>
            </div>
            <div class="col-md-4">
              <input class="btn btn-primary" type="submit" value={ messageI18N("Search") }/>
            </div>
            <input type="submit" style="display:none"/>
          </div>
        </form>
      </div>
    </div>
  }

  def enterClassForCreatingInstance()(implicit lang: String = "en") =
    <div class="row">
      <div class="col-md-12">
        <form role="form" action="/create">
          <div class="form-group">
            <label class="col-md-2 control-label" for="uri">{ messageI18N("Create_instance_of") }</label>
            <div class="col-md-6">
              <input class="form-control" type="text" name="uri" placeholder={
                messageI18N("Paste_ontology")
              } dropzone="copy"></input>
              <select class="form-control selectable" type="text" name="uri" list="class_uris">
                <optgroup label="Assemblée Virtuelle">
                  <optgroup label="Acteur">
                    <option label="foaf:Person long"> http://www.virtual-assembly.org/ontologies/1.0/forms#PersonForm </option>
                    <option label="foaf:Group">                      { foaf.Group } </option>
                    <option label="foaf:Organization">               { foaf.Organization } </option>
                  </optgroup>
                  <optgroup label="Idée">
                    <option label="av:Theme"> { prefixAVontology }Theme </option>
                    <option label="Proposition"> { prefixAVontology }Proposition </option>
                    <option label="Commentaire"> { prefixAVontology }Comment </option>
                  </optgroup>
                  <optgroup label="Projet">
                    <option label="foaf:Project">                    { foaf.Project } </option>
                    <option label="av:InitiativeOrMission"> { prefixAVontology }InitiativeOrMission </option>
                    <option label="av:Event"> { prefixAVontology }Event </option>
                    <option label="Tâche"> { prefixesMap2("tm")("Task") }</option>
                  </optgroup>
                  <optgroup label="Ressource">
                    <option label="Bien ou service"> { prefixesMap2("gr")("Offering") }</option>
                    Logiciel
                    <option label="Desc. Of A Software (DOAS)">      { prefixesMap2("doas")("Description") } </option>
                    Compétence
                    <option label="cco:Skill">                       { prefixesMap2("cco")("Skill") } </option>
                    <option label="Document">                        { foaf.Document } </option>
                    <option label="Lieu">                            { prefixesMap2("schema")("Place") } </option>
                    <option label="Oeuvre">                          { prefixesMap2("schema")("CreativeWork") } </option>
                    <option label="Ressource financière">            { prefixesMap2("pair")("FinancialResource") } </option>
                    <option label="Ressource naturelle">             { prefixesMap2("pair")("NaturalResources") } </option>
                    <option label="av:Resource"> { prefixAVontology }Resource </option>
                  </optgroup>
                </optgroup>
                <optgroup label={ messageI18N("Other_vocabs") }>
                  { suggestedClassesForCreation }
                </optgroup>
              </select>
            </div>
            <div class="col-md-4">
              <input class="btn btn-primary" style="position: relative; top: 18px;" type="submit" value={ messageI18N("Create") }/>
            </div>
            <input type="submit" style="display:none"/>
          </div>
        </form>
      </div>
    </div>

  /** NOTE currently the label is NOT displayed by Firefox :( , only by Chrome */
  def suggestedClassesForCreation: NodeSeq = {
    <option label="foaf:Person short" selected="selected"> { forms("personForm") } </option>
    <option label="doap:Project">                    { prefixesMap2("doap")("Project") } </option>
    <option label="sioc:Thread">                     { sioc("Thread") } </option>
    <option label="sioc:Post">                       { sioc("Post") } </option>
    <option label="ical:Vevent">                     { prefixesMap2("ical")("Vevent") } </option>
    <option label="owl:Class">                       { prefixesMap2("owl")("Class") } </option>
    <option label="owl:DatatypeProperty">            { prefixesMap2("owl")("DatatypeProperty") } </option>
    <option label="owl:ObjectProperty">              { prefixesMap2("owl")("ObjectProperty") } </option>
    <option label="bioc:Planting">                   { prefixesMap2("bioc")("Planting") } </option>

  }
}