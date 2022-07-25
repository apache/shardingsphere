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

SET character_set_database='utf8';
SET character_set_server='utf8';

DROP DATABASE IF EXISTS encrypt_write_ds_0;
DROP DATABASE IF EXISTS encrypt_write_ds_1;
DROP DATABASE IF EXISTS encrypt_write_ds_2;
DROP DATABASE IF EXISTS encrypt_write_ds_3;
DROP DATABASE IF EXISTS encrypt_write_ds_4;
DROP DATABASE IF EXISTS encrypt_write_ds_5;
DROP DATABASE IF EXISTS encrypt_write_ds_6;
DROP DATABASE IF EXISTS encrypt_write_ds_7;
DROP DATABASE IF EXISTS encrypt_write_ds_8;
DROP DATABASE IF EXISTS encrypt_write_ds_9;
DROP DATABASE IF EXISTS encrypt_read_ds_0;
DROP DATABASE IF EXISTS encrypt_read_ds_1;
DROP DATABASE IF EXISTS encrypt_read_ds_2;
DROP DATABASE IF EXISTS encrypt_read_ds_3;
DROP DATABASE IF EXISTS encrypt_read_ds_4;
DROP DATABASE IF EXISTS encrypt_read_ds_5;
DROP DATABASE IF EXISTS encrypt_read_ds_6;
DROP DATABASE IF EXISTS encrypt_read_ds_7;
DROP DATABASE IF EXISTS encrypt_read_ds_8;
DROP DATABASE IF EXISTS encrypt_read_ds_9;

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

CREATE TABLE encrypt_write_ds_0.t_user_0 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE encrypt_write_ds_0.t_user_item_0 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE encrypt_write_ds_0.t_user_10 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE encrypt_write_ds_0.t_user_item_10 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE encrypt_write_ds_0.t_user_20 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE encrypt_write_ds_0.t_user_item_20 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE encrypt_write_ds_0.t_user_30 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE encrypt_write_ds_0.t_user_item_30 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE encrypt_write_ds_0.t_single_table (single_id INT NOT NULL, id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (single_id));
CREATE INDEX user_index_t_user_0 ON encrypt_write_ds_0.t_user_0 (user_id);
CREATE INDEX user_index_t_user_10 ON encrypt_write_ds_0.t_user_10 (user_id);
CREATE INDEX user_index_t_user_20 ON encrypt_write_ds_0.t_user_20 (user_id);
CREATE INDEX user_index_t_user_30 ON encrypt_write_ds_0.t_user_30 (user_id);

CREATE TABLE encrypt_write_ds_1.t_user_1 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE encrypt_write_ds_1.t_user_item_1 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE encrypt_write_ds_1.t_user_11 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE encrypt_write_ds_1.t_user_item_11 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE encrypt_write_ds_1.t_user_21 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE encrypt_write_ds_1.t_user_item_21 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE encrypt_write_ds_1.t_user_31 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE encrypt_write_ds_1.t_user_item_31 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE INDEX user_index_t_user_1 ON encrypt_write_ds_1.t_user_1 (user_id);
CREATE INDEX user_index_t_user_11 ON encrypt_write_ds_1.t_user_11 (user_id);
CREATE INDEX user_index_t_user_21 ON encrypt_write_ds_1.t_user_21 (user_id);
CREATE INDEX user_index_t_user_31 ON encrypt_write_ds_1.t_user_31 (user_id);

CREATE TABLE encrypt_write_ds_2.t_user_2 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE encrypt_write_ds_2.t_user_item_2 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE encrypt_write_ds_2.t_user_12 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE encrypt_write_ds_2.t_user_item_12 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE encrypt_write_ds_2.t_user_22 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE encrypt_write_ds_2.t_user_item_22 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE encrypt_write_ds_2.t_user_32 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE encrypt_write_ds_2.t_user_item_32 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE INDEX user_index_t_user_2 ON encrypt_write_ds_2.t_user_2 (user_id);
CREATE INDEX user_index_t_user_12 ON encrypt_write_ds_2.t_user_12 (user_id);
CREATE INDEX user_index_t_user_22 ON encrypt_write_ds_2.t_user_22 (user_id);
CREATE INDEX user_index_t_user_32 ON encrypt_write_ds_2.t_user_32 (user_id);

CREATE TABLE encrypt_write_ds_3.t_user_3 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE encrypt_write_ds_3.t_user_item_3 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE encrypt_write_ds_3.t_user_13 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE encrypt_write_ds_3.t_user_item_13 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE encrypt_write_ds_3.t_user_23 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE encrypt_write_ds_3.t_user_item_23 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE encrypt_write_ds_3.t_user_33 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE encrypt_write_ds_3.t_user_item_33 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE INDEX user_index_t_user_3 ON encrypt_write_ds_3.t_user_3 (user_id);
CREATE INDEX user_index_t_user_13 ON encrypt_write_ds_3.t_user_13 (user_id);
CREATE INDEX user_index_t_user_23 ON encrypt_write_ds_3.t_user_23 (user_id);
CREATE INDEX user_index_t_user_33 ON encrypt_write_ds_3.t_user_33 (user_id);

