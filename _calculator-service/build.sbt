name := "calculator-service"

version := "0.1"

scalaVersion := "2.9.1"

resolvers ++= Seq(
  "Sonatype"    at "http://nexus.scala-tools.org/content/repositories/public"
)

libraryDependencies ++= Seq(
  "com.reportgrid"          %% "blueeyes"         % "0.5.0-SNAPSHOT" % "compile"
)


