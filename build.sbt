val CatsVersion       = "2.10.0"
val WeaverCatsVersion = "0.8.3"

ThisBuild / organization     := "com.melvinlow"
ThisBuild / organizationName := "Melvin Low"

ThisBuild / scalaVersion       := "3.3.0"
ThisBuild / crossScalaVersions := Seq(scalaVersion.value)

ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision

ThisBuild / licenses := Seq("APL2" -> url("https://www.apache.org/licenses/LICENSE-2.0.txt"))
ThisBuild / homepage := Some(url("https://github.com/lowmelvin/hammer-scala"))
ThisBuild / developers := List(
  Developer("lowmelvin", "Melvin Low", "me@melvinlow.com", url("https://melvinlow.com"))
)

ThisBuild / sonatypeCredentialHost := "s01.oss.sonatype.org"
ThisBuild / sonatypeRepository     := "https://s01.oss.sonatype.org/service/local"

usePgpKeyHex("821A82C15670B776F9950C8046E96DBCFD1E8107")

lazy val root = (project in file("."))
  .settings(
    name        := "hammer",
    description := "Scala library to hammer one ADT to another.",
    libraryDependencies ++= Seq(
      "org.typelevel"       %% "cats-core"   % CatsVersion       % Test,
      "com.disneystreaming" %% "weaver-cats" % WeaverCatsVersion % Test
    ),
    scalacOptions ++= Seq(
      "-encoding",
      "UTF-8",
      "-feature",
      "-unchecked",
      "-deprecation",
      "-Wunused:imports",
      "-Werror",
      "-Wvalue-discard",
      "-no-indent",
      "-explain"
    ),
    testFrameworks ++= List(
      new TestFramework("weaver.framework.CatsEffect")
    )
  )

lazy val docs = (project in file("hammer-docs"))
  .dependsOn(root)
  .enablePlugins(MdocPlugin)
  .settings(
    mdocIn  := file("docs/README.md"),
    mdocOut := file("README.md")
  )
