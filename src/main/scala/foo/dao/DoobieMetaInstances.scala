package foo.dao

import doobie.Meta
import foo.model.{FooItemId, FooItemName, FooItemType}

private[dao] given metaFooItemId: Meta[FooItemId] = Meta[Long].imap(FooItemId.apply)(FooItemId.unapplySafe)
private[dao] given metaFooName: Meta[FooItemName] = Meta[String].imap(FooItemName.apply)(_.value)
private[dao] given metaFooType: Meta[FooItemType] = Meta[String].imap(FooItemType.valueOf)(_.toString)
