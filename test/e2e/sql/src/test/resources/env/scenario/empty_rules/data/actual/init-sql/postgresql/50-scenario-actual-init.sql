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

CREATE DATABASE write_ds_0;
CREATE DATABASE write_ds_1;
CREATE DATABASE write_ds_2;
CREATE DATABASE write_ds_3;
CREATE DATABASE write_ds_4;
CREATE DATABASE write_ds_5;
CREATE DATABASE write_ds_6;
CREATE DATABASE write_ds_7;
CREATE DATABASE write_ds_8;
CREATE DATABASE write_ds_9;
CREATE DATABASE read_ds_0;
CREATE DATABASE read_ds_1;
CREATE DATABASE read_ds_2;
CREATE DATABASE read_ds_3;
CREATE DATABASE read_ds_4;
CREATE DATABASE read_ds_5;
CREATE DATABASE read_ds_6;
CREATE DATABASE read_ds_7;
CREATE DATABASE read_ds_8;
CREATE DATABASE read_ds_9;

CREATE DATABASE rdl_test_0;
CREATE DATABASE rdl_test_1;
CREATE DATABASE rdl_test_2;

GRANT ALL PRIVILEGES ON DATABASE write_ds_0 TO test_user;
GRANT ALL PRIVILEGES ON DATABASE write_ds_1 TO test_user;
GRANT ALL PRIVILEGES ON DATABASE write_ds_2 TO test_user;
GRANT ALL PRIVILEGES ON DATABASE write_ds_3 TO test_user;
GRANT ALL PRIVILEGES ON DATABASE write_ds_4 TO test_user;
GRANT ALL PRIVILEGES ON DATABASE write_ds_5 TO test_user;
GRANT ALL PRIVILEGES ON DATABASE write_ds_6 TO test_user;
GRANT ALL PRIVILEGES ON DATABASE write_ds_7 TO test_user;
GRANT ALL PRIVILEGES ON DATABASE write_ds_8 TO test_user;
GRANT ALL PRIVILEGES ON DATABASE write_ds_9 TO test_user;
GRANT ALL PRIVILEGES ON DATABASE read_ds_0 TO test_user;
GRANT ALL PRIVILEGES ON DATABASE read_ds_1 TO test_user;
GRANT ALL PRIVILEGES ON DATABASE read_ds_2 TO test_user;
GRANT ALL PRIVILEGES ON DATABASE read_ds_3 TO test_user;
GRANT ALL PRIVILEGES ON DATABASE read_ds_4 TO test_user;
GRANT ALL PRIVILEGES ON DATABASE read_ds_5 TO test_user;
GRANT ALL PRIVILEGES ON DATABASE read_ds_6 TO test_user;
GRANT ALL PRIVILEGES ON DATABASE read_ds_7 TO test_user;
GRANT ALL PRIVILEGES ON DATABASE read_ds_8 TO test_user;
GRANT ALL PRIVILEGES ON DATABASE read_ds_9 TO test_user;
GRANT ALL PRIVILEGES ON DATABASE rdl_test_0 TO test_user;
GRANT ALL PRIVILEGES ON DATABASE rdl_test_1 TO test_user;
GRANT ALL PRIVILEGES ON DATABASE rdl_test_2 TO test_user;

\c write_ds_0

DROP TABLE IF EXISTS t_user_0;
DROP TABLE IF EXISTS t_user_item_0;
DROP TABLE IF EXISTS t_user_10;
DROP TABLE IF EXISTS t_user_item_10;
DROP TABLE IF EXISTS t_user_20;
DROP TABLE IF EXISTS t_user_item_20;
DROP TABLE IF EXISTS t_user_30;
DROP TABLE IF EXISTS t_user_item_30;
DROP TABLE IF EXISTS t_single_table;

CREATE TABLE t_user_0 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_0 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_10 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_10 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_20 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_20 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_30 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_30 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_single_table (single_id INT NOT NULL, id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (single_id));
CREATE INDEX user_index_t_user_0 ON t_user_0 (user_id);
CREATE INDEX user_index_t_user_10 ON t_user_10 (user_id);
CREATE INDEX user_index_t_user_20 ON t_user_20 (user_id);
CREATE INDEX user_index_t_user_30 ON t_user_30 (user_id);

\c write_ds_1

