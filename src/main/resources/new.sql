CREATE DATABASE roomdb;

CREATE USER 'roomapp'@'localhost' IDENTIFIED BY 'roomapp1234';

GRANT ALL PRIVILEGES ON roomdb.* TO 'roomapp'@'localhost';
