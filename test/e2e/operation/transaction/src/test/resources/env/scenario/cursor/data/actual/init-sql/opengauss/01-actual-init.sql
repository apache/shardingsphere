/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

--
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

CREATE TABLE "t_product" ("id" INT PRIMARY KEY, "product_id" INT NOT NULL, "address_id" INT, "product_name" varchar, "category_id" INT NOT NULL, "price" numeric NOT NULL, "status" varchar, "creation_date" date);
CREATE TABLE "t_order_0" ("id" INT PRIMARY KEY, "order_id" INT, "address_id" INT, "user_id" INT NOT NULL, "status" VARCHAR(45) NULL);
CREATE TABLE "t_order_1" ("id" INT PRIMARY KEY, "order_id" INT, "address_id" INT, "user_id" INT NOT NULL, "status" VARCHAR(45) NULL);
CREATE TABLE "t_order_item_0" ("item_id" INT PRIMARY KEY, "order_id" int NOT NULL, "user_id" int NOT NULL, "status" varchar(50) DEFAULT NULL);
CREATE TABLE "t_order_item_1" ("item_id" INT PRIMARY KEY, "order_id" int NOT NULL, "user_id" int NOT NULL, "status" varchar(50) DEFAULT NULL);
CREATE OR REPLACE VIEW t_order_view_0 AS SELECT * FROM t_order_0;
CREATE OR REPLACE VIEW t_order_view_1 AS SELECT * FROM t_order_1;
CREATE TABLE "account_0"("id" BIGINT, "balance" FLOAT, "transaction_id" INT);
CREATE TABLE "account_1"("id" BIGINT, "balance" FLOAT, "transaction_id" INT);
CREATE TABLE "t_address" ("address_id" INT PRIMARY KEY, "code" VARCHAR(36) DEFAULT NULL, "address" VARCHAR(36) DEFAULT NULL, "city_id" INT, "province_id" INT, "country_id" INT);
CREATE TABLE "t_country" ("country_id" INT PRIMARY KEY, "country_name" VARCHAR, "continent_name" VARCHAR, "creation_date" DATE NOT NULL);
CREATE TABLE "t_province" ("province_id" INT PRIMARY KEY, "country_id" INT, "province_name" VARCHAR, "creation_date" DATE NOT NULL);
CREATE TABLE "t_city" ("city_id" INT PRIMARY KEY, "province_id" INT, "country_id" INT, "city_name" VARCHAR, "creation_date" DATE NOT NULL);

INSERT INTO "t_address" ("address_id", "code", "address", "city_id", "province_id", "country_id") VALUES (1, '1', 'address1', 10101, 101, 1);
INSERT INTO "t_address" ("address_id", "code", "address", "city_id", "province_id", "country_id") VALUES (2, '2', 'address2', 10102, 101, 1);
INSERT INTO "t_address" ("address_id", "code", "address", "city_id", "province_id", "country_id") VALUES (3, '3', 'address3', 10201, 102, 1);
INSERT INTO "t_address" ("address_id", "code", "address", "city_id", "province_id", "country_id") VALUES (4, '4', 'address4', 10202, 102, 1);

INSERT INTO "t_city" ("city_id", "province_id", "country_id", "city_name", "creation_date") VALUES (10101, 101, 1, 'NanJing', '2022-11-02');
INSERT INTO "t_city" ("city_id", "province_id", "country_id", "city_name", "creation_date") VALUES (10102, 101, 1, 'SuZhou', '2022-11-02');
INSERT INTO "t_city" ("city_id", "province_id", "country_id", "city_name", "creation_date") VALUES (10201, 102, 1, 'HangZhou', '2022-11-02');
INSERT INTO "t_city" ("city_id", "province_id", "country_id", "city_name", "creation_date") VALUES (10202, 102, 1, 'NingBo', '2022-11-02');

INSERT INTO "t_province" ("province_id", "country_id", "province_name", "creation_date") VALUES (101, 1, 'JiangSu', '2022-11-01');
INSERT INTO "t_province" ("province_id", "country_id", "province_name", "creation_date") VALUES (102, 1, 'ZheJiang', '2022-11-01');

INSERT INTO "t_country" ("country_id", "country_name", "continent_name", "creation_date") VALUES (1, 'China', 'Asia', '2022-11-01');

INSERT INTO "t_order_0" ("id", "order_id", "address_id", "user_id", "status") VALUES (2, 2, 1, 2, 'OK');
INSERT INTO "t_order_1" ("id", "order_id", "address_id", "user_id", "status") VALUES (1, 1, 2, 2, 'OK');

