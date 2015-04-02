package controllers

import play.api.libs.json.JsArray
import play.api.libs.json.JsNull
import play.api.libs.json.JsNumber
import play.api.libs.json.JsObject
import play.api.libs.json.JsString
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.mvc.Action
import play.api.mvc.Controller
import play.twirl.api.Html
import play.api.libs.json.Writes

object Application extends Controller {

  def index = Action {
    Ok(views.html.main())
  }

  implicit val attributeWrites = new Writes[(String, Set[String])] {
    def writes(attribute: (String, Set[String])) = Json.obj(
      //"name" -> attribute._1,
      "values" -> attribute._2)
  }

  def allSortedLdapAttributes = Action {
    //val json = 
    //Ok(Json.toJson(LdapUtils.usefulAttributesFinder)) List[(String, Set[String])]
    Ok(Json.prettyPrint(Json.toJson(LdapUtils.usefulAttributesFinder)))
  }

  def main(args: Array[String]): Unit = {
    val json: JsValue = Json.parse("""
      {
        "name" : "Watership Down",
        "location" : {
          "lat" : 51.235685,
          "long" : -1.309197
        },
        "residents" : [ {
          "name" : "Fiver",
          "age" : 4,
          "role" : null
        }, {
          "name" : "Bigwig",
          "age" : 6,
          "role" : "Owsla"
        } ]
      }
      """)
    val json2: JsValue = JsObject(Seq(
      "name" -> JsString("Watership Down"),
      "location" -> JsObject(Seq("lat" -> JsNumber(51.235685), "long" -> JsNumber(-1.309197))),
      "residents" -> JsArray(Seq(
        JsObject(Seq(
          "name" -> JsString("Fiver"),
          "age" -> JsNumber(4),
          "role" -> JsNull)),
        JsObject(Seq(
          "name" -> JsString("Bigwig"),
          "age" -> JsNumber(6),
          "role" -> JsString("Owsla")))))))

    val json3: JsValue = Json.obj(
      "name" -> "Watership Down",
      "location" -> Json.obj("lat" -> 51.235685, "long" -> -1.309197),
      "residents" -> Json.arr(
        Json.obj(
          "name" -> "Fiver",
          "age" -> 4,
          "role" -> JsNull),
        Json.obj(
          "name" -> "Bigwig",
          "age" -> 6,
          "role" -> "Owsla")))

    print(json)

    // basic types
    val jsonString = Json.toJson("Fiver")
    val jsonNumber = Json.toJson(4)
    val jsonBoolean = Json.toJson(false)

    // collections of basic types
    val jsonArrayOfInts = Json.toJson(Seq(1, 2, 3, 4))
    val jsonArrayOfStrings = Json.toJson(List("Fiver", "Bigwig"))
  }
}