CREATE TABLE encrypt_write_ds_4.t_user_4 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE encrypt_write_ds_4.t_user_item_4 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE encrypt_write_ds_4.t_user_14 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE encrypt_write_ds_4.t_user_item_14 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE encrypt_write_ds_4.t_user_24 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE encrypt_write_ds_4.t_user_item_24 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE encrypt_write_ds_4.t_user_34 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE encrypt_write_ds_4.t_user_item_34 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE INDEX user_index_t_user_4 ON encrypt_write_ds_4.t_user_4 (user_id);
CREATE INDEX user_index_t_user_14 ON encrypt_write_ds_4.t_user_14 (user_id);
CREATE INDEX user_index_t_user_24 ON encrypt_write_ds_4.t_user_24 (user_id);
CREATE INDEX user_index_t_user_34 ON encrypt_write_ds_4.t_user_34 (user_id);

CREATE TABLE encrypt_write_ds_5.t_user_5 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE encrypt_write_ds_5.t_user_item_5 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE encrypt_write_ds_5.t_user_15 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE encrypt_write_ds_5.t_user_item_15 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE encrypt_write_ds_5.t_user_25 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE encrypt_write_ds_5.t_user_item_25 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE encrypt_write_ds_5.t_user_35 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE encrypt_write_ds_5.t_user_item_35 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE INDEX user_index_t_user_5 ON encrypt_write_ds_5.t_user_5 (user_id);
CREATE INDEX user_index_t_user_15 ON encrypt_write_ds_5.t_user_15 (user_id);
CREATE INDEX user_index_t_user_25 ON encrypt_write_ds_5.t_user_25 (user_id);
CREATE INDEX user_index_t_user_35 ON encrypt_write_ds_5.t_user_35 (user_id);

CREATE TABLE encrypt_write_ds_6.t_user_6 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE encrypt_write_ds_6.t_user_item_6 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE encrypt_write_ds_6.t_user_16 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE encrypt_write_ds_6.t_user_item_16 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE encrypt_write_ds_6.t_user_26 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE encrypt_write_ds_6.t_user_item_26 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE encrypt_write_ds_6.t_user_36 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE encrypt_write_ds_6.t_user_item_36 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE INDEX user_index_t_user_6 ON encrypt_write_ds_6.t_user_6 (user_id);
CREATE INDEX user_index_t_user_16 ON encrypt_write_ds_6.t_user_16 (user_id);
CREATE INDEX user_index_t_user_26 ON encrypt_write_ds_6.t_user_26 (user_id);
CREATE INDEX user_index_t_user_36 ON encrypt_write_ds_6.t_user_36 (user_id);

CREATE TABLE encrypt_write_ds_7.t_user_7 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE encrypt_write_ds_7.t_user_item_7 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE encrypt_write_ds_7.t_user_17 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE encrypt_write_ds_7.t_user_item_17 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE encrypt_write_ds_7.t_user_27 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE encrypt_write_ds_7.t_user_item_27 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE encrypt_write_ds_7.t_user_37 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE encrypt_write_ds_7.t_user_item_37 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE INDEX user_index_t_user_7 ON encrypt_write_ds_7.t_user_7 (user_id);
CREATE INDEX user_index_t_user_17 ON encrypt_write_ds_7.t_user_17 (user_id);
CREATE INDEX user_index_t_user_27 ON encrypt_write_ds_7.t_user_27 (user_id);
CREATE INDEX user_index_t_user_37 ON encrypt_write_ds_7.t_user_37 (user_id);

CREATE TABLE encrypt_write_ds_8.t_user_8 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE encrypt_write_ds_8.t_user_item_8 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE encrypt_write_ds_8.t_user_18 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE encrypt_write_ds_8.t_user_item_18 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE encrypt_write_ds_8.t_user_28 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE encrypt_write_ds_8.t_user_item_28 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE encrypt_write_ds_8.t_user_38 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE encrypt_write_ds_8.t_user_item_38 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE INDEX user_index_t_user_8 ON encrypt_write_ds_8.t_user_8 (user_id);
CREATE INDEX user_index_t_user_18 ON encrypt_write_ds_8.t_user_18 (user_id);
CREATE INDEX user_index_t_user_28 ON encrypt_write_ds_8.t_user_28 (user_id);
CREATE INDEX user_index_t_user_38 ON encrypt_write_ds_8.t_user_38 (user_id);

