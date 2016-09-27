package infrastructure.confluentschemaregistry

import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import domain.User
import infrastructure.avro.{User => AvroUser}

object GenericSpecificProducerConsumerSpec extends Specification {
  "Confluent Avro serdes" should {
    "producer serializes GenericRecord and consumer deserializes GenericRecord" in new context {
      val bytes = genericSerializer.toBytes(user1)
      genericDeserializer.fromBytes(bytes) must_== user1
    }

    "producer serializes GenericRecord and consumer deserializes SpecificRecord" in new context {
      val bytes = genericSerializer.toBytes(user1)
      specificDeserializer.fromBytes(bytes) must_== user1
    }

    "producer serializes SpecificRecord and consumer deserializes GenericRecord" in new context {
      val bytes = specificSerializer.toBytes(user1)
      genericDeserializer.fromBytes(bytes) must_== user1
    }

    "producer serializes SpecificRecord and consumer deserializes SpecificRecord" in new context {
      val bytes = specificSerializer.toBytes(user1)
      specificDeserializer.fromBytes(bytes) must_== user1
    }
  }

  trait context extends Scope {
    val user1 = User.fromUserFields("user1", "user1").withTweetCount(10)
    val genericSerializer = new TestConfluentAvroGenericSerializer[User]("test-users", false)(GenericRecordUserWriter)
    val specificSerializer = new TestConfluentAvroGenericSerializer[User]("test-users", false)(SpecificRecordUserWriter)
    val genericDeserializer = new TestConfluentGenericRecordDeserializer[User](false)(GenericRecordUserReader)
    val specificDeserializer = new TestConfluentSpecificRecordDeserializer[AvroUser, User](false)(SpecificRecordUserReader)
  }
}
