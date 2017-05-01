import java.io.File

import cats.effect.Resource
import cats.effect.Sync
import cats.implicits._
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeDriverService
import org.openqa.selenium.chrome.ChromeOptions

object Chrome {
  def makeDriver[F[_]: Sync](
      proxyHost: String,
      proxyPort: Int,
      driverPath: String
  ): Resource[F, ChromeDriver] = {

    val acquire: F[ChromeDriver] =
      Sync[F].delay {
        val proxyStr = s"$proxyHost:$proxyPort"
        val proxyArgument = "--proxy-server=http://" + proxyStr

        val options = new ChromeOptions()
          .addArguments(proxyArgument)
        new ChromeDriver(
          new ChromeDriverService.Builder()
            .usingDriverExecutable(new File(driverPath))
            .build(),
          options
        )
      }

    val release: ChromeDriver => F[Unit] =
      driver =>
        Sync[F].delay(driver.close()).attempt *> Sync[F].delay(driver.quit())

    Resource.make[F, ChromeDriver](acquire)(release)
  }
}