CREATE TABLE encrypt_write_ds_9.t_user_9 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE encrypt_write_ds_9.t_user_item_9 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE encrypt_write_ds_9.t_user_19 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE encrypt_write_ds_9.t_user_item_19 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE encrypt_write_ds_9.t_user_29 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE encrypt_write_ds_9.t_user_item_29 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE encrypt_write_ds_9.t_user_39 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE encrypt_write_ds_9.t_user_item_39 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE INDEX user_index_t_user_9 ON encrypt_write_ds_9.t_user_9 (user_id);
CREATE INDEX user_index_t_user_19 ON encrypt_write_ds_9.t_user_19 (user_id);
CREATE INDEX user_index_t_user_29 ON encrypt_write_ds_9.t_user_29 (user_id);
CREATE INDEX user_index_t_user_39 ON encrypt_write_ds_9.t_user_39 (user_id);

CREATE TABLE encrypt_read_ds_0.t_user_0 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE encrypt_read_ds_0.t_user_item_0 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE encrypt_read_ds_0.t_user_10 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE encrypt_read_ds_0.t_user_item_10 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE encrypt_read_ds_0.t_user_20 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE encrypt_read_ds_0.t_user_item_20 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE encrypt_read_ds_0.t_user_30 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE encrypt_read_ds_0.t_user_item_30 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE encrypt_read_ds_0.t_single_table (single_id INT NOT NULL, id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (single_id));
CREATE INDEX user_index_t_user_0 ON encrypt_read_ds_0.t_user_0 (user_id);
CREATE INDEX user_index_t_user_10 ON encrypt_read_ds_0.t_user_10 (user_id);
CREATE INDEX user_index_t_user_20 ON encrypt_read_ds_0.t_user_20 (user_id);
CREATE INDEX user_index_t_user_30 ON encrypt_read_ds_0.t_user_30 (user_id);

CREATE TABLE encrypt_read_ds_1.t_user_1 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE encrypt_read_ds_1.t_user_item_1 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE encrypt_read_ds_1.t_user_11 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE encrypt_read_ds_1.t_user_item_11 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE encrypt_read_ds_1.t_user_21 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE encrypt_read_ds_1.t_user_item_21 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE encrypt_read_ds_1.t_user_31 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE encrypt_read_ds_1.t_user_item_31 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE INDEX user_index_t_user_1 ON encrypt_read_ds_1.t_user_1 (user_id);
CREATE INDEX user_index_t_user_11 ON encrypt_read_ds_1.t_user_11 (user_id);
CREATE INDEX user_index_t_user_21 ON encrypt_read_ds_1.t_user_21 (user_id);
CREATE INDEX user_index_t_user_31 ON encrypt_read_ds_1.t_user_31 (user_id);

CREATE TABLE encrypt_read_ds_2.t_user_2 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE encrypt_read_ds_2.t_user_item_2 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE encrypt_read_ds_2.t_user_12 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE encrypt_read_ds_2.t_user_item_12 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE encrypt_read_ds_2.t_user_22 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE encrypt_read_ds_2.t_user_item_22 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE encrypt_read_ds_2.t_user_32 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE encrypt_read_ds_2.t_user_item_32 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE INDEX user_index_t_user_2 ON encrypt_read_ds_2.t_user_2 (user_id);
CREATE INDEX user_index_t_user_12 ON encrypt_read_ds_2.t_user_12 (user_id);
CREATE INDEX user_index_t_user_22 ON encrypt_read_ds_2.t_user_22 (user_id);
CREATE INDEX user_index_t_user_32 ON encrypt_read_ds_2.t_user_32 (user_id);

CREATE TABLE encrypt_read_ds_3.t_user_3 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE encrypt_read_ds_3.t_user_item_3 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE encrypt_read_ds_3.t_user_13 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE encrypt_read_ds_3.t_user_item_13 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE encrypt_read_ds_3.t_user_23 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE encrypt_read_ds_3.t_user_item_23 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE encrypt_read_ds_3.t_user_33 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE encrypt_read_ds_3.t_user_item_33 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE INDEX user_index_t_user_3 ON encrypt_read_ds_3.t_user_3 (user_id);
CREATE INDEX user_index_t_user_13 ON encrypt_read_ds_3.t_user_13 (user_id);
CREATE INDEX user_index_t_user_23 ON encrypt_read_ds_3.t_user_23 (user_id);
CREATE INDEX user_index_t_user_33 ON encrypt_read_ds_3.t_user_33 (user_id);

CREATE TABLE encrypt_read_ds_4.t_user_4 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE encrypt_read_ds_4.t_user_item_4 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE encrypt_read_ds_4.t_user_14 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE encrypt_read_ds_4.t_user_item_14 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE encrypt_read_ds_4.t_user_24 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE encrypt_read_ds_4.t_user_item_24 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE encrypt_read_ds_4.t_user_34 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE encrypt_read_ds_4.t_user_item_34 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE INDEX user_index_t_user_4 ON encrypt_read_ds_4.t_user_4 (user_id);
CREATE INDEX user_index_t_user_14 ON encrypt_read_ds_4.t_user_14 (user_id);
CREATE INDEX user_index_t_user_24 ON encrypt_read_ds_4.t_user_24 (user_id);
CREATE INDEX user_index_t_user_34 ON encrypt_read_ds_4.t_user_34 (user_id);

