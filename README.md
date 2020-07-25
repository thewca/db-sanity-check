## Setup local database

This batch uses an internal database for handling WCA data and also the batch status.

If you already followed the instructions to setup the database for [the website](https://github.com/thewca/worldcubeassociation.org#run-directly-with-ruby-lightweight-but-only-runs-the-rails-portions-of-the-site) then you can skip this.

In case you do not have it installed yet, you will need to get MySQL.

* Install mysql with `sudo apt update && sudo apt install mysql-server`

* Open MySQL, create a database, a user and give permissions.

```sudo mysql -u root

create database wca_development;

ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY '';

grant all privileges on wca_batch_db.* to 'wca_batch_user'@'localhost';
```

The database `wca_development` will be populated with WCA data and also data from the batch status. If you want to change password, username or others, make sure to also change on `application-local.properties`.


