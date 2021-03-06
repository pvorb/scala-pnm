name := "scala-pnm"

organization := "de.vorb"

version := "0.0.1"

scalaVersion := "2.10.3"


homepage := Some(url("https://github.com/pvorb/scala-pnm"))

licenses := Seq("MIT License" -> url("http://vorba.ch/license/mit.html"))

mainClass := None


// Dependencies
resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"


// Publishing information
publishMavenStyle := true

publishTo <<= version { (version: String) =>
  val nexus = "https://oss.sonatype.org/"
  if (version.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

pomExtra := (
  <scm>
    <url>git@github.com:pvorb/scala-pnm.git</url>
    <connection>scm:git:git@github.com:pvorb/scala-pnm.git</connection>
  </scm>
  <developers>
    <developer>
      <id>pvorb</id>
      <name>Paul Vorbach</name>
      <email>paul@vorba.ch</email>
      <url>http://paul.vorba.ch/</url>
      <timezone>+1</timezone>
    </developer>
  </developers>)
