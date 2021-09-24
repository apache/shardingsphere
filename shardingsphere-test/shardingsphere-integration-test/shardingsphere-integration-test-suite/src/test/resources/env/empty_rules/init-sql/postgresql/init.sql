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

CREATE DATABASE encrypt_write_ds_0;
CREATE DATABASE encrypt_write_ds_1;
CREATE DATABASE encrypt_write_ds_2;
CREATE DATABASE encrypt_write_ds_3;
CREATE DATABASE encrypt_write_ds_4;
CREATE DATABASE encrypt_write_ds_5;
CREATE DATABASE encrypt_write_ds_6;
CREATE DATABASE encrypt_write_ds_7;
CREATE DATABASE encrypt_write_ds_8;
CREATE DATABASE encrypt_write_ds_9;
CREATE DATABASE encrypt_read_ds_0;
CREATE DATABASE encrypt_read_ds_1;
CREATE DATABASE encrypt_read_ds_2;
CREATE DATABASE encrypt_read_ds_3;
CREATE DATABASE encrypt_read_ds_4;
CREATE DATABASE encrypt_read_ds_5;
CREATE DATABASE encrypt_read_ds_6;
CREATE DATABASE encrypt_read_ds_7;
CREATE DATABASE encrypt_read_ds_8;
CREATE DATABASE encrypt_read_ds_9;

CREATE DATABASE rdl_test_0;
CREATE DATABASE rdl_test_1;
CREATE DATABASE rdl_test_2;

GRANT ALL PRIVILEGES ON DATABASE encrypt_write_ds_0 TO root;
GRANT ALL PRIVILEGES ON DATABASE encrypt_write_ds_1 TO root;
GRANT ALL PRIVILEGES ON DATABASE encrypt_write_ds_2 TO root;
GRANT ALL PRIVILEGES ON DATABASE encrypt_write_ds_3 TO root;
GRANT ALL PRIVILEGES ON DATABASE encrypt_write_ds_4 TO root;
GRANT ALL PRIVILEGES ON DATABASE encrypt_write_ds_5 TO root;
GRANT ALL PRIVILEGES ON DATABASE encrypt_write_ds_6 TO root;
GRANT ALL PRIVILEGES ON DATABASE encrypt_write_ds_7 TO root;
GRANT ALL PRIVILEGES ON DATABASE encrypt_write_ds_8 TO root;
GRANT ALL PRIVILEGES ON DATABASE encrypt_write_ds_9 TO root;
GRANT ALL PRIVILEGES ON DATABASE encrypt_read_ds_0 TO root;
GRANT ALL PRIVILEGES ON DATABASE encrypt_read_ds_1 TO root;
GRANT ALL PRIVILEGES ON DATABASE encrypt_read_ds_2 TO root;
GRANT ALL PRIVILEGES ON DATABASE encrypt_read_ds_3 TO root;
GRANT ALL PRIVILEGES ON DATABASE encrypt_read_ds_4 TO root;
GRANT ALL PRIVILEGES ON DATABASE encrypt_read_ds_5 TO root;
GRANT ALL PRIVILEGES ON DATABASE encrypt_read_ds_6 TO root;
GRANT ALL PRIVILEGES ON DATABASE encrypt_read_ds_7 TO root;
GRANT ALL PRIVILEGES ON DATABASE encrypt_read_ds_8 TO root;
GRANT ALL PRIVILEGES ON DATABASE encrypt_read_ds_9 TO root;
GRANT ALL PRIVILEGES ON DATABASE rdl_test_0 TO root;
GRANT ALL PRIVILEGES ON DATABASE rdl_test_1 TO root;
GRANT ALL PRIVILEGES ON DATABASE rdl_test_2 TO root;

\c encrypt_write_ds_0

DROP TABLE IF EXISTS t_user_0;
DROP TABLE IF EXISTS t_user_item_0;
DROP TABLE IF EXISTS t_user_1;
DROP TABLE IF EXISTS t_user_item_1;
DROP TABLE IF EXISTS t_user_2;
DROP TABLE IF EXISTS t_user_item_2;
DROP TABLE IF EXISTS t_user_3;
DROP TABLE IF EXISTS t_user_item_3;
DROP TABLE IF EXISTS t_single_table;

