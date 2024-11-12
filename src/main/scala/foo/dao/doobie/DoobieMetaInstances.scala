package foo.dao.doobie

import doobie.Meta
import foo.model.{FooItemId, FooItemName, FooItemText, FooItemType}

private[doobie] given metaFooItemId: Meta[FooItemId] =
  Meta[Long].imap(FooItemId.apply) { case FooItemId(id) => id }

private[doobie] given metaFooName: Meta[FooItemName] =
  Meta[String].imap(FooItemName.apply) { case FooItemName(n) => n }

private[doobie] given metaFooText: Meta[FooItemText] =
  Meta[String].imap(FooItemText.apply) { case FooItemText(t) => t }

private[doobie] given metaFooType: Meta[FooItemType] =
  Meta[String].imap(FooItemType.valueOf)(_.toString)
