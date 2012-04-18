---
layout: page
title: Setting up a BlueEyes Project
---

Setting up a BlueEyes project using SBT just requires adding ReportGrid's repository to your resolvers, and then creating a dependency on the three BlueEyes packages. The relevant lines are:

{% highlight scala %}
resolvers ++= Seq(
  "ReportGrid"  at "http://nexus.reportgrid.com/content/repositories/public-snapshots"
)

libraryDependencies ++= Seq(
  "com.reportgrid" %% "blueeyes-core"  % "0.6.0-SNAPSHOT",
  "com.reportgrid" %% "blueeyes-mongo" % "0.6.0-SNAPSHOT",
  "com.reportgrid" %% "blueeyes-json"  % "0.6.0-SNAPSHOT",
  "ch.qos.logback" % "logback-classic" % "1.0.0" % "runtime"
)
{% endhighlight scala %}
