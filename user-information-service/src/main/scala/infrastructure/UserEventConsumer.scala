package infrastructure

/*
have a separate XEventConsumer for each X
  - user
  - tweet
  - follow
  - like

Each one consumes different kafka topic
Uses UserWriteRepository to update user storage
*/

trait UserEventConsumer
