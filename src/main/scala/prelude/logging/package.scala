package prelude

import com.typesafe.scalalogging
import org.slf4j

package object logging extends LoggingImports

trait LoggingImports {

  //TODO: Really probably don't need a strict and lazy logger
  /** Extend to get a lazy `logger` val in scope */
  type LazyLogging = scalalogging.LazyLogging

  /** Or a strict `logger` */
  type StrictLogging = scalalogging.StrictLogging

  /** Or if you'd like to include the `Logger`  as a dependency*/
  def Logger(c: Class[_]): scalalogging.Logger = scalalogging.Logger(c)

  def Logger(s: String): scalalogging.Logger = scalalogging.Logger(slf4j.LoggerFactory.getLogger(s))
}
