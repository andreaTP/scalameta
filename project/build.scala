import sbt._
import Keys._
import sbtassembly.Plugin._
import AssemblyKeys._
import com.typesafe.sbt.pgp.PgpKeys._
import sbtunidoc.Plugin._
import UnidocKeys._
import java.io._
import org.scalameta.os._
import scala.compat.Platform.EOL
import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import org.scalajs.sbtplugin.cross.CrossProject

object build extends Build {
  lazy val ScalaVersions = Seq("2.11.8")
  lazy val LibraryVersion = "1.0.0-SNAPSHOT"

  lazy val root = Project(
    id = "root",
    base = file("root")
  ) settings (
    sharedSettings : _*
  ) settings (
    unidocSettings : _*
  ) settings (
    packagedArtifacts := Map.empty,
    unidocProjectFilter in (ScalaUnidoc, unidoc) := inAnyProject,
    aggregate in test := false,
    test := {
      //val runTests = (test in scalameta in Test).value
      val runDocs = (run in readme in Compile).toTask(" --validate").value
    },
    publish := {
      // Others projects are published automatically because we aggregate.
      val publishDocs = (publish in readme).value
    },
    // TODO: The same thing for publishSigned doesn't work.
    // SBT calls publishSigned on aggregated projects, but ignores everything else.
    console := (console in scalametaJVM in Compile).value
  ) aggregate (
    commonJVM,
    dialectsJVM,
    inlineJVM,
    inputsJVM,
    parsersJVM,
    quasiquotesJVM,
    scalametaJVM,
    tokenizersJVM,
    tokensJVM,
    transversersJVM,
    treesJVM
  )

  lazy val common = CrossProject(
    id   = "common",
    base = file("scalameta/common"),
    crossType = CrossType.Full
  ) jvmSettings (
    jvmSharedSettings : _*
  ) jsSettings(
    jsSharedSettings : _*
  ) settings (
    publishableSettings: _*
  ) settings (
    description := "Bag of private and public helpers used in scala.meta's APIs and implementations",
    enableMacros
  )

  lazy val commonJS = common.js
  lazy val commonJVM = common.jvm

  lazy val dialects = CrossProject(
    id   = "dialects",
    base = file("scalameta/dialects"),
    crossType = CrossType.Full
  ) jvmSettings (
    jvmSharedSettings : _*
  ) jsSettings(
    jsSharedSettings : _*
  ) settings (
    publishableSettings: _*
  ) settings (
    description := "Scala.meta's dialects",
    enableMacros
  ) dependsOn (common)

  lazy val dialectsJS = dialects.js
  lazy val dialectsJVM = dialects.jvm

  lazy val inline = CrossProject(
    id   = "inline",
    base = file("scalameta/inline"),
    crossType = CrossType.Full
  ) jvmSettings (
    jvmSharedSettings : _*
  ) jsSettings(
    jsSharedSettings : _*
  ) settings (
    publishableSettings: _*
  ) settings (
    description := "Scala.meta's APIs for new-style (\"inline\") macros"
  ) dependsOn (inputs)

  lazy val inlineJS = inline.js
  lazy val inlineJVM = inline.jvm

  lazy val inputs = CrossProject(
    id   = "inputs",
    base = file("scalameta/inputs"),
    crossType = CrossType.Full
  ) jvmSettings (
    jvmSharedSettings : _*
  ) jsSettings(
    jsSharedSettings : _*
  ) settings (
    publishableSettings: _*
  ) settings (
    description := "Scala.meta's APIs for source code in textual format"
  ) dependsOn (common)

  lazy val inputsJS = inputs.js
  lazy val inputsJVM = inputs.jvm

  lazy val parsers = CrossProject(
    id   = "parsers",
    base = file("scalameta/parsers"),
    crossType = CrossType.Full
  ) jvmSettings (
    jvmSharedSettings : _*
  ) jsSettings(
    jsSharedSettings : _*
  ) settings (
    publishableSettings: _*
  ) settings (
    description := "Scala.meta's API for parsing and its baseline implementation"
  ) dependsOn (common, dialects, inputs, tokens, tokenizers, trees)

  lazy val parsersJS = parsers.js
  lazy val parsersJVM = parsers.jvm

  lazy val quasiquotes = CrossProject(
    id   = "quasiquotes",
    base = file("scalameta/quasiquotes"),
    crossType = CrossType.Full
  ) jvmSettings (
    jvmSharedSettings : _*
  ) jsSettings(
    jsSharedSettings : _*
  ) settings (
    publishableSettings: _*
  ) settings (
    description := "Scala.meta's quasiquotes for abstract syntax trees",
    enableHardcoreMacros
  ) dependsOn (common, dialects, inputs, trees, parsers)

