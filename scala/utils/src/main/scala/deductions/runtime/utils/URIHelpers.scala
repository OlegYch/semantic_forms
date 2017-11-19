package deductions.runtime.utils

import java.net.URI

trait URIHelpers {

  def isAbsoluteURI(uri: String) = {
    try {
      val u = new java.net.URI(uri)
      u.isAbsolute()
    } catch {
      case t: Throwable => false
    }
  }

  def isCorrectURI(uri: String): Boolean = {
    try {
      val u = new java.net.URI(uri)
      true
    } catch {
      case t: Throwable => false
    }
  }

  /** like the Banana function */
  def lastSegment(uri: String): String = {
    try {
      val path = new URI(uri).getPath
      val i = path.lastIndexOf('/')
      if (i < 0)
        path
      else
        path.substring(i + 1, path.length)
    } catch {
      case t: Throwable => uri
    }
  }

  def isDownloadableURL(url: String) = {
      url.startsWith("http") ||
      url.startsWith("ftp:") ||
      url.startsWith("file:") ||
      url.startsWith("https:")
  }
}