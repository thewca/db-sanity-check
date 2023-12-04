# Sanity Check

The database is very large and we need to run verifications periodically. Instead of running a set of queries manually against a database, this batch runs it and sends the report to the WRT monthly.

[![Build](https://github.com/thewca/db-sanity-check/actions/workflows/build.yml/badge.svg)](https://github.com/thewca/db-sanity-check/actions/workflows/build.yml)

## Setup local database

You should an internal database for handling WCA data.

In case you do not have it installed yet, you will need to get MySQL.

- Install [MySQL 8.0](https://dev.mysql.com/doc/refman/8.0/en/linux-installation.html), and set it up with a user with username "root" with an empty password.

```
sudo mysql -u root
ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY '';

create database wca_development;
```

The database `wca_development` will be populated with WCA data. If you want to change password, username or others, make sure to also change on `application-local.properties`.

## Before you run this

You need your copy of the database from WCA. If you already have it (with a user 'root' with no password), you can skip this.

Download [the latest export](https://www.worldcubeassociation.org/wst/wca-developer-database-dump.zip) and execute the sql (as stated in the last step). If you wish, you can execute the file `get_db_export.sh` in this folder.

```
chmod +x get_db_export.sh
./get_db_export.sh
```

It will ask you to run in sudo mode in order to execute the sql.

## How to run it

- IDE: Most recent IDEs can run gradle, use its capabilities.

- Command line: `./gradlew bootRun`

## Tests

Tests use docker database to simulate the whole process.

- Start test database with migrations applied

`docker-compose -f docker-compose-test.yml up -d --build`

- Run tests

`./gradlew test`

_Note_: If you are running on Mac M1, maybe you will have issues starting docker. To try to fix it, you may add `platform: linux/x86_64` to the service `wca-test-db` in the file `docker-compose-test.yml`.

## Project details

This project uses [Gradle](https://gradle.org/) as the build system. It was built using [Spring Boot](https://spring.io/projects/spring-boot), an awesome framework also for building batches/jobs.

If you open the project in an IDE and the build seems to be failing (getters, setters, log...), you may need to install [lombok](https://projectlombok.org/).

## Deploy to production

You'll need the [AWS CLI](https://aws.amazon.com/cli/). 

- Build a jar
`./gradlew build`

- Build a docker image
`docker build -t thewca/db-sanity-check .`

- Push the image
`docker push thewca/db-sanity-check`

- Replace the environment variables in the `cloudformation.yaml`, run in the aws folder

`aws cloudformation deploy --template-file cloudformation.yaml --stack-name sanity-check --capabilities CAPABILITY_IAM`
