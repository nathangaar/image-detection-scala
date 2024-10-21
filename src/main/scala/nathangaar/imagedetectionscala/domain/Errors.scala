package nathangaar.imagedetectionscala.domain

object Errors:
  sealed trait ImageDetectionError                           extends Throwable
  final case class ImaggaError(e: Throwable)                 extends RuntimeException with ImageDetectionError
  final case class ImageDetectionFieldError(message: String) extends ImageDetectionError
  final case class ImageIdNotFound(imageId: String) extends ImageDetectionError {
    def message: String = s"Image with ID $imageId not found"
  }
