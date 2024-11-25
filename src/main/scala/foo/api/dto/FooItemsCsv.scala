package foo.api.dto

import foo.domain.model.{FooItemName, FooItemText, FooItemType, NewFooItem}
import fs2.data.csv.*

import scala.util.Try

object FooItemsCsv:
  given csvTypeCellDecoder: CellDecoder[FooItemType] with
    override def apply(cell: String): DecoderResult[FooItemType] =
      Try(FooItemType.valueOf(cell)).toEither.left.map(e => DecoderError(e.getMessage))

  // Here we define a manual decoder for each row in our CSV
  given csvRowDecoder: CsvRowDecoder[NewFooItem, String] with
    def apply(row: CsvRow[String]): DecoderResult[NewFooItem] =
      for
        nam <- row.as[String]("name")
        txt <- row.as[String]("text")
        typ <- row.as[FooItemType]("type")
      yield
        NewFooItem(FooItemName(nam), FooItemText(txt), typ)
