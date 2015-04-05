package controllers

import java.util.Hashtable

import scala.collection.mutable

import com.typesafe.config.ConfigFactory

import javax.naming.Context
import javax.naming.NamingEnumeration
import javax.naming.NamingException
import javax.naming.directory.SearchControls
import javax.naming.directory.SearchResult
import javax.naming.ldap.InitialLdapContext
import play.api.Logger

/**
 * @author vasiliy
 */

object LdapUtils {
  val conf = ConfigFactory.load()

  /**
   * Getting ldap connection.
   */
  private def getConnection(login: String = conf.getString("ldap.login"),
                            passw: String = conf.getString("ldap.passw")): Option[InitialLdapContext] = {
    val env = new Hashtable[String, String]()
    env.put(Context.PROVIDER_URL, conf.getString("ldap.url"))
    env.put(Context.SECURITY_PRINCIPAL, login)
    env.put(Context.SECURITY_CREDENTIALS, passw)
    env.put(Context.SECURITY_AUTHENTICATION, "simple")
    env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory")
    try
      Some(new InitialLdapContext(env, null))
    catch {
      case e: NamingException =>
        Logger.error(e.printStackTrace().toString())
        None
    }
  }

  private def getUsersData: Option[NamingEnumeration[SearchResult]] = getConnection() match {
    case Some(connection) => Some(connection.search(conf.getString("ldap.users-root"), conf.getString("ldap.filter"),
      new SearchControls(SearchControls.SUBTREE_SCOPE, 0, 0, null, false, false)))
    case None => None
  }

  /**
   * Usefulness is determined by the unique attribute values.
   * If the unique small (e.g. 1), it is likely that the attribute is not interesting.
   */
  def usefulAttributesFinder: Map[String, mutable.Map[ValueAndMarkOfAssignment, Set[String]]] = getUsersData match {
    case Some(enumeration) =>
      var map = mutable.Map.empty[String, mutable.Map[ValueAndMarkOfAssignment, Set[String]]]
      while (enumeration.hasMoreElements()) {
        val user = enumeration.next().getAttributes().getAll
        var userName = ""
        while (user.hasMoreElements()) {
          val attr = user.next()
          val key = attr.getID
          val values = attr.getAll
          var valueMap = map.getOrElse(key, mutable.Map.empty)
          while (values.hasMoreElements()) {
            val v = values.next().toString
            if (key == "cn") userName = v
            valueMap.get(new ValueAndMarkOfAssignment(v)) match {
              case Some(list) => {
                val tmp = (new ValueAndMarkOfAssignment(v, false) -> list)
                valueMap -= new ValueAndMarkOfAssignment(v)
                valueMap += tmp
              }
              case None => valueMap += (new ValueAndMarkOfAssignment(v, false) -> Set.empty)
            }
          }
          map += (key -> valueMap)
        }
        for (item <- map)
          for (it <- item._2)
            if (!it._1.isUserAssigned) {
              val set = it._2 + userName
              map.get(item._1).get += (new ValueAndMarkOfAssignment(it._1.value) -> set)
              it._1.isUserAssigned = true
            }
      }
      map.toMap
    case None => Map.empty
  }


  //Map[String, mutable.Map[ValueAndMarkOfAssignment, Set[String]]]
  //List[(String, Set[String])]
  //List((name, Biris)

  /* 
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
   *   def splittIntoPairs(arg: List[(String, Set[String])]): List[(String, String)] =
    (for (item <- arg) yield Set(item._1) zipAll (item._2, item._1, "")).flatten
   */

  def main(args: Array[String]): Unit = {
    print(LdapUtils.usefulAttributesFinder)
  }

}

class ValueAndMarkOfAssignment(val value: String, var isUserAssigned: Boolean = true) {
  override def equals(o: Any) = o match {
    case that: ValueAndMarkOfAssignment => that.value.equals(this.value)
    case _                              => false
  }

  override def toString() = value

  override def hashCode() = value.hashCode()
}