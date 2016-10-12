## Overview

Consumes a [random sample from the Twitter Streaming API](https://dev.twitter.com/streaming/reference/get/statuses/sample) to simulate 
both tweet and user events, which are produced to Kafka topics.

## Running

Set Twitter OAuth credentials in environment variables:

```
export TWITTER_OAUTH_CONSUMER_KEY="fill this in"
export TWITTER_OAUTH_CONSUMER_SECRET="fill this in"
export TWITTER_OAUTH_ACCESS_TOKEN="fill this in"
export TWITTER_OAUTH_ACCESS_TOKEN_SECRET="fill this in"
```

Then just execute `sbt run`.

## Avro Console Consumer

Download the [Confluent Platform](http://www.confluent.io/download/) to get the `kafka-avro-console-consumer` script.

```
bin/kafka-avro-console-consumer \
  --zookeeper zookeeper.local:32181 \
  --property schema.registry.url=http://schema-registry.local:8081 \
  --topic tweet-service.tweets \
  --property print.key=true
```
