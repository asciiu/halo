package models.db

// AUTO-GENERATED Slick data model [2017-02-20T13:48:21.092-07:00[America/Denver]]

/** Stand-alone Slick data model for immediate use */
object Tables extends {
  val profile = utils.db.TetraoPostgresDriver
} with Tables

/** Slick data model trait for extension, choice of backend or usage in the cake pattern. (Make sure to initialize this late.) */
trait Tables {
  val profile: utils.db.TetraoPostgresDriver
  import profile.api._
  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}

  /** DDL for all tables. Call .create to execute. */
  lazy val schema: profile.SchemaDescription = Account.schema ++ Message.schema ++ PlayEvolutions.schema ++ Translations.schema
  @deprecated("Use .schema instead of .ddl", "3.0")
  def ddl = schema

  /** Entity class storing rows of table Account
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param name Database column name SqlType(text)
   *  @param email Database column email SqlType(text)
   *  @param emailConfirmed Database column email_confirmed SqlType(bool)
   *  @param password Database column password SqlType(text)
   *  @param role Database column role SqlType(user_role)
   *  @param createdAt Database column created_at SqlType(timestamptz)
   *  @param updatedAt Database column updated_at SqlType(timestamptz) */
  case class AccountRow(id: Int, name: String, email: String, emailConfirmed: Boolean, password: String, role: models.db.AccountRole.Value, createdAt: java.time.OffsetDateTime, updatedAt: java.time.OffsetDateTime)
  /** GetResult implicit for fetching AccountRow objects using plain SQL queries */
  implicit def GetResultAccountRow(implicit e0: GR[Int], e1: GR[String], e2: GR[Boolean], e3: GR[models.db.AccountRole.Value], e4: GR[java.time.OffsetDateTime]): GR[AccountRow] = GR{
    prs => import prs._
    AccountRow.tupled((<<[Int], <<[String], <<[String], <<[Boolean], <<[String], <<[models.db.AccountRole.Value], <<[java.time.OffsetDateTime], <<[java.time.OffsetDateTime]))
  }
  /** Table description of table users. Objects of this class serve as prototypes for rows in queries. */
  class Account(_tableTag: Tag) extends Table[AccountRow](_tableTag, "users") {
    def * = (id, name, email, emailConfirmed, password, role, createdAt, updatedAt) <> (AccountRow.tupled, AccountRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(name), Rep.Some(email), Rep.Some(emailConfirmed), Rep.Some(password), Rep.Some(role), Rep.Some(createdAt), Rep.Some(updatedAt)).shaped.<>({r=>import r._; _1.map(_=> AccountRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column name SqlType(text) */
    val name: Rep[String] = column[String]("name")
    /** Database column email SqlType(text) */
    val email: Rep[String] = column[String]("email")
    /** Database column email_confirmed SqlType(bool) */
    val emailConfirmed: Rep[Boolean] = column[Boolean]("email_confirmed")
    /** Database column password SqlType(text) */
    val password: Rep[String] = column[String]("password")
    /** Database column role SqlType(user_role) */
    val role: Rep[models.db.AccountRole.Value] = column[models.db.AccountRole.Value]("role")
    /** Database column created_at SqlType(timestamptz) */
    val createdAt: Rep[java.time.OffsetDateTime] = column[java.time.OffsetDateTime]("created_at")
    /** Database column updated_at SqlType(timestamptz) */
    val updatedAt: Rep[java.time.OffsetDateTime] = column[java.time.OffsetDateTime]("updated_at")

    /** Uniqueness Index over (email) (database name users_email_key) */
    val index1 = index("users_email_key", email, unique=true)
  }
  /** Collection-like TableQuery object for table Account */
  lazy val Account = new TableQuery(tag => new Account(tag))

  /** Entity class storing rows of table Message
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param content Database column content SqlType(text)
   *  @param tagList Database column tag_list SqlType(_text), Length(2147483647,false)
   *  @param createdAt Database column created_at SqlType(timestamptz)
   *  @param updatedAt Database column updated_at SqlType(timestamptz) */
  case class MessageRow(id: Int, content: String, tagList: List[String], createdAt: java.time.OffsetDateTime, updatedAt: java.time.OffsetDateTime)
  /** GetResult implicit for fetching MessageRow objects using plain SQL queries */
  implicit def GetResultMessageRow(implicit e0: GR[Int], e1: GR[String], e2: GR[List[String]], e3: GR[java.time.OffsetDateTime]): GR[MessageRow] = GR{
    prs => import prs._
    MessageRow.tupled((<<[Int], <<[String], <<[List[String]], <<[java.time.OffsetDateTime], <<[java.time.OffsetDateTime]))
  }
  /** Table description of table message. Objects of this class serve as prototypes for rows in queries. */
  class Message(_tableTag: Tag) extends Table[MessageRow](_tableTag, "message") {
    def * = (id, content, tagList, createdAt, updatedAt) <> (MessageRow.tupled, MessageRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(content), Rep.Some(tagList), Rep.Some(createdAt), Rep.Some(updatedAt)).shaped.<>({r=>import r._; _1.map(_=> MessageRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column content SqlType(text) */
    val content: Rep[String] = column[String]("content")
    /** Database column tag_list SqlType(_text), Length(2147483647,false) */
    val tagList: Rep[List[String]] = column[List[String]]("tag_list", O.Length(2147483647,varying=false))
    /** Database column created_at SqlType(timestamptz) */
    val createdAt: Rep[java.time.OffsetDateTime] = column[java.time.OffsetDateTime]("created_at")
    /** Database column updated_at SqlType(timestamptz) */
    val updatedAt: Rep[java.time.OffsetDateTime] = column[java.time.OffsetDateTime]("updated_at")
  }
  /** Collection-like TableQuery object for table Message */
  lazy val Message = new TableQuery(tag => new Message(tag))

  /** Entity class storing rows of table PlayEvolutions
   *  @param id Database column id SqlType(int4), PrimaryKey
   *  @param hash Database column hash SqlType(varchar), Length(255,true)
   *  @param appliedAt Database column applied_at SqlType(timestamp)
   *  @param applyScript Database column apply_script SqlType(text), Default(None)
   *  @param revertScript Database column revert_script SqlType(text), Default(None)
   *  @param state Database column state SqlType(varchar), Length(255,true), Default(None)
   *  @param lastProblem Database column last_problem SqlType(text), Default(None) */
  case class PlayEvolutionsRow(id: Int, hash: String, appliedAt: java.time.OffsetDateTime, applyScript: Option[String] = None, revertScript: Option[String] = None, state: Option[String] = None, lastProblem: Option[String] = None)
  /** GetResult implicit for fetching PlayEvolutionsRow objects using plain SQL queries */
  implicit def GetResultPlayEvolutionsRow(implicit e0: GR[Int], e1: GR[String], e2: GR[java.time.OffsetDateTime], e3: GR[Option[String]]): GR[PlayEvolutionsRow] = GR{
    prs => import prs._
    PlayEvolutionsRow.tupled((<<[Int], <<[String], <<[java.time.OffsetDateTime], <<?[String], <<?[String], <<?[String], <<?[String]))
  }
  /** Table description of table play_evolutions. Objects of this class serve as prototypes for rows in queries. */
  class PlayEvolutions(_tableTag: Tag) extends Table[PlayEvolutionsRow](_tableTag, "play_evolutions") {
    def * = (id, hash, appliedAt, applyScript, revertScript, state, lastProblem) <> (PlayEvolutionsRow.tupled, PlayEvolutionsRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(hash), Rep.Some(appliedAt), applyScript, revertScript, state, lastProblem).shaped.<>({r=>import r._; _1.map(_=> PlayEvolutionsRow.tupled((_1.get, _2.get, _3.get, _4, _5, _6, _7)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(int4), PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.PrimaryKey)
    /** Database column hash SqlType(varchar), Length(255,true) */
    val hash: Rep[String] = column[String]("hash", O.Length(255,varying=true))
    /** Database column applied_at SqlType(timestamp) */
    val appliedAt: Rep[java.time.OffsetDateTime] = column[java.time.OffsetDateTime]("applied_at")
    /** Database column apply_script SqlType(text), Default(None) */
    val applyScript: Rep[Option[String]] = column[Option[String]]("apply_script", O.Default(None))
    /** Database column revert_script SqlType(text), Default(None) */
    val revertScript: Rep[Option[String]] = column[Option[String]]("revert_script", O.Default(None))
    /** Database column state SqlType(varchar), Length(255,true), Default(None) */
    val state: Rep[Option[String]] = column[Option[String]]("state", O.Length(255,varying=true), O.Default(None))
    /** Database column last_problem SqlType(text), Default(None) */
    val lastProblem: Rep[Option[String]] = column[Option[String]]("last_problem", O.Default(None))
  }
  /** Collection-like TableQuery object for table PlayEvolutions */
  lazy val PlayEvolutions = new TableQuery(tag => new PlayEvolutions(tag))

  /** Entity class storing rows of table Translations
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param context Database column context SqlType(text)
   *  @param wording Database column wording SqlType(text)
   *  @param normalization Database column normalization SqlType(text)
   *  @param createdAt Database column created_at SqlType(timestamptz)
   *  @param updatedAt Database column updated_at SqlType(timestamptz) */
  case class TranslationsRow(id: Int, context: String, wording: String, normalization: String, createdAt: java.time.OffsetDateTime, updatedAt: java.time.OffsetDateTime)
  /** GetResult implicit for fetching TranslationsRow objects using plain SQL queries */
  implicit def GetResultTranslationsRow(implicit e0: GR[Int], e1: GR[String], e2: GR[java.time.OffsetDateTime]): GR[TranslationsRow] = GR{
    prs => import prs._
    TranslationsRow.tupled((<<[Int], <<[String], <<[String], <<[String], <<[java.time.OffsetDateTime], <<[java.time.OffsetDateTime]))
  }
  /** Table description of table translations. Objects of this class serve as prototypes for rows in queries. */
  class Translations(_tableTag: Tag) extends Table[TranslationsRow](_tableTag, "translations") {
    def * = (id, context, wording, normalization, createdAt, updatedAt) <> (TranslationsRow.tupled, TranslationsRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(context), Rep.Some(wording), Rep.Some(normalization), Rep.Some(createdAt), Rep.Some(updatedAt)).shaped.<>({r=>import r._; _1.map(_=> TranslationsRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column context SqlType(text) */
    val context: Rep[String] = column[String]("context")
    /** Database column wording SqlType(text) */
    val wording: Rep[String] = column[String]("wording")
    /** Database column normalization SqlType(text) */
    val normalization: Rep[String] = column[String]("normalization")
    /** Database column created_at SqlType(timestamptz) */
    val createdAt: Rep[java.time.OffsetDateTime] = column[java.time.OffsetDateTime]("created_at")
    /** Database column updated_at SqlType(timestamptz) */
    val updatedAt: Rep[java.time.OffsetDateTime] = column[java.time.OffsetDateTime]("updated_at")

    /** Uniqueness Index over (normalization) (database name translations_normalization_key) */
    val index1 = index("translations_normalization_key", normalization, unique=true)
  }
  /** Collection-like TableQuery object for table Translations */
  lazy val Translations = new TableQuery(tag => new Translations(tag))
}
