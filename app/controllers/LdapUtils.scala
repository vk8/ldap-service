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
  def usefulAttributesFinder: Map[String, Set[String]] = getUsersData match {
    case Some(enumeration) =>
      var map = mutable.Map.empty[String, Set[String]]
      while (enumeration.hasMoreElements()) {
        val user = enumeration.next().getAttributes().getAll
        while (user.hasMoreElements()) {
          val attr = user.next()
          val key = attr.getID
          val values = attr.getAll
          var set = map.getOrElse(key, Set.empty)
          while (values.hasMoreElements())
            set += values.next().toString
          map += (key -> set)
        }
      }
      map.toMap
    case None => Map.empty
  }

  def main(args: Array[String]): Unit = {
    LdapUtils.usefulAttributesFinder
  }

}