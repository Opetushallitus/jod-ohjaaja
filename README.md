# JOD Ohjaaja Backend

Part of the [Digital Service Ecosystem for Continuous Learning (JOD) project](https://wiki.eduuni.fi/pages/viewpage.action?pageId=404882394).

---

Copyright (c) 2025 The Finnish Ministry of Education and Culture, The Finnish
The Ministry of Economic Affairs and Employment, The Finnish National Agency of
Education (Opetushallitus) and The Finnish Development and Administration centre
for ELY Centres and TE Offices (KEHA).

Licensed under the European Union Public Licence EUPL-1.2 or later.

---

## Getting Started

The backend application is a Spring Boot 3 application that requires Java 21 and uses Gradle
as a build tool.

* Install a OpenJDK 21 distribution (e.g. [Eclipse Temurin™](https://adoptium.net/temurin/releases/)).
* Install Docker and Docker compose
* Clone the repository.
* Run the application with `./gradlew bootRun`.
* The application should be shortly available at `http://localhost:8080`.

## Development

* Build and test application with `./gradlew build`
* Code style is enforced using Spotless and Checkstyle (based on Google Java Style).
  * You can format the code with `./gradlew spotlessApply`.
  * If using IntelliJ IDEA, the Checkstyle-IDEA and google-java-format plugins are recommended.

### Database migrations

The database migrations are managed with Flyway, located in the
`src/main/resources/db/migration` directory. The migrations should be named in the format
`V<yyymmdd>.<id>__<description>.sql`, where `<id>` is the Jira issue number of the migration, and
`<description>` is a short description of the migration. For example,
`V20250708.1234__add_new_table.sql`.
