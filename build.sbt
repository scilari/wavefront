name := "wavefront"

organization := "com.scilari"

version := "0.1"

scalaVersion := "2.12.4"

resolvers += Resolver.sonatypeRepo("snapshots")

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.0" % "test"
libraryDependencies += "com.scilari" %% "spatialsearch" % "0.2.2-SNAPSHOT" % "test"

addCompilerPlugin("org.psywerx.hairyfotr" %% "linter" % "0.1.17")