CREATE TABLE t_user_0 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_0 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_1 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_1 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_2 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_2 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_3 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_3 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_single_table (single_id INT NOT NULL, id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (single_id));
CREATE INDEX user_index_t_user_0 ON t_user_0 (user_id);
CREATE INDEX user_index_t_user_1 ON t_user_1 (user_id);
CREATE INDEX user_index_t_user_2 ON t_user_2 (user_id);
CREATE INDEX user_index_t_user_3 ON t_user_3 (user_id);

\c encrypt_write_ds_1

DROP TABLE IF EXISTS t_user_0;
DROP TABLE IF EXISTS t_user_item_0;
DROP TABLE IF EXISTS t_user_1;
DROP TABLE IF EXISTS t_user_item_1;
DROP TABLE IF EXISTS t_user_2;
DROP TABLE IF EXISTS t_user_item_2;
DROP TABLE IF EXISTS t_user_3;
DROP TABLE IF EXISTS t_user_item_3;

CREATE TABLE t_user_0 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_0 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_1 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_1 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_2 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_2 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_3 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_3 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE INDEX user_index_t_user_0 ON t_user_0 (user_id);
CREATE INDEX user_index_t_user_1 ON t_user_1 (user_id);
CREATE INDEX user_index_t_user_2 ON t_user_2 (user_id);
CREATE INDEX user_index_t_user_3 ON t_user_3 (user_id);

\c encrypt_write_ds_2

DROP TABLE IF EXISTS t_user_0;
DROP TABLE IF EXISTS t_user_item_0;
DROP TABLE IF EXISTS t_user_1;
DROP TABLE IF EXISTS t_user_item_1;
DROP TABLE IF EXISTS t_user_2;
DROP TABLE IF EXISTS t_user_item_2;
DROP TABLE IF EXISTS t_user_3;
DROP TABLE IF EXISTS t_user_item_3;

CREATE TABLE t_user_0 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_0 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_1 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_1 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_2 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_2 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_3 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_3 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE INDEX user_index_t_user_0 ON t_user_0 (user_id);
CREATE INDEX user_index_t_user_1 ON t_user_1 (user_id);
CREATE INDEX user_index_t_user_2 ON t_user_2 (user_id);
CREATE INDEX user_index_t_user_3 ON t_user_3 (user_id);

\c encrypt_write_ds_3

DROP TABLE IF EXISTS t_user_0;
DROP TABLE IF EXISTS t_user_item_0;
DROP TABLE IF EXISTS t_user_1;
DROP TABLE IF EXISTS t_user_item_1;
DROP TABLE IF EXISTS t_user_2;
DROP TABLE IF EXISTS t_user_item_2;
DROP TABLE IF EXISTS t_user_3;
DROP TABLE IF EXISTS t_user_item_3;

CREATE TABLE t_user_0 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_0 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_1 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_1 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_2 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_2 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_3 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_3 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE INDEX user_index_t_user_0 ON t_user_0 (user_id);
CREATE INDEX user_index_t_user_1 ON t_user_1 (user_id);
CREATE INDEX user_index_t_user_2 ON t_user_2 (user_id);
CREATE INDEX user_index_t_user_3 ON t_user_3 (user_id);

\c encrypt_write_ds_4

DROP TABLE IF EXISTS t_user_0;
DROP TABLE IF EXISTS t_user_item_0;
DROP TABLE IF EXISTS t_user_1;
DROP TABLE IF EXISTS t_user_item_1;
DROP TABLE IF EXISTS t_user_2;
DROP TABLE IF EXISTS t_user_item_2;
DROP TABLE IF EXISTS t_user_3;
DROP TABLE IF EXISTS t_user_item_3;

CREATE TABLE t_user_0 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_0 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_1 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_1 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_2 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_2 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_3 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_3 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE INDEX user_index_t_user_0 ON t_user_0 (user_id);
CREATE INDEX user_index_t_user_1 ON t_user_1 (user_id);
CREATE INDEX user_index_t_user_2 ON t_user_2 (user_id);
CREATE INDEX user_index_t_user_3 ON t_user_3 (user_id);

\c encrypt_write_ds_5

DROP TABLE IF EXISTS t_user_0;
DROP TABLE IF EXISTS t_user_item_0;
DROP TABLE IF EXISTS t_user_1;
DROP TABLE IF EXISTS t_user_item_1;
DROP TABLE IF EXISTS t_user_2;
DROP TABLE IF EXISTS t_user_item_2;
DROP TABLE IF EXISTS t_user_3;
DROP TABLE IF EXISTS t_user_item_3;

