// The Typesafe repository
resolvers += "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/"

// Use the Play sbt plugin for Play projects
addSbtPlugin("com.typesafe.play" %% "sbt-plugin" % "2.5.4")

// To check dependency updates: https://github.com/rtimush/sbt-updates
addSbtPlugin("com.timushev.sbt" %% "sbt-updates" % "0.1.10")

// frontend client written in scala.js
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.13")

// allows using scala.js with play web server
addSbtPlugin("com.vmunier" % "sbt-web-scalajs" % "1.0.3")

addSbtPlugin("com.typesafe.sbt" % "sbt-digest" % "1.1.1")

addSbtPlugin("com.typesafe.sbt" % "sbt-gzip" % "1.0.0")