  lazy val quasiquotesJS = quasiquotes.js
  lazy val quasiquotesJVM = quasiquotes.jvm

  lazy val tokenizers = CrossProject(
    id   = "tokenizers",
    base = file("scalameta/tokenizers"),
    crossType = CrossType.Full
  ) jvmSettings (
    jvmSharedSettings : _*
  ) jsSettings(
    jsSharedSettings : _*
  ) settings (
    publishableSettings: _*
  ) settings (
    description := "Scala.meta's APIs for tokenization and its baseline implementation",
    enableMacros
  ) jvmSettings (
    libraryDependencies += "com.lihaoyi" %% "scalaparse" % "0.3.7"
  ) jsSettings (
    libraryDependencies += "com.lihaoyi" %%% "scalaparse" % "0.3.7"
  )dependsOn (common, dialects, inputs, tokens)

  lazy val tokenizersJS = tokenizers.js
  lazy val tokenizersJVM = tokenizers.jvm

  lazy val tokens = CrossProject(
    id   = "tokens",
    base = file("scalameta/tokens"),
    crossType = CrossType.Full
  ) jvmSettings (
    jvmSharedSettings : _*
  ) jsSettings(
    jsSharedSettings : _*
  ) settings (
    publishableSettings: _*
  ) settings (
    description := "Scala.meta's tokens and token-based abstractions (inputs and positions)",
    enableMacros
  ) dependsOn (common, dialects, inputs)

  lazy val tokensJS = tokens.js
  lazy val tokensJVM = tokens.jvm

  lazy val transversers = CrossProject(
    id   = "transversers",
    base = file("scalameta/transversers"),
    crossType = CrossType.Full
  ) jvmSettings (
    jvmSharedSettings : _*
  ) jsSettings(
    jsSharedSettings : _*
  ) settings (
    publishableSettings: _*
  ) settings (
    description := "Scala.meta's traversal and transformation infrastructure for abstract syntax trees",
    enableMacros
  ) dependsOn (common, trees)

  lazy val transversersJS = transversers.js
  lazy val transversersJVM = transversers.jvm

  lazy val trees = CrossProject(
    id   = "trees",
    base = file("scalameta/trees"),
    crossType = CrossType.Full
  ) jvmSettings (
    jvmSharedSettings : _*
  ) jsSettings(
    jsSharedSettings : _*
  )  settings (
    publishableSettings: _*
  ) settings (
    description := "Scala.meta's abstract syntax trees",
    // NOTE: uncomment this to update ast.md
    // scalacOptions += "-Xprint:typer",
    enableMacros
  ) dependsOn (common, dialects, inputs, tokens, tokenizers) // NOTE: tokenizers needed for Tree.tokens when Tree.pos.isEmpty

  lazy val treesJS = trees.js
  lazy val treesJVM = trees.jvm

  lazy val scalameta = CrossProject(
    id   = "scalameta",
    base = file("scalameta/scalameta"),
    crossType = CrossType.Full
  ) jvmSettings (
    jvmSharedSettings : _*
  ) jsSettings(
    jsSharedSettings : _*
  ) settings (
    publishableSettings: _*
  ) settings (
    description := "Scala.meta's metaprogramming APIs"
  ) settings (
    exposePaths("scalameta", Test): _*
  ) dependsOn (common, dialects, parsers, quasiquotes, tokenizers, transversers, trees, inline)

  lazy val scalametaJS = scalameta.js
  lazy val scalametaJVM = scalameta.jvm

