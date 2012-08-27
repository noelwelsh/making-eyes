---
layout: page
title: Setting up a BlueEyes Project
---

Setting up a BlueEyes project using SBT just requires adding ReportGrid's repository to your resolvers, and then creating a dependency on the three BlueEyes packages. The relevant lines are:

{% highlight scala %}
resolvers ++= Seq(
  "Sonatype" at "http://oss.sonatype.org/content/repositories/public",
  "Typesafe" at "http://repo.typesafe.com/typesafe/releases/"
)

libraryDependencies ++= Seq(
  "com.github.jdegoes" %% "blueeyes-core"  % "0.6.0",
  "com.github.jdegoes" %% "blueeyes-mongo" % "0.6.0",
  "com.github.jdegoes" %% "blueeyes-json"  % "0.6.0",
  "ch.qos.logback" % "logback-classic" % "1.0.0" % "runtime"
)
{% endhighlight scala %}
