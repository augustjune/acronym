name := "acronym"

version := "0.1"

scalaVersion := "2.12.6"

scalacOptions ++= Seq(
  "-language:higherKinds"
)

libraryDependencies ++= Seq(
  "com.softwaremill.sttp"   %% "core"       % "1.5.11",
  "org.typelevel"           %% "cats-core"  % "1.6.0",
  "com.typesafe"            % "config"      % "1.3.4",
)

libraryDependencies ++= Seq(
  "org.scalatest"           %% "scalatest"    % "3.0.5"     % Test,
  "com.github.pureconfig"   %% "pureconfig"   % "0.10.2"    % Test
)
