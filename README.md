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

## Get started
A guide how to start development can be
found in the [wiki](https://meitrex.readthedocs.io/en/latest/dev-manuals/backend/get-started.html).