CREATE TABLE t_user_0 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_0 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_1 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_1 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_2 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_2 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_3 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_3 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE INDEX user_index_t_user_0 ON t_user_0 (user_id);
CREATE INDEX user_index_t_user_1 ON t_user_1 (user_id);
CREATE INDEX user_index_t_user_2 ON t_user_2 (user_id);
CREATE INDEX user_index_t_user_3 ON t_user_3 (user_id);

\c encrypt_write_ds_6

DROP TABLE IF EXISTS t_user_0;
DROP TABLE IF EXISTS t_user_item_0;
DROP TABLE IF EXISTS t_user_1;
DROP TABLE IF EXISTS t_user_item_1;
DROP TABLE IF EXISTS t_user_2;
DROP TABLE IF EXISTS t_user_item_2;
DROP TABLE IF EXISTS t_user_3;
DROP TABLE IF EXISTS t_user_item_3;

CREATE TABLE t_user_0 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_0 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_1 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_1 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_2 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_2 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_3 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_3 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE INDEX user_index_t_user_0 ON t_user_0 (user_id);
CREATE INDEX user_index_t_user_1 ON t_user_1 (user_id);
CREATE INDEX user_index_t_user_2 ON t_user_2 (user_id);
CREATE INDEX user_index_t_user_3 ON t_user_3 (user_id);

\c encrypt_write_ds_7

DROP TABLE IF EXISTS t_user_0;
DROP TABLE IF EXISTS t_user_item_0;
DROP TABLE IF EXISTS t_user_1;
DROP TABLE IF EXISTS t_user_item_1;
DROP TABLE IF EXISTS t_user_2;
DROP TABLE IF EXISTS t_user_item_2;
DROP TABLE IF EXISTS t_user_3;
DROP TABLE IF EXISTS t_user_item_3;

CREATE TABLE t_user_0 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_0 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_1 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_1 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_2 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_2 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_3 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_3 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE INDEX user_index_t_user_0 ON t_user_0 (user_id);
CREATE INDEX user_index_t_user_1 ON t_user_1 (user_id);
CREATE INDEX user_index_t_user_2 ON t_user_2 (user_id);
CREATE INDEX user_index_t_user_3 ON t_user_3 (user_id);

\c encrypt_write_ds_8

DROP TABLE IF EXISTS t_user_0;
DROP TABLE IF EXISTS t_user_item_0;
DROP TABLE IF EXISTS t_user_1;
DROP TABLE IF EXISTS t_user_item_1;
DROP TABLE IF EXISTS t_user_2;
DROP TABLE IF EXISTS t_user_item_2;
DROP TABLE IF EXISTS t_user_3;
DROP TABLE IF EXISTS t_user_item_3;

CREATE TABLE t_user_0 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_0 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_1 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_1 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_2 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_2 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_3 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_3 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE INDEX user_index_t_user_0 ON t_user_0 (user_id);
CREATE INDEX user_index_t_user_1 ON t_user_1 (user_id);
CREATE INDEX user_index_t_user_2 ON t_user_2 (user_id);
CREATE INDEX user_index_t_user_3 ON t_user_3 (user_id);

\c encrypt_write_ds_9

DROP TABLE IF EXISTS t_user_0;
DROP TABLE IF EXISTS t_user_item_0;
DROP TABLE IF EXISTS t_user_1;
DROP TABLE IF EXISTS t_user_item_1;
DROP TABLE IF EXISTS t_user_2;
DROP TABLE IF EXISTS t_user_item_2;
DROP TABLE IF EXISTS t_user_3;
DROP TABLE IF EXISTS t_user_item_3;

CREATE TABLE t_user_0 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_0 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_1 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_1 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_2 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_2 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_3 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_3 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE INDEX user_index_t_user_0 ON t_user_0 (user_id);
CREATE INDEX user_index_t_user_1 ON t_user_1 (user_id);
CREATE INDEX user_index_t_user_2 ON t_user_2 (user_id);
CREATE INDEX user_index_t_user_3 ON t_user_3 (user_id);

\c encrypt_read_ds_0

DROP TABLE IF EXISTS t_user_0;
DROP TABLE IF EXISTS t_user_item_0;
DROP TABLE IF EXISTS t_user_1;
DROP TABLE IF EXISTS t_user_item_1;
DROP TABLE IF EXISTS t_user_2;
DROP TABLE IF EXISTS t_user_item_2;
DROP TABLE IF EXISTS t_user_3;
DROP TABLE IF EXISTS t_user_item_3;

