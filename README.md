# Sanity Check

The database is very large and we need to run verifications periodically. Instead of running a set of queries manually against a database, this batch runs it and sends the report to the WRT monthly.

[![Build Status](https://travis-ci.com/thewca/db-sanity-check.svg?branch=main)](https://travis-ci.com/github/thewca/db-sanity-check)

[![Coverage Status](https://coveralls.io/repos/github/thewca/db-sanity-check/badge.svg?branch=main)](https://coveralls.io/github/thewca/db-sanity-check?branch=main)

## Setup local database

You should an internal database for handling WCA data.

In case you do not have it installed yet, you will need to get MySQL.

* Install [MySQL 8.0](https://dev.mysql.com/doc/refman/8.0/en/linux-installation.html), and set it up with a user with username "root" with an empty password.

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

* Run `mvn clean package` to build an executable

* Execute it with `java -jar -Dspring.profiles.active=local target/db-sanity-check.jar`

## Project details

This project uses [Maven](https://maven.apache.org/) as the build system. It was built using [Spring Boot](https://spring.io/projects/spring-boot), an awesome framework also for building batches/jobs.