CREATE TABLE encrypt_read_ds_5.t_user_5 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE encrypt_read_ds_5.t_user_item_5 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE encrypt_read_ds_5.t_user_15 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE encrypt_read_ds_5.t_user_item_15 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE encrypt_read_ds_5.t_user_25 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE encrypt_read_ds_5.t_user_item_25 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE encrypt_read_ds_5.t_user_35 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE encrypt_read_ds_5.t_user_item_35 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE INDEX user_index_t_user_5 ON encrypt_read_ds_5.t_user_5 (user_id);
CREATE INDEX user_index_t_user_15 ON encrypt_read_ds_5.t_user_15 (user_id);
CREATE INDEX user_index_t_user_25 ON encrypt_read_ds_5.t_user_25 (user_id);
CREATE INDEX user_index_t_user_35 ON encrypt_read_ds_5.t_user_35 (user_id);

CREATE TABLE encrypt_read_ds_6.t_user_6 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE encrypt_read_ds_6.t_user_item_6 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE encrypt_read_ds_6.t_user_16 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE encrypt_read_ds_6.t_user_item_16 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE encrypt_read_ds_6.t_user_26 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE encrypt_read_ds_6.t_user_item_26 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE encrypt_read_ds_6.t_user_36 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE encrypt_read_ds_6.t_user_item_36 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE INDEX user_index_t_user_6 ON encrypt_read_ds_6.t_user_6 (user_id);
CREATE INDEX user_index_t_user_16 ON encrypt_read_ds_6.t_user_16 (user_id);
CREATE INDEX user_index_t_user_26 ON encrypt_read_ds_6.t_user_26 (user_id);
CREATE INDEX user_index_t_user_36 ON encrypt_read_ds_6.t_user_36 (user_id);

CREATE TABLE encrypt_read_ds_7.t_user_7 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE encrypt_read_ds_7.t_user_item_7 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE encrypt_read_ds_7.t_user_17 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE encrypt_read_ds_7.t_user_item_17 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE encrypt_read_ds_7.t_user_27 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE encrypt_read_ds_7.t_user_item_27 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE encrypt_read_ds_7.t_user_37 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE encrypt_read_ds_7.t_user_item_37 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE INDEX user_index_t_user_7 ON encrypt_read_ds_7.t_user_7 (user_id);
CREATE INDEX user_index_t_user_17 ON encrypt_read_ds_7.t_user_17 (user_id);
CREATE INDEX user_index_t_user_27 ON encrypt_read_ds_7.t_user_27 (user_id);
CREATE INDEX user_index_t_user_37 ON encrypt_read_ds_7.t_user_37 (user_id);

CREATE TABLE encrypt_read_ds_8.t_user_8 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE encrypt_read_ds_8.t_user_item_8 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE encrypt_read_ds_8.t_user_18 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE encrypt_read_ds_8.t_user_item_18 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE encrypt_read_ds_8.t_user_28 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE encrypt_read_ds_8.t_user_item_28 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE encrypt_read_ds_8.t_user_38 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE encrypt_read_ds_8.t_user_item_38 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE INDEX user_index_t_user_8 ON encrypt_read_ds_8.t_user_8 (user_id);
CREATE INDEX user_index_t_user_18 ON encrypt_read_ds_8.t_user_18 (user_id);
CREATE INDEX user_index_t_user_28 ON encrypt_read_ds_8.t_user_28 (user_id);
CREATE INDEX user_index_t_user_38 ON encrypt_read_ds_8.t_user_38 (user_id);

CREATE TABLE encrypt_read_ds_9.t_user_9 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE encrypt_read_ds_9.t_user_item_9 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE encrypt_read_ds_9.t_user_19 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE encrypt_read_ds_9.t_user_item_19 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE encrypt_read_ds_9.t_user_29 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE encrypt_read_ds_9.t_user_item_29 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE encrypt_read_ds_9.t_user_39 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_plain VARCHAR(45) NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE encrypt_read_ds_9.t_user_item_39 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE INDEX user_index_t_user_9 ON encrypt_read_ds_9.t_user_9 (user_id);
CREATE INDEX user_index_t_user_19 ON encrypt_read_ds_9.t_user_19 (user_id);
CREATE INDEX user_index_t_user_29 ON encrypt_read_ds_9.t_user_29 (user_id);
CREATE INDEX user_index_t_user_39 ON encrypt_read_ds_9.t_user_39 (user_id);
