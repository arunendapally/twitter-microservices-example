package infrastructure.kafka

import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import org.apache.kafka.test.ProcessorTopologyTestDriver
import mwt.mockedstreams.MockedStreams
import io.confluent.kafka.serializers.KafkaAvroSerializer

import org.apache.kafka.common.serialization.Serde
class KafkaAvroSerde extends Serde[Any] {
  def close(): Unit = ???
  def configure(x$1: java.util.Map[String, _],x$2: Boolean): Unit = ???
  def deserializer(): org.apache.kafka.common.serialization.Deserializer[Any] = ???
  def serializer(): org.apache.kafka.common.serialization.Serializer[Any] = ???
}

//TODO this test needs to be implemented

object UserInformationJoinServiceSpec extends Specification {
  "User information join service" should {
    "build a working topology" in new context {
      val input = Seq(("x", "v1"), ("y", "v2"))
      val exp = Seq(("x", "V1"), ("y", "V2"))
      val keySerde = new KafkaAvroSerde()
      val valueSerde = new KafkaAvroSerde()

      MockedStreams()
        .topology { builder => UserInformationJoinService.build(usersTopic, tweetsTopic, followsTopic, userInformationTopic, builder) }
        .input(usersTopic, keySerde, valueSerde, input)
        .output("topic-out", keySerde, valueSerde, exp.size) shouldEqual exp
    }
  }

  trait context extends Scope {
    val usersTopic = "test.users"
    val tweetsTopic = "test.tweets"
    val followsTopic = "test.follows"
    val userInformationTopic = "test.user-information"
  }
}