INSERT INTO "t_product" ("id", "product_id", "address_id", "product_name", "category_id", "price", "status", "creation_date") VALUES (1, 1, 1, 'product1', 1, 1, 'OK', '2022-11-02');
INSERT INTO "t_product" ("id", "product_id", "address_id", "product_name", "category_id", "price", "status", "creation_date") VALUES (2, 2, 2, 'product2', 2, 2, 'OK', '2022-11-02');
INSERT INTO "t_product" ("id", "product_id", "address_id", "product_name", "category_id", "price", "status", "creation_date") VALUES (3, 3, 3, 'product3', 3, 3, 'OK', '2022-11-02');
INSERT INTO "t_product" ("id", "product_id", "address_id", "product_name", "category_id", "price", "status", "creation_date") VALUES (4, 4, 4, 'product4', 4, 4, 'OK', '2022-11-02');

\c
\c ds_1;

CREATE TABLE "t_order_0" ("id" INT PRIMARY KEY, "order_id" INT, "address_id" INT, "user_id" INT NOT NULL, "status" VARCHAR(45) NULL);
CREATE TABLE "t_order_1" ("id" INT PRIMARY KEY, "order_id" INT, "address_id" INT, "user_id" INT NOT NULL, "status" VARCHAR(45) NULL);
CREATE TABLE "t_order_item_0" ("item_id" INT PRIMARY KEY, "order_id" int NOT NULL, "user_id" int NOT NULL, "status" varchar(50) DEFAULT NULL);
CREATE TABLE "t_order_item_1" ("item_id" INT PRIMARY KEY, "order_id" int NOT NULL, "user_id" int NOT NULL, "status" varchar(50) DEFAULT NULL);
CREATE OR REPLACE VIEW t_order_view_0 AS SELECT * FROM t_order_0;
CREATE OR REPLACE VIEW t_order_view_1 AS SELECT * FROM t_order_1;
CREATE TABLE "account_0"("id" BIGINT, "balance" FLOAT, "transaction_id" INT);
CREATE TABLE "account_1"("id" BIGINT, "balance" FLOAT, "transaction_id" INT);
CREATE TABLE "t_address" ("address_id" INT PRIMARY KEY, "code" VARCHAR(36) DEFAULT NULL, "address" VARCHAR(36) DEFAULT NULL, "city_id" INT, "province_id" INT, "country_id" INT);
CREATE TABLE "t_country" ("country_id" INT PRIMARY KEY, "country_name" VARCHAR, "continent_name" VARCHAR, "creation_date" DATE NOT NULL);
CREATE TABLE "t_province" ("province_id" INT PRIMARY KEY, "country_id" INT, "province_name" VARCHAR, "creation_date" DATE NOT NULL);
CREATE TABLE "t_city" ("city_id" INT PRIMARY KEY, "province_id" INT, "country_id" INT, "city_name" VARCHAR, "creation_date" DATE NOT NULL);

INSERT INTO "t_address" ("address_id", "code", "address", "city_id", "province_id", "country_id") VALUES (1, '1', 'address1', 10101, 101, 1);
INSERT INTO "t_address" ("address_id", "code", "address", "city_id", "province_id", "country_id") VALUES (2, '2', 'address2', 10102, 101, 1);
INSERT INTO "t_address" ("address_id", "code", "address", "city_id", "province_id", "country_id") VALUES (3, '3', 'address3', 10201, 102, 1);
INSERT INTO "t_address" ("address_id", "code", "address", "city_id", "province_id", "country_id") VALUES (4, '4', 'address4', 10202, 102, 1);

INSERT INTO "t_city" ("city_id", "province_id", "country_id", "city_name", "creation_date") VALUES (10101, 101, 1, 'NanJing', '2022-11-02');
INSERT INTO "t_city" ("city_id", "province_id", "country_id", "city_name", "creation_date") VALUES (10102, 101, 1, 'SuZhou', '2022-11-02');
INSERT INTO "t_city" ("city_id", "province_id", "country_id", "city_name", "creation_date") VALUES (10201, 102, 1, 'HangZhou', '2022-11-02');
INSERT INTO "t_city" ("city_id", "province_id", "country_id", "city_name", "creation_date") VALUES (10202, 102, 1, 'NingBo', '2022-11-02');

INSERT INTO "t_province" ("province_id", "country_id", "province_name", "creation_date") VALUES (101, 1, 'JiangSu', '2022-11-01');
INSERT INTO "t_province" ("province_id", "country_id", "province_name", "creation_date") VALUES (102, 1, 'ZheJiang', '2022-11-01');

INSERT INTO "t_country" ("country_id", "country_name", "continent_name", "creation_date") VALUES (1, 'China', 'Asia', '2022-11-01');

INSERT INTO "t_order_0" ("id", "order_id", "address_id", "user_id", "status") VALUES (2, 2, 3, 1, 'OK');
INSERT INTO "t_order_1" ("id", "order_id", "address_id", "user_id", "status") VALUES (1, 1, 4, 1, 'OK');
