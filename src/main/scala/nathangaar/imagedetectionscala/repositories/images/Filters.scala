package nathangaar.imagedetectionscala.repositories.images

import io.circe.Json
import nathangaar.imagedetectionscala.repositories.images.ImageTypes.{ImageUrl, Label}

object Filters:
  opaque type Term = String
  object Term:
    def apply(term: String): Term            = term
    extension (term: Term) def value: String = term

  final case class FilterImages(
    terms: Seq[Term] = Seq.empty[Term]
  )
  final case class ImageUUID(id: String)

object Commands:
  final case class InsertImage(metaData: Json, imageUrl: ImageUrl, label: Label)
