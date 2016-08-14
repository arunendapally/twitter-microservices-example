package infrastructure

object Main 
  extends infrastructure.http4s.Http4sService
  with infrastructure.inmemory.InMemoryUserRepositoryWithTestData
