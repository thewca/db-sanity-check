## Setup local database

This batch uses an internal database for handling WCA data and also the batch status.

In case you do not have it installed yet, you will need to get MySQL.

* Install [MySQL 8.0](https://dev.mysql.com/doc/refman/8.0/en/linux-installation.html), and set it up with a user with username "root" with an empty password.

```

sudo mysql -u root
ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY '';

create database wca_development;
```

The database `wca_development` will be populated with WCA data and also data from the batch status. If you want to change password, username or others, make sure to also change on `application-local.properties`.

## How to run it

* Run `mvn clean package` to build an executable

* Execute it with `java -jar -Dspring.profiles.active=local target/db-sanity-check.jar`


