# Gamification Service

## Description

The Gamification Service primarily focuses on the following core responsibilities:
- Calculating the Player Hexad Score if the results of the questionaire

## Environment variables
### Relevant for deployment
| Name                       | Description                        | Value in Dev Environment                               | Value in Prod Environment                           |
|----------------------------|------------------------------------|--------------------------------------------------------|-----------------------------------------------------|
| spring.datasource.url      | PostgreSQL database URL            | jdbc:postgresql://localhost:5432/gamification_service  | jdbc:postgresql://localhost:1032/${database_name}   |
| spring.datasource.username | Database username                  | root                                                   | gits                                                |
| spring.datasource.password | Database password                  | root                                                   | *secret*                                            |
| DAPR_HTTP_PORT             | Dapr HTTP Port                     | 1200                                                   | 3500                                                |
| server.port                | Port on which the application runs | 1201                                                   | 2001                                                |

### Other properties
| Name                                    | Description                               | Value in Dev Environment                | Value in Prod Environment               |
|-----------------------------------------|-------------------------------------------|-----------------------------------------|-----------------------------------------|
| spring.graphql.graphiql.enabled         | Enable GraphiQL web interface for GraphQL | true                                    | true                                    |
| spring.graphql.graphiql.path            | Path for GraphiQL when enabled            | /graphiql                               | /graphiql                               |
| spring.profiles.active                  | Active Spring profile                     | dev                                     | prod                                    |
| spring.jpa.properties.hibernate.dialect | Hibernate dialect for PostgreSQL          | org.hibernate.dialect.PostgreSQLDialect | org.hibernate.dialect.PostgreSQLDialect |
| spring.sql.init.mode                    | SQL initialization mode                   | always                                  | always                                  |
| spring.jpa.show-sql                     | Show SQL queries in logs                  | true                                    | false                                   |
| spring.sql.init.continue-on-error       | Continue on SQL init error                | true                                    | true                                    |
| spring.jpa.hibernate.ddl-auto           | Hibernate DDL auto strategy               | create                                  | update                                  |
| DAPR_GRPC_PORT                          | Dapr gRPC Port                            | -                                       | 50001                                   |

## GraphQL API

The API documentation can be found in the wiki in the [API docs](api.md).

The API is available at `/graphql` and the GraphiQL interface is available at `/graphiql`.

## Package Structure – `de.unistuttgart.iste.meitrex.gamification_service.service`

The business logic of the Gamification Service is organized in the package  
`de.unistuttgart.iste.meitrex.gamification_service.service`.  
To ensure a clear architecture and separation of concerns, this package is divided into several **subpackages**:

| Package                                                                                              | Purpose                                                                                                                                                                                                 |
|-------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `de.unistuttgart.iste.meitrex.gamification_service.service.internal`                                  | Contains **implementations of business logic** that are **not directly exposed via GraphQL**. These classes encapsulate internal application logic and are typically resolvers or event listeners.                                |
| `de.unistuttgart.iste.meitrex.gamification_service.service.functional`                                | Contains **functional, idempotent utility functions** that have no side effects. These classes and methods are usually stateless and can safely be reused throughout the application.                                                        |
| `de.unistuttgart.iste.meitrex.gamification_service.service.reactive`                                  | Contains **event listeners** that extend `de.unistuttgart.iste.meitrex.gamification_service.events.internal.AbstractInternalListener`. These listeners react to **application events**, especially **external events that have been translated into internal events**. |

### Implementation Guidelines
- **`internal`**: Use this package for services or components that implement complex business rules or calculations. These components may be used by various GraphQL resolvers or event listeners, but they should not handle transport or DTO transformations.
- **`functional`**: Use this package for small, easily testable functions that perform calculations, formatting, or transformations without modifying the application state.
- **`reactive`**: Listeners in this package are triggered via Spring Application Events and handle reactions to internal event streams. Typically, these correspond to **external events** that have been converted into **internal events** by a separate adapter layer.

## Processing External Events

External events are processed in three steps to ensure durability:

1. **Reception & Mapping**  
   External events are received through Dapr pub/sub endpoints by listeners extending  
   `de.unistuttgart.iste.meitrex.gamification_service.dapr.AbstractExternalListener<T>`.  
   These map incoming `CloudEvent<T>` payloads to `PersistentEvent` objects and pass them to the `IEventPublicationService`.

2. **Persistence & Publication**  
   The `DefaultEventPublicationService` persists each `PersistentEvent` and publishes a corresponding `InternalEvent` after the surrounding transaction commits.  
   Duplicate events (based on sequence numbers) are ignored.

3. **Internal Event Handling**  
   Internal events (`de.unistuttgart.iste.meitrex.gamification_service.events.internal.InternalEvent`) are processed asynchronously by listeners extending  
   `de.unistuttgart.iste.meitrex.gamification_service.events.internal.AbstractInternalListener<U,V>`.  
   These listeners:
    - Load the persistent event from the database,
    - Track processing status and retry attempts,
    - Execute business logic in `doProcess(U persistentEvent)`,
    - Distinguish between transient and non-transient failures.

**Example Flow**:  
`CloudEvent<T>` → `AbstractExternalListener` → `PersistentEvent` stored → `InternalEvent` published → `AbstractInternalListener` processes domain logic.

### Implementing a Custom Internal Listener

To react to new internal events, developers must:

1. **Create a `PersistentEvent` subclass** representing the data structure to be stored for the new external event.
2. **Extend `AbstractExternalListener<T>`** to receive and map the external event to the new `PersistentEvent`.
3. **Register the new event type** in `DefaultEventPublicationService` by adding a mapping from the new `PersistentEvent` class to a function that saves it and creates a corresponding `InternalEvent`.
4. **Create an `InternalEvent` subclass** that references the persisted event’s UUID.
5. **Implement a new listener** by extending `AbstractInternalListener<YourPersistentEvent, YourInternalEvent>` and overriding:
    - `getName()` with a unique, stable identifier,
    - `doProcess(...)` with the actual business logic to run when the event is processed.
6. Optionally throw `TransientEventListenerException` for retryable errors or `NonTransientEventListenerException` for permanent failures.

After these steps, the new internal listener will automatically react to published internal events of the specified type.


## Get started
A guide how to start development can be
found in the [wiki](https://meitrex.readthedocs.io/en/latest/dev-manuals/backend/get-started.html).