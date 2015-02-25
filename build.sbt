name := "schedule"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  "postgresql" % "postgresql" % "9.1-901.jdbc4",
   "org.hibernate" % "hibernate-entitymanager" % "3.6.9.Final"
)     

play.Project.playJavaSettings
