package nathangaar.imagedetectionscala.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.*

object Imagga:
  final case class Analysis(result: TagCollection)
  final case class TagCollection(tags: Seq[ImageTag])
  final case class ImageTag(confidence: Confidence, tag: Tag)
  final case class Confidence(confidence: Double)
  final case class Tag(en: En)
  final case class En(en: String)

  given Decoder[Confidence] = Decoder[Double].map(Confidence(_))

  given Decoder[En] = Decoder[String].map(En(_))

  given Decoder[ImageTag]      = deriveDecoder[ImageTag]
  given Decoder[Tag]           = deriveDecoder[Tag]
  given Decoder[TagCollection] = deriveDecoder[TagCollection]
  given Decoder[Analysis]      = deriveDecoder[Analysis]

  given Encoder[Confidence] = Encoder[Double].contramap(_.confidence)

  given Encoder[En] = Encoder[String].contramap(_.en)

  given Encoder[ImageTag]      = deriveEncoder[ImageTag]
  given Encoder[Tag]           = deriveEncoder[Tag]
  given Encoder[TagCollection] = deriveEncoder[TagCollection]
  given Encoder[Analysis]      = deriveEncoder[Analysis]
