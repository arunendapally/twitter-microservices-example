package infrastructure.confluentschemaregistry

import api._
import io.confluent.kafka.schemaregistry.client.{SchemaRegistryClient, MockSchemaRegistryClient, CachedSchemaRegistryClient}
import io.confluent.kafka.serializers.{KafkaAvroSerializer, KafkaAvroDeserializer}
import io.confluent.kafka.serializers.{KafkaAvroDeserializerConfig, AbstractKafkaAvroSerDeConfig}
import scala.collection.JavaConversions._
import org.apache.avro.generic.GenericContainer
import org.apache.avro.generic.GenericRecord
import org.apache.avro.specific.SpecificRecord
import scala.collection.mutable

trait ConfluentAvroSerde {
  def schemaRegistryClient: SchemaRegistryClient
  def isKey: Boolean
  def createConfigs = Map.empty[String, String]
  val configs = createConfigs
}

/*
serializer needs to convert T to some type the confluent serializer can handle
  - basically a primitive or GenericContainer
  - confluent uses Object type, so just T => Object

primitives don't need any conversion
  - have one impl that does no conversion, and just passes object through directly
  - good for message keys, which are typically String

non-primitives do need conversion
  - e.g. case classes
  - T => GenericContainer
  - ^^ produces everything that confluent serializer can handle
  - impls can use any Scala/Avro tool, or could even populate a GenericRecord manually
*/

trait ConfluentAvroSerializer extends ConfluentAvroSerde {
  lazy val kafkaAvroSerializer = {
    val s = new KafkaAvroSerializer(schemaRegistryClient)
    s.configure(configs, isKey)
    s
  }
}

trait ConfluentAvroPrimitiveSerializer[T] extends ConfluentAvroSerializer with Serializer[T] {
  def topic: String
  override def toBytes(t: T): Array[Byte] = kafkaAvroSerializer.serialize(topic, t)
}

class RealConfluentAvroPrimitiveSerializer[T](schemaRegistryUrl: String, val topic: String, val isKey: Boolean) extends ConfluentAvroPrimitiveSerializer[T] {
  override lazy val schemaRegistryClient: SchemaRegistryClient = new CachedSchemaRegistryClient(schemaRegistryUrl, 1000)
  override def createConfigs = super.createConfigs ++ Map(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG -> schemaRegistryUrl)
}

trait GenericContainerWriter[T] {
  def write(t: T): GenericContainer
}

abstract class ConfluentAvroGenericSerializer[T : GenericContainerWriter] extends ConfluentAvroSerializer with Serializer[T] {
  def topic: String
  lazy val writer = implicitly[GenericContainerWriter[T]]
  override def toBytes(t: T): Array[Byte] = kafkaAvroSerializer.serialize(topic, writer.write(t))
}

class RealConfluentAvroGenericSerializer[T : GenericContainerWriter](schemaRegistryUrl: String, val topic: String, val isKey: Boolean) extends ConfluentAvroGenericSerializer[T] {
  override lazy val schemaRegistryClient: SchemaRegistryClient = new CachedSchemaRegistryClient(schemaRegistryUrl, 1000)
  override def createConfigs = super.createConfigs ++ Map(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG -> schemaRegistryUrl)
}

/*
confluent deserializer will return either:
  - primitive (String, Int, Array[Byte], etc)
  - avro object: should be GenericRecord or SpecificRecord

primitive objects can be returned from deserializer as-is
  - no need to convert to anything else
  - at most, just needs a cast to desired type T

avro objects need to be converted
  - IndexedRecord => T
  - or do we need separate GenericRecord => T and SpecificRecord => T?
*/

trait ConfluentAvroDeserializer[T] extends ConfluentAvroSerde with Deserializer[T] {
  lazy val kafkaAvroDeserializer = {
    val d = new KafkaAvroDeserializer(schemaRegistryClient)
    d.configure(configs, isKey)
    d
  }
  def deserialize(bytes: Array[Byte]): Any = kafkaAvroDeserializer.deserialize(null, bytes) //it ignores topic name argument, so just use null
  override def fromBytes(bytes: Array[Byte]): T = deserialize(bytes).asInstanceOf[T]
}

trait GenericRecordReader[T] {
  def read(record: GenericRecord): T
}

trait SpecificRecordReader[R <: SpecificRecord, T] {
  def read(record: R): T
}

abstract class ConfluentGenericRecordDeserializer[T : GenericRecordReader] extends ConfluentAvroDeserializer[T] {
  lazy val reader = implicitly[GenericRecordReader[T]]
  override def fromBytes(bytes: Array[Byte]): T = reader.read(deserialize(bytes).asInstanceOf[GenericRecord])
}

abstract class ConfluentSpecificRecordDeserializer[R <: SpecificRecord, T](implicit reader: SpecificRecordReader[R, T]) extends ConfluentAvroDeserializer[T] {
  override def createConfigs = super.createConfigs ++ Map(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG -> "true")
  override def fromBytes(bytes: Array[Byte]): T = reader.read(deserialize(bytes).asInstanceOf[R])
}

class RealConfluentGenericRecordDeserializer[T : GenericRecordReader](schemaRegistryUrl: String, val isKey: Boolean) extends ConfluentGenericRecordDeserializer[T] {
  override lazy val schemaRegistryClient: SchemaRegistryClient = new CachedSchemaRegistryClient(schemaRegistryUrl, 1000)
  override def createConfigs = super.createConfigs ++ Map(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG -> schemaRegistryUrl)
}

