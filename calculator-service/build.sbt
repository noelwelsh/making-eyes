name := "calculator-service"

version := "0.1"

scalaVersion := "2.9.1"

resolvers ++= Seq(
  "Sonatype"    at "http://nexus.scala-tools.org/content/repositories/public",
  "Scala Tools" at "http://scala-tools.org/repo-snapshots/",
  "JBoss"       at "http://repository.jboss.org/nexus/content/groups/public/",
  "Akka"        at "http://akka.io/repository/",
  "GuiceyFruit" at "http://guiceyfruit.googlecode.com/svn/repo/releases/"
)

libraryDependencies ++= Seq(
  "com.reportgrid"          %% "blueeyes"         % "0.4.24" % "compile",
  "org.scala-tools.testing" %  "specs_2.9.0-1"    % "1.6.8" % "test",
  "net.lag"                 %  "configgy"         % "2.0.0" % "compile" intransitive()
)

ivyXML :=
<dependencies>
  <dependency org="com.reportgrid" name="blueeyes_2.9.1" rev="0.4.24">
    <exclude module="configgy"/>
  </dependency>
</dependencies>

