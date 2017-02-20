
name := "halo"

//common settings for the project and subprojects
lazy val commonSettings = Seq(
	organization := "com.flowmaster",
	version := "0.1.2",
	scalaVersion := "2.11.8",
	scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-target:jvm-1.8")
)

// javascript frontend for arbiter
lazy val arbiterJs = (project in file("arbiterjs"))
	.settings(commonSettings:_*)
	.settings(
		persistLauncher := true,
		skip in packageJSDependencies := false,
		jsDependencies ++= Seq(
			"org.webjars" % "jquery" % "2.1.4" / "jquery.js",
			"org.webjars" % "foundation" % "6.2.3" / "foundation.js" dependsOn "jquery.js"
		),
		libraryDependencies ++= Seq(
			"be.doeraene" %%% "scalajs-jquery" % "0.9.1",
			"com.github.karasiq" %%% "scalajs-highcharts" % "1.1.2-test",
			"com.lihaoyi" %%% "upickle" % "0.4.3"
		)
	)
	.enablePlugins(ScalaJSPlugin, ScalaJSWeb)
  .dependsOn(sharedJs)


// main backend server for arbiter
lazy val arbiter = (project in file("arbiter"))
	.settings(commonSettings: _*)
	.settings(routesGenerator := InjectedRoutesGenerator)
  .settings(addCommandAlias("tables", "run-main utils.db.SourceCodeGenerator"): _*)
	.settings(
		libraryDependencies ++= Seq(
		  "com.typesafe.slick" %% "slick" % "3.1.1",
		  "com.typesafe.slick" %% "slick-codegen" % "3.1.1",
		  "com.github.tminglei" %% "slick-pg" % "0.14.3",
		  "com.github.tminglei" %% "slick-pg_date2" % "0.14.3",
		  "com.typesafe.play" %% "play-slick" % "2.0.2",
		  "com.typesafe.play" %% "play-slick-evolutions" % "2.0.2",
		  "jp.t2v" %% "play2-auth" % "0.14.2",
		  play.sbt.Play.autoImport.cache,
		  "com.github.t3hnar" %% "scala-bcrypt" % "2.6",
		  "org.webjars" %% "webjars-play" % "2.5.0",
		  "org.webjars" % "foundation" % "6.2.3",
		  "com.typesafe.play" %% "play-mailer" % "5.0.0",
      "com.vmunier" %% "scalajs-scripts" % "1.0.0",
      "com.lihaoyi" %%% "upickle" % "0.4.3",
		  filters,
		  specs2 % Test
		),
		scalaJSProjects := Seq(arbiterJs),
		pipelineStages in Assets := Seq(scalaJSPipeline),
		pipelineStages := Seq(digest, gzip)
	)
  .enablePlugins(PlayScala, SbtWeb)
  .dependsOn(sharedJvm)


lazy val common = (crossProject.crossType(CrossType.Pure) in file("common"))
  .settings(commonSettings:_*)

lazy val sharedJvm = common.jvm
lazy val sharedJs = common.js
