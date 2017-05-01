import cats.effect.Resource
import cats.effect.Sync
import org.openqa.selenium.chrome.ChromeDriver

object ChromeProxyAuth {
  def make[F[_]: Sync](
      proxyHost: String,
      proxyPort: Int,
      proxyUser: String,
      proxyPass: String,
      chromedriverPath: String
  ): Resource[F, ChromeDriver] =
    for {
      anonymousProxy <- Squid()
        .anonymizeProxy[F](proxyHost, proxyPort, proxyUser, proxyPass)
      chromeDriver <- Chrome
        .makeDriver[F](anonymousProxy._1, anonymousProxy._2, chromedriverPath)
    } yield chromeDriver
}
