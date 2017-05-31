import com.github.agourlay.cornichon.CornichonFeature

class CreateData extends CornichonFeature {

  def feature = Feature("CreateData") {

    Scenario("CreateData") {

      When I get("http://localhost:9000/create-data")
      Then assert status.is(200)
      And assert body.path("subject").isPresent
      And assert body.path("title").isPresent
      And assert body.path("formURI").isPresent
      And assert body.path("formLabel").isPresent
      And assert body.path("fields").asArray.isNotEmpty
    }
  }
}