## Setup local database

This batch uses an internal database for handling WCA data and also the batch status.

In case you do not have it installed yet, you will need to get MySQL.

* Install mysql with `sudo apt update && sudo apt install mysql-server`

* Open MySQL, create a database, a user and give permissions.

```sudo mysql

create database wca_batch_db;

create user 'wca_batch_user'@'localhost' identified by 'password';

grant all privileges on wca_batch_db.* to 'wca_batch_user'@'localhost';
```

The database `wca_batch_db` will be populated with WCA data and also data from the batch status. If you want to change password, username or others, make sure to also change on `application-local.properties`.


