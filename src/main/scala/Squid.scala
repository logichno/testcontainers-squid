import cats.effect.Resource
import cats.effect.Sync
import cats.implicits._
import org.slf4j.LoggerFactory
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.output.Slf4jLogConsumer
import org.testcontainers.images.builder.ImageFromDockerfile

class Squid private (
    squidContainerPort: Int,
    baseImage: String,
    squidConfPath: String
) {

  def anonymizeProxy[F[_]: Sync](
      host: String,
      port: Int,
      username: String,
      password: String
  ): Resource[F, (String, Int)] = {
    val acquire: F[GenericContainer[_]] =
      acquireContainer[F](host, port, username, password)
    Resource
      .make[F, GenericContainer[_]](acquire)(release)
      .map(c => (c.getHost, c.getMappedPort(squidContainerPort)))
  }

  def acquireContainer[F[_]: Sync](
      host: String,
      port: Int,
      username: String,
      password: String
  ): F[GenericContainer[_]] = Sync[F].delay {
    def makeContainer =
      new GenericContainer(
        makeImage(makeSquidConf(host, port, username, password))
      )
    val container = makeContainer
    container.withLogConsumer(
      new Slf4jLogConsumer(LoggerFactory.getLogger(getClass.getName))
    )
    container.start()
    container
  }

  def release[F[_]: Sync]: GenericContainer[_] => F[Unit] =
    c => Sync[F].delay(c.stop())

  def makeSquidConf(
      host: String,
      port: Int,
      username: String,
      password: String
  ): String = {
    s"""http_access allow all
       |http_port ${squidContainerPort.show}
       |http_port 127.0.0.1:${squidContainerPort.show} intercept
       |
       |cache_peer $host parent $port 0 no-query default login=$username:$password
       |never_direct allow all
       |""".stripMargin
  }

  def makeImage(squidConfContent: String): ImageFromDockerfile =
    new ImageFromDockerfile()
      .withFileFromString("squid.conf", squidConfContent)
      .withFileFromString(
        "Dockerfile",
        s"FROM $baseImage\nCOPY squid.conf $squidConfPath"
      )

}

object Squid {
  def apply(
      squidContainerPort: Int = 3128,
      baseImage: String = "sameersbn/squid:3.5.27-2",
      squidConfPath: String = "/etc/squid/squid.conf"
  ): Squid =
    new Squid(
      squidContainerPort = squidContainerPort,
      baseImage = baseImage,
      squidConfPath = squidConfPath
    )
}