DROP TABLE IF EXISTS t_user_1;
DROP TABLE IF EXISTS t_user_item_1;
DROP TABLE IF EXISTS t_user_11;
DROP TABLE IF EXISTS t_user_item_11;
DROP TABLE IF EXISTS t_user_21;
DROP TABLE IF EXISTS t_user_item_21;
DROP TABLE IF EXISTS t_user_31;
DROP TABLE IF EXISTS t_user_item_31;

CREATE TABLE t_user_1 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_1 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_11 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_11 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_21 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_21 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_31 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_31 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE INDEX user_index_t_user_1 ON t_user_1 (user_id);
CREATE INDEX user_index_t_user_11 ON t_user_11 (user_id);
CREATE INDEX user_index_t_user_21 ON t_user_21 (user_id);
CREATE INDEX user_index_t_user_31 ON t_user_31 (user_id);

\c write_ds_2

DROP TABLE IF EXISTS t_user_2;
DROP TABLE IF EXISTS t_user_item_2;
DROP TABLE IF EXISTS t_user_12;
DROP TABLE IF EXISTS t_user_item_12;
DROP TABLE IF EXISTS t_user_22;
DROP TABLE IF EXISTS t_user_item_22;
DROP TABLE IF EXISTS t_user_32;
DROP TABLE IF EXISTS t_user_item_32;

CREATE TABLE t_user_2 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_2 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_12 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_12 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_22 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_22 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_32 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_32 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE INDEX user_index_t_user_2 ON t_user_2 (user_id);
CREATE INDEX user_index_t_user_12 ON t_user_12 (user_id);
CREATE INDEX user_index_t_user_22 ON t_user_22 (user_id);
CREATE INDEX user_index_t_user_32 ON t_user_32 (user_id);

\c write_ds_3

DROP TABLE IF EXISTS t_user_3;
DROP TABLE IF EXISTS t_user_item_3;
DROP TABLE IF EXISTS t_user_13;
DROP TABLE IF EXISTS t_user_item_13;
DROP TABLE IF EXISTS t_user_23;
DROP TABLE IF EXISTS t_user_item_23;
DROP TABLE IF EXISTS t_user_33;
DROP TABLE IF EXISTS t_user_item_33;

CREATE TABLE t_user_3 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_3 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_13 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_13 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_23 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_23 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_33 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_33 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE INDEX user_index_t_user_3 ON t_user_3 (user_id);
CREATE INDEX user_index_t_user_13 ON t_user_13 (user_id);
CREATE INDEX user_index_t_user_23 ON t_user_23 (user_id);
CREATE INDEX user_index_t_user_33 ON t_user_33 (user_id);

\c write_ds_4

DROP TABLE IF EXISTS t_user_4;
DROP TABLE IF EXISTS t_user_item_4;
DROP TABLE IF EXISTS t_user_14;
DROP TABLE IF EXISTS t_user_item_14;
DROP TABLE IF EXISTS t_user_24;
DROP TABLE IF EXISTS t_user_item_24;
DROP TABLE IF EXISTS t_user_34;
DROP TABLE IF EXISTS t_user_item_34;

CREATE TABLE t_user_4 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_4 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_14 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_14 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_24 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_24 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_34 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_34 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE INDEX user_index_t_user_4 ON t_user_4 (user_id);
CREATE INDEX user_index_t_user_14 ON t_user_14 (user_id);
CREATE INDEX user_index_t_user_24 ON t_user_24 (user_id);
CREATE INDEX user_index_t_user_34 ON t_user_34 (user_id);

\c write_ds_5

DROP TABLE IF EXISTS t_user_5;
DROP TABLE IF EXISTS t_user_item_5;
DROP TABLE IF EXISTS t_user_15;
DROP TABLE IF EXISTS t_user_item_15;
DROP TABLE IF EXISTS t_user_25;
DROP TABLE IF EXISTS t_user_item_25;
DROP TABLE IF EXISTS t_user_35;
DROP TABLE IF EXISTS t_user_item_35;

CREATE TABLE t_user_5 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_5 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_15 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_15 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_25 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_25 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_35 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_35 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE INDEX user_index_t_user_5 ON t_user_5 (user_id);
CREATE INDEX user_index_t_user_15 ON t_user_15 (user_id);
CREATE INDEX user_index_t_user_25 ON t_user_25 (user_id);
CREATE INDEX user_index_t_user_35 ON t_user_35 (user_id);

