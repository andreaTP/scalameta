package scala.meta
package taxonomic

import org.scalameta.data._
import scala.{Seq => _}
import scala.collection.immutable.Seq

@data class Domain(artifacts: Artifact*) {
  override def toString = "Domain(" + artifacts.mkString(", ") + ")"
}
