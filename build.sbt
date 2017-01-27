name := "google-dataflow-boilerplate"
organization := "com.github.vspiewak"

scalaVersion := "2.11.8"

val servletApi = "javax.servlet" % "servlet-api" % "2.5" % "provided"
val scioCore = "com.spotify" %% "scio-core" % "0.2.0"
val scioTest = "com.spotify" %% "scio-test" % "0.2.0" % "test"

libraryDependencies ++= Seq(
  servletApi,
  scioCore,
  scioTest
)

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
