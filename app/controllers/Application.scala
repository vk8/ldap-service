package controllers

import play.api.libs.functional.syntax.functionalCanBuildApplicative
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.functional.syntax.unlift
import play.api.libs.json.JsError
import play.api.libs.json.JsPath
import play.api.libs.json.Json
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import play.api.libs.json.Reads
import play.api.libs.json.Writes
import play.api.mvc.Action
import play.api.mvc.BodyParsers
import play.api.mvc.Controller

object Application extends Controller {

  def index = Action {
    Ok(views.html.main())
  }

  implicit val attributeWrites = new Writes[(String, Set[String])] {
    def writes(attribute: (String, Set[String])) = Json.obj(
      "name" -> attribute._1,
      "values" -> attribute._2)
  }

  def allSortedLdapAttributes = Action {
    Ok(Json.toJson(LdapUtils.usefulAttributesFinder))
  }

  def attributesTableMergeOptions = Action {
    Ok(Json.parse("""[{
        "index" : 1,
        "field" : "name",
        "rowspan" : 4 },{
        "index" : 5,
        "field" : "name",
        "rowspan" : 4 }]"""))
  }

  def listPlaces = Action {
    val json = Json.toJson(Place.list)
    Ok(Json.prettyPrint(json))
  }

  //  curl --include ^
  //  --request POST ^
  //  --header "Content-type: application/json" ^
  //  --data "{\"name\":\"Nuthanger Farm\",\"location\":{\"lat\" : 51.244031,\"long\" : -1.263224}}" ^
  //  http://localhost:9000/places
  def savePlace = Action(BodyParsers.parse.json) { request =>
    val placeResult = request.body.validate[Place]
    placeResult.fold(
      errors => {
        BadRequest(Json.obj("status" -> "KO", "message" -> JsError.toFlatJson(errors)))
      },
      place => {
        Place.save(place)
        Ok(Json.obj("status" -> "OK", "message" -> ("Place '" + place.name + "' saved.")))
      })
  }

  def main(args: Array[String]): Unit = {

  }

  case class Location(lat: Double, long: Double)

  case class Place(name: String, location: Location)

  object Place {

    var list: List[Place] = List(
      Place("Sandleford", Location(51.377797, -1.318965)),
      Place("Watership Down", Location(51.235685, -1.309197)))

    def save(place: Place) = {
      list = list ::: List(place)
    }
  }

  implicit val locationWrites: Writes[Location] = (
    (JsPath \ "lat").write[Double] and
    (JsPath \ "long").write[Double])(unlift(Location.unapply))

  implicit val placeWrites: Writes[Place] = (
    (JsPath \ "name").write[String] and
    (JsPath \ "location").write[Location])(unlift(Place.unapply))

  implicit val locationReads: Reads[Location] = (
    (JsPath \ "lat").read[Double] and
    (JsPath \ "long").read[Double])(Location.apply _)

  implicit val placeReads: Reads[Place] = (
    (JsPath \ "name").read[String] and
    (JsPath \ "location").read[Location])(Place.apply _)
}




