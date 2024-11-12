package foo.dao.doobie

import doobie.Meta
import foo.model.{FooItemId, FooItemName, FooItemType}

private[doobie] given metaFooItemId: Meta[FooItemId] = Meta[Long].imap(FooItemId.apply)(FooItemId.unapplySafe)
private[doobie] given metaFooName: Meta[FooItemName] = Meta[String].imap(FooItemName.apply)(_.value)
private[doobie] given metaFooType: Meta[FooItemType] = Meta[String].imap(FooItemType.valueOf)(_.toString)