class RealConfluentSpecificRecordDeserializer[R <: SpecificRecord, T](schemaRegistryUrl: String, val isKey: Boolean)(implicit reader: SpecificRecordReader[R, T]) extends ConfluentSpecificRecordDeserializer[R, T] {
  override lazy val schemaRegistryClient: SchemaRegistryClient = new CachedSchemaRegistryClient(schemaRegistryUrl, 1000)
  override def createConfigs = super.createConfigs ++ Map(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG -> schemaRegistryUrl)
}

trait TestConfluentAvroSerde extends ConfluentAvroSerde {
  override val schemaRegistryClient = TestConfluentAvroSerde.SchemaRegistryClient
  override def createConfigs = super.createConfigs ++ Map("schema.registry.url" -> "http://nothing.com")
}

object TestConfluentAvroSerde {
  val SchemaRegistryClient = new MockSchemaRegistryClient()
}

class TestConfluentAvroPrimitiveSerializer[T](val topic: String, val isKey: Boolean) extends ConfluentAvroPrimitiveSerializer[T] with TestConfluentAvroSerde
class TestConfluentAvroGenericSerializer[T : GenericContainerWriter](val topic: String, val isKey: Boolean) extends ConfluentAvroGenericSerializer[T] with TestConfluentAvroSerde

class TestConfluentAvroDeserializer[T](val isKey: Boolean) extends TestConfluentAvroSerde with ConfluentAvroDeserializer[T]
class TestConfluentGenericRecordDeserializer[T : GenericRecordReader](val isKey: Boolean) extends ConfluentGenericRecordDeserializer[T] with TestConfluentAvroSerde
class TestConfluentSpecificRecordDeserializer[R <: SpecificRecord, T](val isKey: Boolean)(implicit reader: SpecificRecordReader[R, T]) extends ConfluentSpecificRecordDeserializer[R, T] with TestConfluentAvroSerde

import domain.User
import infrastructure.avro.{User => AvroUser}
object GenericRecordUserWriter extends GenericContainerWriter[User] {
  import org.apache.avro._
  import org.apache.avro.generic._
  override def write(user: User): GenericContainer = {
    // val schema = new Schema.Parser().parse(getClass.getResourceAsStream("/user.avsc"))
    val schema = new Schema.Parser().parse(new java.io.File("src/main/avro/user.avsc"))
    val record = new GenericData.Record(schema)
    record.put("user_id", user.userId)
    record.put("username", user.username.getOrElse("")) //TODO handle Option
    record.put("tweet_count", user.tweetCount) //TODO default to 0 in schema
    record
  }
}

object SpecificRecordUserWriter extends GenericContainerWriter[User] {
  override def write(user: User): GenericContainer = 
    new AvroUser(user.userId, user.username.getOrElse(""), user.tweetCount)
}

object GenericRecordUserReader extends GenericRecordReader[User] {
  override def read(record: GenericRecord): User = 
    User.fromUserFields(
      record.get("user_id").toString, 
      record.get("username").toString)
      .withTweetCount(record.get("tweet_count").asInstanceOf[Int])
}

object SpecificRecordUserReader extends SpecificRecordReader[AvroUser, User] {
  override def read(record: AvroUser): User = 
    User.fromUserFields(record.getUserId, record.getUsername).withTweetCount(record.getTweetCount)
}

object AvroUserIdSerializer extends TestConfluentAvroPrimitiveSerializer("user-topic", true)
object AvroUserSerializer extends TestConfluentAvroGenericSerializer[User]("user-topic", false)(GenericRecordUserWriter)
object AvroUserIdDeserializer extends TestConfluentAvroDeserializer[String](true)
object AvroUserDeserializer extends TestConfluentAvroDeserializer[User](false)

/*
Need to handle T => IndexedRecord in here somehow
  - object passed to serialize() above must be an IndexedRecord
  - might want to make this more generic, as it will also be needed elsewhere, not just here

This gets in to Scala/Avro land...
  - one service is putting data into a kafka topic, and n services are consuming that data
    - producer service performs the serialization
    - assume all services using schema registry and avro
    - whatever the producer service puts into kafka topic:
      - key and value schemas are registered with schema registry
      - keys and values are serialized using avro
  - IndexedRecord has 2 impls: GenericRecord and SpecificRecord
    - deserializer will use either GenericDatumReader or SpecificDatumReader based on config value (manual)
    - serializer will use SpecificDatumWriter if object is SpecificRecord, else it will use GenericDatumWriter (automatic)
      - serializer can also serialize a NonRecordContainer
    - can producer and consumer use different generic/specific?
      => yes! see GenericSpecificProducerConsumerSpec
  - services should be loosely coupled
    - serialization format has potential to tightly couple services
    - e.g. if producer service provides library to deserialize into domain objects, they can become tightly coupled
    - ideally you want consumer services to do their own deserialization and conversion to their own domain objects
    - don't want: producer service changes schema, publishes new client lib version, consumer service on old client lib version breaks
    - schema registry and avro forward/backward compatibility is designed to prevent this
    - but don't want to introduce it back via client lib 
    - whatever tech consumer service uses to do avro => domain object, it needs to follow Tolerant Reader pattern

In the specific case of the user info service:
  - producer is the kafka-streams join service
  - consumer is the http service
  - producer needs to define the avro schema, and use it to serialize domain objects into avro bytes
    - this will also send schema for kafka topic to schema registry
  - consumer can get the latest schema for topic from schema registry
    - or provide its own schema? i.e. separate reader and writer schemas...
    - but what about tests in consumer repo? producer's schema is not available here, because we don't use real schema registry
*/