CREATE TABLE t_user_0 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_0 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_1 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_1 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_2 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_2 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_3 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_3 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_single_table (single_id INT NOT NULL, id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (single_id));
CREATE INDEX user_index_t_user_0 ON t_user_0 (user_id);
CREATE INDEX user_index_t_user_1 ON t_user_1 (user_id);
CREATE INDEX user_index_t_user_2 ON t_user_2 (user_id);
CREATE INDEX user_index_t_user_3 ON t_user_3 (user_id);

\c encrypt_read_ds_1

DROP TABLE IF EXISTS t_user_0;
DROP TABLE IF EXISTS t_user_item_0;
DROP TABLE IF EXISTS t_user_1;
DROP TABLE IF EXISTS t_user_item_1;
DROP TABLE IF EXISTS t_user_2;
DROP TABLE IF EXISTS t_user_item_2;
DROP TABLE IF EXISTS t_user_3;
DROP TABLE IF EXISTS t_user_item_3;


CREATE TABLE t_user_0 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_0 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_1 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_1 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_2 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_2 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_3 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_3 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE INDEX user_index_t_user_0 ON t_user_0 (user_id);
CREATE INDEX user_index_t_user_1 ON t_user_1 (user_id);
CREATE INDEX user_index_t_user_2 ON t_user_2 (user_id);
CREATE INDEX user_index_t_user_3 ON t_user_3 (user_id);

\c encrypt_read_ds_2

DROP TABLE IF EXISTS t_user_0;
DROP TABLE IF EXISTS t_user_item_0;
DROP TABLE IF EXISTS t_user_1;
DROP TABLE IF EXISTS t_user_item_1;
DROP TABLE IF EXISTS t_user_2;
DROP TABLE IF EXISTS t_user_item_2;
DROP TABLE IF EXISTS t_user_3;
DROP TABLE IF EXISTS t_user_item_3;

CREATE TABLE t_user_0 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_0 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_1 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_1 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_2 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_2 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_3 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_3 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE INDEX user_index_t_user_0 ON t_user_0 (user_id);
CREATE INDEX user_index_t_user_1 ON t_user_1 (user_id);
CREATE INDEX user_index_t_user_2 ON t_user_2 (user_id);
CREATE INDEX user_index_t_user_3 ON t_user_3 (user_id);

\c encrypt_read_ds_3

DROP TABLE IF EXISTS t_user_0;
DROP TABLE IF EXISTS t_user_item_0;
DROP TABLE IF EXISTS t_user_1;
DROP TABLE IF EXISTS t_user_item_1;
DROP TABLE IF EXISTS t_user_2;
DROP TABLE IF EXISTS t_user_item_2;
DROP TABLE IF EXISTS t_user_3;
DROP TABLE IF EXISTS t_user_item_3;

CREATE TABLE t_user_0 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_0 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_1 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_1 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_2 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_2 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_3 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_3 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE INDEX user_index_t_user_0 ON t_user_0 (user_id);
CREATE INDEX user_index_t_user_1 ON t_user_1 (user_id);
CREATE INDEX user_index_t_user_2 ON t_user_2 (user_id);
CREATE INDEX user_index_t_user_3 ON t_user_3 (user_id);

\c encrypt_read_ds_4

DROP TABLE IF EXISTS t_user_0;
DROP TABLE IF EXISTS t_user_item_0;
DROP TABLE IF EXISTS t_user_1;
DROP TABLE IF EXISTS t_user_item_1;
DROP TABLE IF EXISTS t_user_2;
DROP TABLE IF EXISTS t_user_item_2;
DROP TABLE IF EXISTS t_user_3;
DROP TABLE IF EXISTS t_user_item_3;

CREATE TABLE t_user_0 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_0 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_1 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_1 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_2 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_2 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_3 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_3 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE INDEX user_index_t_user_0 ON t_user_0 (user_id);
CREATE INDEX user_index_t_user_1 ON t_user_1 (user_id);
CREATE INDEX user_index_t_user_2 ON t_user_2 (user_id);
CREATE INDEX user_index_t_user_3 ON t_user_3 (user_id);

\c encrypt_read_ds_5

