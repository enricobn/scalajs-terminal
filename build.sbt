import sbt.Keys.*

val ver = "1.0.0"

lazy val root = (project in file("."))
  .settings(
    scalaVersion := "2.13.16",
    organization := "org.enricobn",
    name := "scalajs-terminal",
    version := ver,
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "2.8.0",
    libraryDependencies ++= Seq("org.scala-lang" % "scala-reflect" % "2.13.16"),

    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.19" % "test",
    libraryDependencies += "org.scalamock" %% "scalamock" % "7.3.0" % "test",
  )
  .enablePlugins(ScalaJSPlugin)

scalacOptions ++= Seq(
  "-Xsource:3",
  "-deprecation"
)