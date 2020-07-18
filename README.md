`sudo mysql`

`create user 'wca_batch'@'localhost' identified by 'password';`

`create database wca_batch;`

`GRANT ALL ON wca_batch.* TO 'wca_batch'@'localhost';`
