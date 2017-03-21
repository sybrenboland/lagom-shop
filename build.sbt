
organization in ThisBuild := "com.example"
version in ThisBuild := "1.0-SNAPSHOT"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.11.8"

val macwire = "com.softwaremill.macwire" %% "macros" % "2.2.5" % "provided"
val scalaTest = "org.scalatest" %% "scalatest" % "3.0.1" % Test
//val conductr = "com.typesafe.conductr" %% "scala-conductr-bundle-lib" % "1.6.0"

lazy val `shop` = (project in file("."))
  .aggregate(`basket-api`, `basket-impl`)

lazy val `basket-api` = (project in file("basket-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )
  .dependsOn(`catalogue-api`)

lazy val `basket-impl` = (project in file("basket-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceCassandra,
      lagomScaladslKafkaBroker,
      lagomScaladslTestKit,
      macwire,
      scalaTest
    )
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`basket-api`)

lazy val `stock-api` = (project in file("stock-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )
  .dependsOn(`basket-api`)

lazy val `stock-impl` = (project in file("stock-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceCassandra,
      lagomScaladslKafkaBroker,
      lagomScaladslTestKit,
      macwire,
      scalaTest
    )
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`stock-api`, `catalogue-api`)

lazy val `catalogue-api` = (project in file("catalogue-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )

lazy val `catalogue-impl` = (project in file("catalogue-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceCassandra,
      lagomScaladslKafkaBroker,
      lagomScaladslTestKit,
      macwire,
      scalaTest
    )
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`catalogue-api`)

//lazy val `front-end` = (project in file("front-end"))
//  .enablePlugins(PlayScala, LagomPlay)
//  .settings(
//    version := "1.0-SNAPSHOT",
//    routesGenerator := InjectedRoutesGenerator,
//    libraryDependencies ++= Seq(
//      "org.webjars" % "react" % "0.14.3",
//      "org.webjars" % "react-router" % "1.0.3",
//      "org.webjars" % "jquery" % "2.2.0",
//      "org.webjars" % "foundation" % "5.3.0"
//    )
//    //ReactJsKeys.sourceMapInline := true
//  )