\c write_ds_6

DROP TABLE IF EXISTS t_user_6;
DROP TABLE IF EXISTS t_user_item_6;
DROP TABLE IF EXISTS t_user_16;
DROP TABLE IF EXISTS t_user_item_16;
DROP TABLE IF EXISTS t_user_26;
DROP TABLE IF EXISTS t_user_item_26;
DROP TABLE IF EXISTS t_user_36;
DROP TABLE IF EXISTS t_user_item_36;

CREATE TABLE t_user_6 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_6 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_16 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_16 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_26 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_26 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_36 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_36 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE INDEX user_index_t_user_6 ON t_user_6 (user_id);
CREATE INDEX user_index_t_user_16 ON t_user_16 (user_id);
CREATE INDEX user_index_t_user_26 ON t_user_26 (user_id);
CREATE INDEX user_index_t_user_36 ON t_user_36 (user_id);

\c write_ds_7

DROP TABLE IF EXISTS t_user_7;
DROP TABLE IF EXISTS t_user_item_7;
DROP TABLE IF EXISTS t_user_17;
DROP TABLE IF EXISTS t_user_item_17;
DROP TABLE IF EXISTS t_user_27;
DROP TABLE IF EXISTS t_user_item_27;
DROP TABLE IF EXISTS t_user_37;
DROP TABLE IF EXISTS t_user_item_37;

CREATE TABLE t_user_7 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_7 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_17 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_17 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_27 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_27 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_37 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_37 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE INDEX user_index_t_user_7 ON t_user_7 (user_id);
CREATE INDEX user_index_t_user_17 ON t_user_17 (user_id);
CREATE INDEX user_index_t_user_27 ON t_user_27 (user_id);
CREATE INDEX user_index_t_user_37 ON t_user_37 (user_id);

\c write_ds_8

DROP TABLE IF EXISTS t_user_8;
DROP TABLE IF EXISTS t_user_item_8;
DROP TABLE IF EXISTS t_user_18;
DROP TABLE IF EXISTS t_user_item_18;
DROP TABLE IF EXISTS t_user_28;
DROP TABLE IF EXISTS t_user_item_28;
DROP TABLE IF EXISTS t_user_38;
DROP TABLE IF EXISTS t_user_item_38;

CREATE TABLE t_user_8 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_8 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_18 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_18 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_28 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_28 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_38 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_38 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE INDEX user_index_t_user_8 ON t_user_8 (user_id);
CREATE INDEX user_index_t_user_18 ON t_user_18 (user_id);
CREATE INDEX user_index_t_user_28 ON t_user_28 (user_id);
CREATE INDEX user_index_t_user_38 ON t_user_38 (user_id);

\c write_ds_9

DROP TABLE IF EXISTS t_user_9;
DROP TABLE IF EXISTS t_user_item_9;
DROP TABLE IF EXISTS t_user_19;
DROP TABLE IF EXISTS t_user_item_19;
DROP TABLE IF EXISTS t_user_29;
DROP TABLE IF EXISTS t_user_item_29;
DROP TABLE IF EXISTS t_user_39;
DROP TABLE IF EXISTS t_user_item_39;

CREATE TABLE t_user_9 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_9 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_19 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_19 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_29 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_29 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_39 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_39 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE INDEX user_index_t_user_9 ON t_user_9 (user_id);
CREATE INDEX user_index_t_user_19 ON t_user_19 (user_id);
CREATE INDEX user_index_t_user_29 ON t_user_29 (user_id);
CREATE INDEX user_index_t_user_39 ON t_user_39 (user_id);

\c read_ds_0

DROP TABLE IF EXISTS t_user_0;
DROP TABLE IF EXISTS t_user_item_0;
DROP TABLE IF EXISTS t_user_10;
DROP TABLE IF EXISTS t_user_item_10;
DROP TABLE IF EXISTS t_user_20;
DROP TABLE IF EXISTS t_user_item_20;
DROP TABLE IF EXISTS t_user_30;
DROP TABLE IF EXISTS t_user_item_30;
DROP TABLE IF EXISTS t_single_table;

