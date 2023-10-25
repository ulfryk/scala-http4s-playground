package foo.routes

import cats.data.Validated.{Invalid, Valid}
import cats.effect.IO
import foo.dto.FooItemApiId
import foo.model.FooItemId
import org.http4s.*
import org.http4s.dsl.io.*


val allIds = List(54321, 54322, 6543212, 123413, 1115)

val fooItemsRoutes = HttpRoutes.of[IO] {
  case GET -> Root / "foo-items" =>
    val ids = allIds.map(FooItemId(_).toApiString)
    Ok(s"Hello $ids")

  case GET -> Root / "foo-items" / FooItemApiId(id) =>
    id match
      case Valid(idVal) =>
        if allIds.contains(idVal) then Ok(s"whatever ${idVal.toApiString}")
        else NotFound(s"couldn't find ${idVal.toApiString}")
      case Invalid(es) => BadRequest {
        es.foldLeft("")((txt, next) => txt ++ next.message)
      }


}
