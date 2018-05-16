import sbt._
import sbt.Keys._
import scala.sys.process.Process

import Path.rebase

object FrontEnd extends AutoPlugin {

  object autoImport {
    val frontendDev = taskKey[Unit]("Build front-end in dev")
    val frontendFiles = taskKey[Seq[(File, String)]]("Front-end files")
  }

  import autoImport._

  override def trigger = allRequirements

  override def projectSettings = Seq[Setting[_]](
    frontendDev := {
      val s = streams.value
      s.log.info("Preparing front-end for dev (grunt wiredep)")
      Process("grunt" :: "wiredep" :: Nil, baseDirectory.value / "ui") ! s.log
      ()
    },

    frontendFiles := {
      val s = streams.value
      s.log.info("Preparing front-end for prod ...")
      s.log.info("yarn --ignore-engines")
      Process("yarn" :: "--ignore-engines" :: Nil, baseDirectory.value / "ui") ! s.log
      s.log.info("grunt build")
      Process("grunt" :: "build" :: Nil, baseDirectory.value / "ui") ! s.log
      val dir = baseDirectory.value / "ui" / "dist"
      (dir.**(AllPassFilter)) pair rebase(dir, "ui")
    })
}