CREATE TABLE t_user_0 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_0 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_10 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_10 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_20 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_20 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_30 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_30 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_single_table (single_id INT NOT NULL, id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (single_id));
CREATE INDEX user_index_t_user_0 ON t_user_0 (user_id);
CREATE INDEX user_index_t_user_10 ON t_user_10 (user_id);
CREATE INDEX user_index_t_user_20 ON t_user_20 (user_id);
CREATE INDEX user_index_t_user_30 ON t_user_30 (user_id);

\c read_ds_1

DROP TABLE IF EXISTS t_user_1;
DROP TABLE IF EXISTS t_user_item_1;
DROP TABLE IF EXISTS t_user_11;
DROP TABLE IF EXISTS t_user_item_11;
DROP TABLE IF EXISTS t_user_21;
DROP TABLE IF EXISTS t_user_item_21;
DROP TABLE IF EXISTS t_user_31;
DROP TABLE IF EXISTS t_user_item_31;

CREATE TABLE t_user_1 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_1 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_11 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_11 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_21 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_21 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_31 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_31 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE INDEX user_index_t_user_1 ON t_user_1 (user_id);
CREATE INDEX user_index_t_user_11 ON t_user_11 (user_id);
CREATE INDEX user_index_t_user_21 ON t_user_21 (user_id);
CREATE INDEX user_index_t_user_31 ON t_user_31 (user_id);

\c read_ds_2

DROP TABLE IF EXISTS t_user_2;
DROP TABLE IF EXISTS t_user_item_2;
DROP TABLE IF EXISTS t_user_12;
DROP TABLE IF EXISTS t_user_item_12;
DROP TABLE IF EXISTS t_user_22;
DROP TABLE IF EXISTS t_user_item_22;
DROP TABLE IF EXISTS t_user_32;
DROP TABLE IF EXISTS t_user_item_32;

CREATE TABLE t_user_2 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_2 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_12 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_12 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_22 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_22 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_32 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_32 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE INDEX user_index_t_user_2 ON t_user_2 (user_id);
CREATE INDEX user_index_t_user_12 ON t_user_12 (user_id);
CREATE INDEX user_index_t_user_22 ON t_user_22 (user_id);
CREATE INDEX user_index_t_user_32 ON t_user_32 (user_id);

\c read_ds_3

DROP TABLE IF EXISTS t_user_3;
DROP TABLE IF EXISTS t_user_item_3;
DROP TABLE IF EXISTS t_user_13;
DROP TABLE IF EXISTS t_user_item_13;
DROP TABLE IF EXISTS t_user_23;
DROP TABLE IF EXISTS t_user_item_23;
DROP TABLE IF EXISTS t_user_33;
DROP TABLE IF EXISTS t_user_item_33;

CREATE TABLE t_user_3 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_3 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_13 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_13 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_23 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_23 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_33 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_33 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE INDEX user_index_t_user_3 ON t_user_3 (user_id);
CREATE INDEX user_index_t_user_13 ON t_user_13 (user_id);
CREATE INDEX user_index_t_user_23 ON t_user_23 (user_id);
CREATE INDEX user_index_t_user_33 ON t_user_33 (user_id);

\c read_ds_4

DROP TABLE IF EXISTS t_user_4;
DROP TABLE IF EXISTS t_user_item_4;
DROP TABLE IF EXISTS t_user_14;
DROP TABLE IF EXISTS t_user_item_14;
DROP TABLE IF EXISTS t_user_24;
DROP TABLE IF EXISTS t_user_item_24;
DROP TABLE IF EXISTS t_user_34;
DROP TABLE IF EXISTS t_user_item_34;

CREATE TABLE t_user_4 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_4 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_14 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_14 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_24 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_24 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_34 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_34 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE INDEX user_index_t_user_4 ON t_user_4 (user_id);
CREATE INDEX user_index_t_user_14 ON t_user_14 (user_id);
CREATE INDEX user_index_t_user_24 ON t_user_24 (user_id);
CREATE INDEX user_index_t_user_34 ON t_user_34 (user_id);

\c read_ds_5

DROP TABLE IF EXISTS t_user_5;
DROP TABLE IF EXISTS t_user_item_5;
DROP TABLE IF EXISTS t_user_15;
DROP TABLE IF EXISTS t_user_item_15;
DROP TABLE IF EXISTS t_user_25;
DROP TABLE IF EXISTS t_user_item_25;
DROP TABLE IF EXISTS t_user_35;
DROP TABLE IF EXISTS t_user_item_35;

