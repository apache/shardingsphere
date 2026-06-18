--
-- Licensed to the Apache Software Foundation (ASF) under one or more
-- contributor license agreements.  See the NOTICE file distributed with
-- this work for additional information regarding copyright ownership.
-- The ASF licenses this file to You under the Apache License, Version 2.0
-- (the "License"); you may not use this file except in compliance with
-- the License.  You may obtain a copy of the License at
--
--     http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--

DROP DATABASE IF EXISTS ds_0;
DROP DATABASE IF EXISTS ds_1;
DROP DATABASE IF EXISTS ds_2;

CREATE DATABASE ds_0;
CREATE DATABASE ds_1;
CREATE DATABASE ds_2;

GRANT ALL PRIVILEGES ON DATABASE ds_0 TO test_user;
GRANT ALL PRIVILEGES ON DATABASE ds_1 TO test_user;
GRANT ALL PRIVILEGES ON DATABASE ds_2 TO test_user;

\c ds_0;

CREATE TABLE "t_order_0" ("order_id" INT PRIMARY KEY, "user_id" INT NOT NULL, "status" VARCHAR(45) NULL);
CREATE TABLE "t_order_1" ("order_id" INT PRIMARY KEY, "user_id" INT NOT NULL, "status" VARCHAR(45) NULL);
CREATE TABLE "t_order_item_0" ("item_id" INT PRIMARY KEY, "order_id" int NOT NULL, "user_id" int NOT NULL, "status" varchar(50) DEFAULT NULL);
CREATE TABLE "t_order_item_1" ("item_id" INT PRIMARY KEY, "order_id" int NOT NULL, "user_id" int NOT NULL, "status" varchar(50) DEFAULT NULL);
CREATE TABLE "account_0"("id" INT PRIMARY KEY, "balance" FLOAT, "transaction_id" INT);
CREATE TABLE "account_1"("id" INT PRIMARY KEY, "balance" FLOAT, "transaction_id" INT);
CREATE TABLE "t_address" ("id" INT PRIMARY KEY, "code" VARCHAR(36) DEFAULT NULL, "address" VARCHAR(36) DEFAULT NULL);

\c ds_1;

CREATE TABLE "t_order_0" ("order_id" INT PRIMARY KEY, "user_id" INT NOT NULL, "status" VARCHAR(45) NULL);
CREATE TABLE "t_order_1" ("order_id" INT PRIMARY KEY, "user_id" INT NOT NULL, "status" VARCHAR(45) NULL);
CREATE TABLE "t_order_item_0" ("item_id" INT PRIMARY KEY, "order_id" int NOT NULL, "user_id" int NOT NULL, "status" varchar(50) DEFAULT NULL);
CREATE TABLE "t_order_item_1" ("item_id" INT PRIMARY KEY, "order_id" int NOT NULL, "user_id" int NOT NULL, "status" varchar(50) DEFAULT NULL);
CREATE TABLE "account_0"("id" INT PRIMARY KEY, "balance" FLOAT, "transaction_id" INT);
CREATE TABLE "account_1"("id" INT PRIMARY KEY, "balance" FLOAT, "transaction_id" INT);
CREATE TABLE "t_address" ("id" INT PRIMARY KEY, "code" VARCHAR(36) DEFAULT NULL, "address" VARCHAR(36) DEFAULT NULL);

\c ds_2;
CREATE TABLE "t_address" ("id" INT PRIMARY KEY, "code" VARCHAR(36) DEFAULT NULL, "address" VARCHAR(36) DEFAULT NULL);
