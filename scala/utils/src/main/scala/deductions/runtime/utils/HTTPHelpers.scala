package deductions.runtime.utils

trait HTTPHelpers {
  val config: Configuration
  import config._

  def setTimeoutsFromConfig() = {
//    println(s">>>> setTimeoutsFromConfig: defaultReadTimeout $defaultReadTimeout, defaultConnectTimeout $defaultConnectTimeout")
    System.setProperty("sun.net.client.defaultReadTimeout", defaultReadTimeout.toString)
    System.setProperty("sun.net.client.defaultConnectTimeout", defaultConnectTimeout.toString)
  }
}