  lazy val readme = scalatex.ScalatexReadme(
    projectId = "readme",
    wd = file(""),
    url = "https://github.com/scalameta/scalameta/tree/master",
    source = "Readme"
  ) settings (
    exposePaths("readme", Runtime): _*
  ) settings (
    libraryDependencies += "org.scala-lang" % "scala-compiler" % scalaVersion.value,
    // Workaround for https://github.com/lihaoyi/Scalatex/issues/25
    dependencyOverrides += "com.lihaoyi" %% "scalaparse" % "0.3.1",
    sources in Compile ++= List("os.scala").map(f => baseDirectory.value / "../project" / f),
    watchSources ++= baseDirectory.value.listFiles.toList,
    publish := {
      // generate the scalatex readme into `website`
      val website = new File(target.value.getAbsolutePath + File.separator + "scalatex")
      if (website.exists) website.delete
      val _ = (run in Compile).toTask(" --validate").value
      if (!website.exists) sys.error("failed to generate the scalatex website")

      // import the scalatex readme into `repo`
      val repo = new File(temp.mkdir.getAbsolutePath + File.separator + "scalameta.org")
      shell.call(s"git clone https://github.com/scalameta/scalameta.github.com ${repo.getAbsolutePath}")
      println(s"erasing everything in ${repo.getAbsolutePath}...")
      repo.listFiles.filter(f => f.getName != ".git").foreach(shutil.rmtree)
      println(s"importing website from ${website.getAbsolutePath} to ${repo.getAbsolutePath}...")
      new PrintWriter(new File(repo.getAbsolutePath + File.separator + "CNAME")) { write("scalameta.org"); close }
      website.listFiles.foreach(src => shutil.copytree(src, new File(repo.getAbsolutePath + File.separator + src.getName)))

      // make sure that we have a stable reference to the working copy that produced the website
      val currentSha = shell.check_output("git rev-parse HEAD", cwd = ".")
      val changed = shell.check_output("git diff --name-status", cwd = ".")
      if (changed.trim.nonEmpty) sys.error("repository " + new File(".").getAbsolutePath + " is dirty (has modified files)")
      val staged = shell.check_output("git diff --staged --name-status", cwd = ".")
      if (staged.trim.nonEmpty) sys.error("repository " + new File(".").getAbsolutePath + " is dirty (has staged files)")
      val untracked = shell.check_output("git ls-files --others --exclude-standard", cwd = ".")
      if (untracked.trim.nonEmpty) sys.error("repository " + new File(".").getAbsolutePath + " is dirty (has untracked files)")
      val (exitcode, stdout, stderr) = shell.exec(s"git branch -r --contains $currentSha")
      if (exitcode != 0 || stdout.isEmpty) sys.error("repository " + new File(".").getAbsolutePath + " doesn't contain commit " + currentSha)

      // commit and push the changes if any
      shell.call(s"git add -A", cwd = repo.getAbsolutePath)
      val nothingToCommit = "nothing to commit, working directory clean"
      try {
        val currentUrl = s"https://github.com/scalameta/scalameta/tree/" + currentSha.trim
        shell.call(s"git config user.email 'scalametabot@gmail.com'", cwd = repo.getAbsolutePath)
        shell.call(s"git config user.name 'Scalameta Bot'", cwd = repo.getAbsolutePath)
        shell.call(s"git commit -m $currentUrl", cwd = repo.getAbsolutePath)
        val httpAuthentication = secret.obtain("github").map{ case (username, password) => s"$username:$password@" }.getOrElse("")
        val authenticatedUrl = s"https://${httpAuthentication}github.com/scalameta/scalameta.github.com"
        shell.call(s"git push $authenticatedUrl master", cwd = repo.getAbsolutePath)
      } catch {
        case ex: Exception if ex.getMessage.contains(nothingToCommit) => println(nothingToCommit)
      }
    },
    // TODO: doesn't work at the moment, see https://github.com/sbt/sbt-pgp/issues/42
    publishSigned := publish.value,
    publishLocal := {},
    publishLocalSigned := {},
    publishM2 := {}
  ) dependsOn (scalametaJVM)

  lazy val sharedSettings = crossVersionSharedSources ++ Seq(
    scalaVersion := ScalaVersions.max,
    crossScalaVersions := ScalaVersions,
    crossVersion := CrossVersion.binary,
    version := LibraryVersion,
    organization := "org.scalameta",
    resolvers += Resolver.sonatypeRepo("snapshots"),
    resolvers += Resolver.sonatypeRepo("releases"),
    addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
    publishMavenStyle := true,
    publishArtifact in Compile := false,
    publishArtifact in Test := false,
    scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked"),
    scalacOptions in (Compile, doc) ++= Seq("-skip-packages", ""),
    scalacOptions in (Compile, doc) ++= Seq("-implicits", "-implicits-hide:."),
    scalacOptions in (Compile, doc) ++= Seq("-groups"),
    scalacOptions ++= Seq("-Xfatal-warnings"),
    parallelExecution in Test := false, // hello, reflection sync!!
    logBuffered := false,
    scalaHome := {
      val scalaHome = System.getProperty("core.scala.home")
      if (scalaHome != null) {
        println(s"Going for custom scala home at $scalaHome")
        Some(file(scalaHome))
      } else None
    },
    publishMavenStyle := true,
    publishTo <<= version { v: String =>
      val nexus = "https://oss.sonatype.org/"
      if (v.trim.endsWith("SNAPSHOT"))
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("releases" at nexus + "service/local/staging/deploy/maven2")
    },
    pomIncludeRepository := { x => false },
    pomExtra := (
      <url>https://github.com/scalameta/scalameta</url>
      <inceptionYear>2014</inceptionYear>
      <licenses>
        <license>
          <name>BSD-like</name>
          <url>https://github.com/scalameta/scalameta/blob/master/LICENSE.md</url>
          <distribution>repo</distribution>
        </license>
      </licenses>
      <scm>
        <url>git://github.com/scalameta/scalameta.git</url>
        <connection>scm:git:git://github.com/scalameta/scalameta.git</connection>
      </scm>
      <issueManagement>
        <system>GitHub</system>
        <url>https://github.com/scalameta/scalameta/issues</url>
      </issueManagement>
      <developers>
        <developer>
          <id>xeno-by</id>
          <name>Eugene Burmako</name>
          <url>http://xeno.by</url>
        </developer>
        <developer>
          <id>densh</id>
          <name>Denys Shabalin</name>
          <url>http://den.sh</url>
        </developer>
      </developers>
    )
  )

