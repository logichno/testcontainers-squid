import org.testcontainers.containers.GenericContainer

trait SpecificContainer extends GenericContainer[SpecificContainer]

object SpecificContainer {
  def apply(): SpecificContainer =
    new GenericContainer[SpecificContainer]() with SpecificContainer
  def apply(dockerImageName: String): SpecificContainer =
    new GenericContainer[SpecificContainer](dockerImageName)
      with SpecificContainer
  def apply(image: java.util.concurrent.Future[String]): SpecificContainer =
    new GenericContainer[SpecificContainer](image) with SpecificContainer
}
