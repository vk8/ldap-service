package controllers

import java.io.File
import java.io.PrintWriter
import java.util.Hashtable

import scala.annotation.migration
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

object AdUtils {
  val conf = ConfigFactory.load()

  def getLdap(login: String = conf.getString("ldap.login"),
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

  def getLdapData: Option[NamingEnumeration[SearchResult]] = getLdap() match {
    case Some(context) => Some(context.search(conf.getString("ldap.root"), conf.getString("ldap.filter"),
      new SearchControls(SearchControls.SUBTREE_SCOPE, 0, 0, null, false, false)))
    case None => None
  }

  def usefulAttributesFinder: List[(String, Set[String])] = getLdapData match {
    case Some(enumeration) =>
      var map = mutable.Map.empty[String, Set[String]]
      while (enumeration.hasMoreElements()) {
        val user = enumeration.next().getAttributes().getAll
        while (user.hasMoreElements()) {
          val attr = user.next()
          val key = attr.getID
          val value = attr.get.toString
          val set = map.getOrElse(key, Set.empty) + value
          map += (key -> set)
        }
      }
      map.toList.sortWith((x, y) => x._2.size < y._2.size)
    case None => List.empty
  }

  def printAttributesToFile(attributes: List[(String, Set[String])]) = {
    val pw = new PrintWriter(new File("helloList.txt"))
    pw.write(attributes.toString())
    pw.close
  }

  def main(args: Array[String]) {
    printAttributesToFile(usefulAttributesFinder)
  }

}