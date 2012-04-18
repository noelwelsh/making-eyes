name := "calculator-service"

version := "0.1"

scalaVersion := "2.9.1"

resolvers ++= Seq(
  "ReportGrid"  at "http://nexus.reportgrid.com/content/repositories/public-snapshots",
  "Typesafe"    at "http://repo.typesafe.com/typesafe/releases/"
)

libraryDependencies ++= Seq(
  "com.reportgrid" %% "blueeyes-core"  % "0.6.0-SNAPSHOT",
  "com.reportgrid" %% "blueeyes-mongo" % "0.6.0-SNAPSHOT",
  "com.reportgrid" %% "blueeyes-json"  % "0.6.0-SNAPSHOT",
  "org.specs2"     %% "specs2"         % "1.8" % "test",
  "ch.qos.logback" % "logback-classic" % "1.0.0" % "runtime"
)
