import cats.effect.IOApp
import cats.effect.Resource
import cats.effect.ExitCode
import cats.effect.IO
import cats.implicits._
import org.openqa.selenium.chrome.ChromeDriver

object Main extends IOApp {

  type HostPortUserPassPath = (String, Int, String, String, String)

  def parseArgs(args: List[String]): IO[HostPortUserPassPath] = IO(
    (args(0), args(1).toInt, args(2), args(3), args(4))
  )

  def argsToChromeDriver(args: List[String]): Resource[IO, ChromeDriver] =
    for {
      parsedArgs <- Resource.liftF(parseArgs(args))
      proxyHost = parsedArgs._1
      proxyPort = parsedArgs._2
      proxyUser = parsedArgs._3
      proxyPass = parsedArgs._4
      chromeDriverPath = parsedArgs._5
      chromeDriver <- ChromeProxyAuth.make[IO](
        proxyHost,
        proxyPort,
        proxyUser,
        proxyPass,
        chromeDriverPath
      )
    } yield chromeDriver

  override def run(args: List[String]): IO[ExitCode] = {
    argsToChromeDriver(args).use(chromeDriver =>
      IO(chromeDriver.get("https://httpbin.org/ip")) *> IO(
        println(chromeDriver.getPageSource)
      )
    ) *> ExitCode.Success.pure[IO]
  }

}
