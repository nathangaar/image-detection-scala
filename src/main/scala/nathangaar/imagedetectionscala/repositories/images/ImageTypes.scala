package nathangaar.imagedetectionscala.repositories.images

import io.circe.generic.auto.*
import io.circe.syntax.*
import io.circe.{Encoder, Json}

object ImageTypes:
  opaque type ImageId  = String
  opaque type ImageUrl = String
  opaque type Label    = String

  object ImageId:
    def apply(id: String): ImageId            = id
    extension (id: ImageId) def value: String = id

  object ImageUrl:
    def apply(url: String): ImageUrl            = url
    extension (url: ImageUrl) def value: String = url

  object Label:
    def apply(label: String): Label            = label
    extension (label: Label) def value: String = label

  case class Image(id: ImageId, metaData: Json, imageUrl: ImageUrl, label: Label)

  given imageIdEncoder: Encoder[ImageId]   = Encoder.instance(id => Json.fromString(id))
  given imageUrlEncoder: Encoder[ImageUrl] = Encoder.instance(url => Json.fromString(url))
  given labelEncoder: Encoder[Label]       = Encoder.instance(label => Json.fromString(label))

  implicit val imageEncoder: Encoder[Image] = Encoder.instance { image =>
    Json.obj(
      "id"       -> image.id.asJson(using imageIdEncoder),
      "metaData" -> image.metaData,
      "imageUrl" -> image.imageUrl.asJson(using imageUrlEncoder),
      "label"    -> image.label.asJson(using labelEncoder)
    )
  }