  lazy val jvmSharedSettings: Seq[sbt.Def.Setting[_]] = Seq (
    libraryDependencies += "org.scalatest" %% "scalatest" % "2.1.3" % "test",
    libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.11.3" % "test"
  )

  lazy val jsSharedSettings: Seq[sbt.Def.Setting[_]] = Seq (
    libraryDependencies += "org.scalatest" %%% "scalatest" % "3.0.0-M15" % "test",
    libraryDependencies += "org.scalacheck" %%% "scalacheck" % "1.13.0" % "test"
  )

  lazy val publishableSettings = sharedSettings ++ Seq(
    publishArtifact in Compile := true,
    publishArtifact in Test := false,
    credentials ++= secret.obtain("sonatype").map({
      case (username, password) => Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", username, password)
    }).toList
  )

  lazy val mergeSettings: Seq[sbt.Def.Setting[_]] = assemblySettings ++ Seq(
    test in assembly := {},
    logLevel in assembly := Level.Error,
    jarName in assembly := name.value + "_" + scalaVersion.value + "-" + version.value + "-assembly.jar",
    assemblyOption in assembly ~= { _.copy(includeScala = false) },
    Keys.`package` in Compile := {
      val slimJar = (Keys.`package` in Compile).value
      val fatJar = new File(crossTarget.value + "/" + (jarName in assembly).value)
      val _ = assembly.value
      IO.copy(List(fatJar -> slimJar), overwrite = true)
      slimJar
    },
    packagedArtifact in Compile in packageBin := {
      val temp = (packagedArtifact in Compile in packageBin).value
      val (art, slimJar) = temp
      val fatJar = new File(crossTarget.value + "/" + (jarName in assembly).value)
      val _ = assembly.value
      IO.copy(List(fatJar -> slimJar), overwrite = true)
      (art, slimJar)
    }
  )

  def exposePaths(projectName: String, config: Configuration) = {
    def uncapitalize(s: String) = if (s.length == 0) "" else { val chars = s.toCharArray; chars(0) = chars(0).toLower; new String(chars) }
    val prefix = "sbt.paths." + projectName + "." + uncapitalize(config.name) + "."
    Seq(
      sourceDirectory in config := {
        val defaultValue = (sourceDirectory in config).value
        System.setProperty(prefix + "sources", defaultValue.getAbsolutePath)
        defaultValue
      },
      resourceDirectory in config := {
        val defaultValue = (resourceDirectory in config).value
        System.setProperty(prefix + "resources", defaultValue.getAbsolutePath)
        defaultValue
      },
      fullClasspath in config := {
        val defaultValue = (fullClasspath in config).value
        val classpath = defaultValue.files.map(_.getAbsolutePath)
        val scalaLibrary = classpath.map(_.toString).find(_.contains("scala-library")).get
        System.setProperty("sbt.paths.scalalibrary.classes", scalaLibrary)
        System.setProperty(prefix + "classes", classpath.mkString(java.io.File.pathSeparator))
        defaultValue
      }
    )
  }

  def macroDependencies(hardcore: Boolean) = libraryDependencies ++= {
    val scalaReflect = Seq("org.scala-lang" % "scala-reflect" % scalaVersion.value % "provided")
    val scalaCompiler = {
      if (hardcore) Seq("org.scala-lang" % "scala-compiler" % scalaVersion.value % "provided")
      else Nil
    }
    val backwardCompat210 = {
      if (scalaVersion.value.startsWith("2.10")) Seq("org.scalamacros" %% "quasiquotes" % "2.1.0")
      else Seq()
    }
    scalaReflect ++ scalaCompiler ++ backwardCompat210
  }

  lazy val enableMacros = macroDependencies(hardcore = false)
  lazy val enableHardcoreMacros = macroDependencies(hardcore = true)

  lazy val crossVersionSharedSources: Seq[Setting[_]] =
    Seq(Compile, Test).map { sc =>
      (unmanagedSourceDirectories in sc) ++= {
        (unmanagedSourceDirectories in sc).value.map { dir =>
          CrossVersion.partialVersion(scalaVersion.value) match {
            case Some((2, y)) if y == 10 => new File(dir.getPath + "_2.10")
            case Some((2, y)) if y == 11 => new File(dir.getPath + "_2.11")
            case other => sys.error("unsupported Scala version " + other)
          }
        }
      }
    }
}
