package nathangaar.imagedetectionscala.repositories.images

import cats.data.NonEmptyList
import cats.effect.*
import cats.implicits.*
import doobie.*
import doobie.implicits.*
import io.circe.*
import io.circe.parser.*
import nathangaar.imagedetectionscala.repositories.images.Filters.*
import nathangaar.imagedetectionscala.repositories.images.ImageTypes.{Image, ImageId, ImageUrl, Label}

trait ImageRepositoryLike[F[_]]:
  def images(queries: FilterImages): F[Seq[Image]]
  def image(uuid: ImageUUID): F[Option[Image]]
  def insert(image: Commands.InsertImage): F[Option[Image]]

class ImageRepository[F[_]: Async](xa: Transactor[F]) extends ImageRepositoryLike[F]:
  implicit val jsonRead: Read[Json] = Read[String].map { jsonString =>
    parse(jsonString).fold(
      left => throw new Exception(s"Invalid JSON: ${left.getMessage}"),
      right => right
    )
  }

  private def buildQuery(filter: FilterImages): Fragment = {
    val terms = filter.terms.map(_.value).toList
    val labelFilters =
      NonEmptyList
        .fromList(terms)
        .fold(fr"") { labels =>
          fr"WHERE" ++ Fragments.in(fr"label", labels)
        }

    val metaDataFilters = terms.map { term =>
      fr"meta_data::text ILIKE '%' || $term || '%'"
    }

    // Combine metadata filters
    val metadataCondition = if (metaDataFilters.nonEmpty) {
      fr"OR" ++ metaDataFilters.reduceLeft(_ ++ fr"OR" ++ _)
    } else {
      fr""
    }
    fr"SELECT uuid, meta_data, image_url, label FROM images" ++ labelFilters ++ metadataCondition
  }

  override def images(filter: FilterImages): F[Seq[Image]] =
    buildQuery(filter)
      .query[(String, Json, String, String)]
      .map { case (uuid, metaData, imageUrl, label) =>
        Image(ImageId(uuid), metaData, ImageUrl(imageUrl), Label(label))
      }
      .to[List]
      .map(_.toSeq)
      .transact(xa)

  override def image(uuid: ImageUUID): F[Option[Image]] =
    fr"SELECT uuid, meta_data, image_url, label FROM images WHERE uuid = ${uuid.id}::uuid"
      .query[(String, Json, String, String)]
      .map { case (uuid, metaData, imageUrl, label) =>
        Image(ImageId(uuid), metaData, ImageUrl(imageUrl), Label(label))
      }
      .option
      .transact(xa)
      .handleError(_ => None)

  override def insert(image: Commands.InsertImage): F[Option[Image]] = {
    val insertQuery =
      sql"""
        INSERT INTO images (meta_data, image_url, label)
        VALUES (${image.metaData.noSpaces}::jsonb, ${image.imageUrl.value}, ${image.label.value})
        RETURNING uuid, meta_data, image_url, label
      """.update

    insertQuery
      .withUniqueGeneratedKeys[String](
        "uuid"
      )
      .map { case (uuid) =>
        Image(ImageId(uuid), image.metaData, image.imageUrl, image.label)
      }
      .transact(xa)
      .map(Some(_))
      .handleError { _ =>
        None // Typically we'd log this but moving quick
      }

  }