CREATE TABLE t_user_5 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_5 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_15 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_15 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_25 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_25 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_35 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_35 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE INDEX user_index_t_user_5 ON t_user_5 (user_id);
CREATE INDEX user_index_t_user_15 ON t_user_15 (user_id);
CREATE INDEX user_index_t_user_25 ON t_user_25 (user_id);
CREATE INDEX user_index_t_user_35 ON t_user_35 (user_id);

\c read_ds_6

DROP TABLE IF EXISTS t_user_6;
DROP TABLE IF EXISTS t_user_item_6;
DROP TABLE IF EXISTS t_user_16;
DROP TABLE IF EXISTS t_user_item_16;
DROP TABLE IF EXISTS t_user_26;
DROP TABLE IF EXISTS t_user_item_26;
DROP TABLE IF EXISTS t_user_36;
DROP TABLE IF EXISTS t_user_item_36;

CREATE TABLE t_user_6 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_6 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_16 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_16 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_26 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_26 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_36 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_36 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE INDEX user_index_t_user_6 ON t_user_6 (user_id);
CREATE INDEX user_index_t_user_16 ON t_user_16 (user_id);
CREATE INDEX user_index_t_user_26 ON t_user_26 (user_id);
CREATE INDEX user_index_t_user_36 ON t_user_36 (user_id);

\c read_ds_7

DROP TABLE IF EXISTS t_user_7;
DROP TABLE IF EXISTS t_user_item_7;
DROP TABLE IF EXISTS t_user_17;
DROP TABLE IF EXISTS t_user_item_17;
DROP TABLE IF EXISTS t_user_27;
DROP TABLE IF EXISTS t_user_item_27;
DROP TABLE IF EXISTS t_user_37;
DROP TABLE IF EXISTS t_user_item_37;

CREATE TABLE t_user_7 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_7 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_17 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_17 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_27 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_27 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_37 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_37 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE INDEX user_index_t_user_7 ON t_user_7 (user_id);
CREATE INDEX user_index_t_user_17 ON t_user_17 (user_id);
CREATE INDEX user_index_t_user_27 ON t_user_27 (user_id);
CREATE INDEX user_index_t_user_37 ON t_user_37 (user_id);

\c read_ds_8

DROP TABLE IF EXISTS t_user_8;
DROP TABLE IF EXISTS t_user_item_8;
DROP TABLE IF EXISTS t_user_18;
DROP TABLE IF EXISTS t_user_item_18;
DROP TABLE IF EXISTS t_user_28;
DROP TABLE IF EXISTS t_user_item_28;
DROP TABLE IF EXISTS t_user_38;
DROP TABLE IF EXISTS t_user_item_38;

CREATE TABLE t_user_8 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_8 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_18 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_18 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_28 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_28 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_38 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_38 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE INDEX user_index_t_user_8 ON t_user_8 (user_id);
CREATE INDEX user_index_t_user_18 ON t_user_18 (user_id);
CREATE INDEX user_index_t_user_28 ON t_user_28 (user_id);
CREATE INDEX user_index_t_user_38 ON t_user_38 (user_id);

\c read_ds_9

DROP TABLE IF EXISTS t_user_9;
DROP TABLE IF EXISTS t_user_item_9;
DROP TABLE IF EXISTS t_user_19;
DROP TABLE IF EXISTS t_user_item_19;
DROP TABLE IF EXISTS t_user_29;
DROP TABLE IF EXISTS t_user_item_29;
DROP TABLE IF EXISTS t_user_39;
DROP TABLE IF EXISTS t_user_item_39;

CREATE TABLE t_user_9 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_9 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_19 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_19 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_29 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_29 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE TABLE t_user_39 (user_id INT NOT NULL, address_id INT NOT NULL, pwd_cipher VARCHAR(45) NULL, status VARCHAR(45) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_item_39 (item_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, creation_date DATE, PRIMARY KEY (item_id));
CREATE INDEX user_index_t_user_9 ON t_user_9 (user_id);
CREATE INDEX user_index_t_user_19 ON t_user_19 (user_id);
CREATE INDEX user_index_t_user_29 ON t_user_29 (user_id);
CREATE INDEX user_index_t_user_39 ON t_user_39 (user_id);
