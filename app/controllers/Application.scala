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

  implicit val attributeWrites = new Writes[(String, String)] {
    def writes(attribute: (String, String)) = Json.obj(
      "name" -> attribute._1,
      "values" -> attribute._2)
  }

  /*
   * !! Correct sorting output is provided by size-sorting in LdapUtils.usefulAttributesFinder   
   */
  def allSortedLdapAttributes = Action {
    Ok(Json.toJson(splittIntoPairs(LdapUtils.usefulAttributesFinder)))
  }

  def attributesTableMergeOptions = Action {
    Ok(xxx)
  }

  /*
   * Example explaining the transformation.
   * 
   * Before:
   * 
   * List((name,  Set(Boris, Nick, Petr, Ivan)), 
   *      (color, Set(red, green))) 
   * 
   * After zipAll:
   * 
   * List( Set( (name, Biris), (name, Nick), (name, Petr), (name, Ivan)), 
   *       Set( (color, red), (color, green)))
   *       
   * Result after flatten: 
   * 
   * List((name, Biris), (name, Nick), (name, Petr), (name, Ivan), (color, red), (color, green))
   *    
   */
  def splittIntoPairs(arg: List[(String, Set[String])]): List[(String, String)] =
    (for (item <- arg) yield Set(item._1) zipAll (item._2, item._1, "")).flatten

  def main(args: Array[String]): Unit = {}

  def xxx(): String = {
    var str = "["
    var count = 0
    for (item <- LdapUtils.usefulAttributesFinder) {
      str += "{\"index\" : " + count + ","
      str += "\"field\" : \"name\","
      str += "\"rowspan\" : " + item._2.size + "},"
      count += item._2.size
    }
    str = str.substring(0, str.length() - 1)
    str + "]"
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




