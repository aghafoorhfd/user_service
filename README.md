---
# User Service
The User Service is a Spring Boot application that provides user management functionalities, including user creation, update, deletion, and access control based on their subscription.

## Table of Contents

- [Prerequisites](#prerequisites)
- [MySQL DB Local Setup](#mongo-db-local-setup)
- [Run Auth Service](#run-auth-service)
- [Run Subscription Service](#run-subscription-service)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [Build from Command Line](#build-from-command-line)
- [Change Set ID generation for Liquibase](#change-set-id-generation-for-liquibase)


## Prerequisites
Before running the User Service, ensure you have the following prerequisites installed:

- Java Development Kit (JDK) 11 or higher
- MySQL Server and Workbench
- Gradle

### MySQL DB Local Setup:
* Open the Terminal and type **brew update**
* After updating 'Homebrew', type **brew install mysql**
* After downloading Mysql, You can start the MySQL server by running **brew services start mysql**
* To stop mySql services, run **brew services stop mysql**
* **For Ref:**
* **MySql Installation:** https://flaviocopes.com/mysql-how-to-install/
* **Homebrew Installation:** https://treehouse.github.io/installation-guides/mac/homebrew

### User Service Local Setup:
* Open MySQL Workbench
* Run the following SQL script
```
CREATE SCHEMA master;
USE master;
CREATE TABLE `tenant` (
  `id` char(36) NOT NULL,
  `company_name` varchar(100) NOT NULL,
  `client_secret` varchar(100) DEFAULT NULL,
  `public_key` varchar(1000) DEFAULT NULL,
  `is_obsolete` bit(1) NOT NULL DEFAULT b'0',
  `is_delete` bit(1) NOT NULL DEFAULT b'0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `company_name_UNIQUE` (`company_name`)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

LOCK TABLES `tenant` WRITE;
INSERT INTO `tenant` VALUES ('069db80c-e560-44f6-bb69-510e1d3a1156','pmotracker_default',NULL,NULL,_binary '\0',_binary '\0');
UNLOCK TABLES;
```
* The script will create a new schema 'master' and a new table inside that schema, named 'tenant' and will add a default company, so that when user_service gets bootstrapped, it makes connection with the Master database and finds that default company in the tenant table

### Run ActiveMQ
* Execute commands in terminal:
    - **brew install apache-activemq**
    - **brew services start activemq**
* ActiveMQ service should be up at http://127.0.0.1:8161/admin/
* Open portal and enter credentials:
    - username: **admin**
    - password: **admin**

### Build lib_common
Kindly follow this README.md: https://github.com/PMO-Tracker/lib_common/blob/develop/README.md

### Run Auth Service
Kindly follow auth_service README.md, for ref: https://github.com/PMO-Tracker/auth_service/blob/develop/README.md

### Run Subscription Service
Kindly follow auth_service README.md, for ref: https://github.com/PMO-Tracker/subscription_service/blob/develop/README.md

### Getting Started
* Open the terminal and type **git clone git@github.com:PMO-Tracker/user_service.git**
* Open your IDE and import the project.

### Configuration
* Go to application.yml located under **src/main/resources/** .
* Change the following properties values:
    - **spring.datasource.username** = (your mysql username, if not set default will be root)
    - **spring.datasource.password** = (your mysql password, if not set default will be root)
    - **subscription.service.url** = http://localhost:{port}/subscription_service/api/subscriptions
    - **auth.service.url** = http://localhost:{port}/auth_service/api/users
    - **keycloak.auth-server-url** = http://localhost:{port}/ .
    - **keycloak.credentials.secret** = (Keycloak Console -> Clients -> auth-service -> Credentials -> copy client secret).

The Resource Service will now be running locally on `http://localhost:8085`

### Build from Command Line
* Go to the **terminal** tab at the bottom of your IDE.
* To compile, test, and build all jars run **./gradlew build**.
* To start the application run **./gradlew bootRun**.

### Change Set ID generation for Liquibase
- kindly use following website to generate changeset id: https://timestampgenerator.com/1672139527/+05:00
- Select current time and current timezone click submit, copy Timestamp and use it as id of changeset.

