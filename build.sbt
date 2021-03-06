import sbt.Keys._


val ver = "1.0.0"

val artifactPrefix = "target/scala-2.11/scalajs-terminal-" + ver

lazy val root = (project in file("."))
  .settings(
    scalaVersion := "2.11.8",
    organization := "org.enricobn",
    name := "scalajs-terminal",
    version := ver,
    artifactPath in (Compile, fullOptJS) := baseDirectory.value / (artifactPrefix + ".min.js"),
    artifactPath in (Compile, packageJSDependencies) := baseDirectory.value / (artifactPrefix + "-jsdeps.js"),
    artifactPath in (Compile, packageMinifiedJSDependencies) := baseDirectory.value / (artifactPrefix + "-jsdeps.min.js"),
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.1",
    libraryDependencies ++= Seq("org.scala-lang" % "scala-reflect" % "2.11.8"),
    // TEST
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % "test",
    libraryDependencies += "org.scalamock" %% "scalamock-scalatest-support" % "3.3.0" % "test",
    libraryDependencies += "com.lihaoyi" %%% "utest" % "0.4.3" % "test",
    testFrameworks += new TestFramework("utest.runner.Framework")
  )
  .enablePlugins(ScalaJSPlugin)
    