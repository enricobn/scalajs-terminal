import sbt.Keys._


val ver = "1.0.0"

val artifactPrefix = "target/scala-2.11/textscreen-" + ver

lazy val root = (project in file("."))
  .settings(
    scalaVersion := "2.11.8",
    name := "scalajs-terminal",
    version := ver,
    artifactPath in (Compile, fullOptJS) := baseDirectory.value / (artifactPrefix + ".min.js"),
    artifactPath in (Compile, packageJSDependencies) := baseDirectory.value / (artifactPrefix + "-jsdeps.js"),
    artifactPath in (Compile, packageMinifiedJSDependencies) := baseDirectory.value / (artifactPrefix + "-jsdeps.min.js"),
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.1",
    libraryDependencies ++= Seq("org.scala-lang" % "scala-reflect" % "2.11.8"),
    // TEST
    libraryDependencies += "com.lihaoyi" %%% "utest" % "0.4.3" % "test",
    testFrameworks += new TestFramework("utest.runner.Framework")
  )
  .enablePlugins(ScalaJSPlugin)
    