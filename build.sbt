val ZioVersion = "1.0.0-RC17"

resolvers += Resolver.sonatypeRepo("releases")
resolvers += Resolver.sonatypeRepo("snapshots")

lazy val root = (project in file("."))
  .settings(
    organization := "com.github.zachalbia",
    name := "practical-fp",
    version := "0.0.1",
    scalaVersion := "2.12.10",
    maxErrors := 3,
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio"          % ZioVersion,
      "dev.zio" %% "zio-test"     % ZioVersion % "test",
      "dev.zio" %% "zio-test-sbt" % ZioVersion % "test"
    )
  )

testFrameworks ++= Seq(new TestFramework("zio.test.sbt.ZTestFramework"))

// Refine scalac params from tpolecat
scalacOptions --= Seq(
  "-Xfatal-warnings"
)

addCommandAlias("fmt", "all scalafmtSbt scalafmt test:scalafmt")
addCommandAlias("chk", "all scalafmtSbtCheck scalafmtCheck test:scalafmtCheck")
