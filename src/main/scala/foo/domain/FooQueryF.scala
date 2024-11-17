package foo.domain

import foo.domain.model.{FooItem, FooItemId, FooItemsFilter, NewFooItem}

// NOTE: Doesn't seem to make sense on such high level.
// Lower level SQL query would make more sense - especially
// if it would mean proper SQL joins… :upside_down:
sealed trait FooQueryF[R]

object FooQueryF:
  case class Create(inp: NewFooItem) extends FooQueryF[FooItem]
  case class Get(id: FooItemId) extends FooQueryF[Option[FooItem]]
  case class GetList(f: FooItemsFilter) extends FooQueryF[List[FooItem]]