DROP TABLE IF EXISTS t_user_0;
DROP TABLE IF EXISTS t_user_item_0;
DROP TABLE IF EXISTS t_user_1;
DROP TABLE IF EXISTS t_user_item_1;
DROP TABLE IF EXISTS t_user_2;
DROP TABLE IF EXISTS t_user_item_2;
DROP TABLE IF EXISTS t_user_3;
DROP TABLE IF EXISTS t_user_item_3;

CREATE TABLE t_user_0 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_0 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_1 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_1 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_2 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_2 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_3 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_3 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE INDEX user_index_t_user_0 ON t_user_0 (user_id);
CREATE INDEX user_index_t_user_1 ON t_user_1 (user_id);
CREATE INDEX user_index_t_user_2 ON t_user_2 (user_id);
CREATE INDEX user_index_t_user_3 ON t_user_3 (user_id);

\c encrypt_read_ds_6

DROP TABLE IF EXISTS t_user_0;
DROP TABLE IF EXISTS t_user_item_0;
DROP TABLE IF EXISTS t_user_1;
DROP TABLE IF EXISTS t_user_item_1;
DROP TABLE IF EXISTS t_user_2;
DROP TABLE IF EXISTS t_user_item_2;
DROP TABLE IF EXISTS t_user_3;
DROP TABLE IF EXISTS t_user_item_3;

CREATE TABLE t_user_0 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_0 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_1 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_1 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_2 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_2 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_3 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_3 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE INDEX user_index_t_user_0 ON t_user_0 (user_id);
CREATE INDEX user_index_t_user_1 ON t_user_1 (user_id);
CREATE INDEX user_index_t_user_2 ON t_user_2 (user_id);
CREATE INDEX user_index_t_user_3 ON t_user_3 (user_id);

\c encrypt_read_ds_7

DROP TABLE IF EXISTS t_user_0;
DROP TABLE IF EXISTS t_user_item_0;
DROP TABLE IF EXISTS t_user_1;
DROP TABLE IF EXISTS t_user_item_1;
DROP TABLE IF EXISTS t_user_2;
DROP TABLE IF EXISTS t_user_item_2;
DROP TABLE IF EXISTS t_user_3;
DROP TABLE IF EXISTS t_user_item_3;

CREATE TABLE t_user_0 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_0 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_1 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_1 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_2 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_2 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_3 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_3 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE INDEX user_index_t_user_0 ON t_user_0 (user_id);
CREATE INDEX user_index_t_user_1 ON t_user_1 (user_id);
CREATE INDEX user_index_t_user_2 ON t_user_2 (user_id);
CREATE INDEX user_index_t_user_3 ON t_user_3 (user_id);

\c encrypt_read_ds_8

DROP TABLE IF EXISTS t_user_0;
DROP TABLE IF EXISTS t_user_item_0;
DROP TABLE IF EXISTS t_user_1;
DROP TABLE IF EXISTS t_user_item_1;
DROP TABLE IF EXISTS t_user_2;
DROP TABLE IF EXISTS t_user_item_2;
DROP TABLE IF EXISTS t_user_3;
DROP TABLE IF EXISTS t_user_item_3;

CREATE TABLE t_user_0 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_0 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_1 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_1 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_2 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_2 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_3 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_3 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE INDEX user_index_t_user_0 ON t_user_0 (user_id);
CREATE INDEX user_index_t_user_1 ON t_user_1 (user_id);
CREATE INDEX user_index_t_user_2 ON t_user_2 (user_id);
CREATE INDEX user_index_t_user_3 ON t_user_3 (user_id);

\c encrypt_read_ds_9

DROP TABLE IF EXISTS t_user_0;
DROP TABLE IF EXISTS t_user_item_0;
DROP TABLE IF EXISTS t_user_1;
DROP TABLE IF EXISTS t_user_item_1;
DROP TABLE IF EXISTS t_user_2;
DROP TABLE IF EXISTS t_user_item_2;
DROP TABLE IF EXISTS t_user_3;
DROP TABLE IF EXISTS t_user_item_3;

CREATE TABLE t_user_0 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_0 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_1 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_1 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_2 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_2 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_3 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_3 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE INDEX user_index_t_user_0 ON t_user_0 (user_id);
CREATE INDEX user_index_t_user_1 ON t_user_1 (user_id);
CREATE INDEX user_index_t_user_2 ON t_user_2 (user_id);
CREATE INDEX user_index_t_user_3 ON t_user_3 (user_id);
