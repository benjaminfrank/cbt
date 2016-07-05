package cbt
import java.io.File
import java.net.URL
import java.nio.file.Files.readAllBytes

trait Publish extends PackageJars{
  def description: String
  def url: URL
  def developers: Seq[Developer]
  def licenses: Seq[License]
  def scmUrl: String
  def scmConnection: String
  def inceptionYear: Int
  def organization: Option[Organization]

  // ========== package ==========

  /** put additional xml that should go into the POM file in here */
  def pom: File = lib.pom(
    groupId = groupId,
    artifactId = artifactId,
    version = version,
    scalaMajorVersion = scalaMajorVersion,
    name = name,
    description = description,
    url = url,
    developers = developers,
    licenses = licenses,
    scmUrl = scmUrl,
    scmConnection = scmConnection,
    inceptionYear,
    organization,
    dependencies = dependencies,
    jarTarget = jarTarget
  )

  // ========== publish ==========
  final protected def releaseFolder = s"/${groupId.replace(".","/")}/${artifactId}_$scalaMajorVersion/$version/"
  private def snapshotUrl = new URL("https://oss.sonatype.org/content/repositories/snapshots")
  private def releaseUrl = new URL("https://oss.sonatype.org/service/local/staging/deploy/maven2")
  def publishUrl = if(version.endsWith("-SNAPSHOT")) snapshotUrl else releaseUrl
  override def copy(context: Context) = super.copy(context).asInstanceOf[Publish]

  protected def sonatypeCredentials: Option[String] = {
    // FIXME: this should probably not use cbtHome, but some reference to the system's host cbt
    Some(new String(readAllBytes((context.cbtRootHome ++ "/sonatype.login").toPath)).trim)
  }

  def publishSnapshot: Unit = {
    copy( context.copy(version = Some(version+"-SNAPSHOT")) ).publishUnsigned
  }

  def publishUnsigned: Unit = {
    lib.publishUnsigned(
      sourceFiles, `package` :+ pom, publishUrl ++ releaseFolder, sonatypeCredentials
    )
  }
  def publishSigned: Unit = {
    lib.publishSigned(
      sourceFiles, `package` :+ pom, publishUrl ++ releaseFolder, sonatypeCredentials
    )
  }
}
