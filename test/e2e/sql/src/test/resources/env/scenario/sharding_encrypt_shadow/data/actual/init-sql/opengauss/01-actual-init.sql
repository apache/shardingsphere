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

CREATE DATABASE db_0;
CREATE DATABASE db_1;
CREATE DATABASE db_2;
CREATE DATABASE db_3;
CREATE DATABASE db_4;
CREATE DATABASE db_5;
CREATE DATABASE db_6;
CREATE DATABASE db_7;
CREATE DATABASE db_8;
CREATE DATABASE db_9;
CREATE DATABASE shadow_db_0;
CREATE DATABASE shadow_db_1;
CREATE DATABASE shadow_db_2;
CREATE DATABASE shadow_db_3;
CREATE DATABASE shadow_db_4;
CREATE DATABASE shadow_db_5;
CREATE DATABASE shadow_db_6;
CREATE DATABASE shadow_db_7;
CREATE DATABASE shadow_db_8;
CREATE DATABASE shadow_db_9;

GRANT ALL PRIVILEGES ON DATABASE db_0 TO test_user;
GRANT ALL PRIVILEGES ON DATABASE db_1 TO test_user;
GRANT ALL PRIVILEGES ON DATABASE db_2 TO test_user;
GRANT ALL PRIVILEGES ON DATABASE db_3 TO test_user;
GRANT ALL PRIVILEGES ON DATABASE db_4 TO test_user;
GRANT ALL PRIVILEGES ON DATABASE db_5 TO test_user;
GRANT ALL PRIVILEGES ON DATABASE db_6 TO test_user;
GRANT ALL PRIVILEGES ON DATABASE db_7 TO test_user;
GRANT ALL PRIVILEGES ON DATABASE db_8 TO test_user;
GRANT ALL PRIVILEGES ON DATABASE db_9 TO test_user;
GRANT ALL PRIVILEGES ON DATABASE shadow_db_0 TO test_user;
GRANT ALL PRIVILEGES ON DATABASE shadow_db_1 TO test_user;
GRANT ALL PRIVILEGES ON DATABASE shadow_db_2 TO test_user;
GRANT ALL PRIVILEGES ON DATABASE shadow_db_3 TO test_user;
GRANT ALL PRIVILEGES ON DATABASE shadow_db_4 TO test_user;
GRANT ALL PRIVILEGES ON DATABASE shadow_db_5 TO test_user;
GRANT ALL PRIVILEGES ON DATABASE shadow_db_6 TO test_user;
GRANT ALL PRIVILEGES ON DATABASE shadow_db_7 TO test_user;
GRANT ALL PRIVILEGES ON DATABASE shadow_db_8 TO test_user;
GRANT ALL PRIVILEGES ON DATABASE shadow_db_9 TO test_user;

\c db_0

CREATE TYPE season AS ENUM ('spring', 'summer', 'autumn', 'winter');

DROP TABLE IF EXISTS t_shadow_0;

CREATE TABLE t_shadow_0 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_1;

CREATE TABLE t_shadow_1 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_2;

CREATE TABLE t_shadow_2 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_3;

CREATE TABLE t_shadow_3 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_4;

CREATE TABLE t_shadow_4 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_5;

CREATE TABLE t_shadow_5 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_6;

CREATE TABLE t_shadow_6 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_7;

CREATE TABLE t_shadow_7 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_8;

CREATE TABLE t_shadow_8 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_9;

CREATE TABLE t_shadow_9 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

\c shadow_db_0

CREATE TYPE season AS ENUM ('spring', 'summer', 'autumn', 'winter');

DROP TABLE IF EXISTS t_shadow_0;

CREATE TABLE t_shadow_0 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_1;

CREATE TABLE t_shadow_1 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_2;

CREATE TABLE t_shadow_2 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_3;

CREATE TABLE t_shadow_3 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_4;

CREATE TABLE t_shadow_4 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_5;

CREATE TABLE t_shadow_5 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_6;

CREATE TABLE t_shadow_6 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_7;

CREATE TABLE t_shadow_7 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_8;

CREATE TABLE t_shadow_8 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_9;

CREATE TABLE t_shadow_9 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

\c db_1

CREATE TYPE season AS ENUM ('spring', 'summer', 'autumn', 'winter');

DROP TABLE IF EXISTS t_shadow_0;

CREATE TABLE t_shadow_0 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_1;

CREATE TABLE t_shadow_1 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_2;

CREATE TABLE t_shadow_2 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_3;

CREATE TABLE t_shadow_3 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_4;

CREATE TABLE t_shadow_4 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_5;

CREATE TABLE t_shadow_5 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_6;

CREATE TABLE t_shadow_6 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_7;

CREATE TABLE t_shadow_7 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_8;

CREATE TABLE t_shadow_8 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_9;

CREATE TABLE t_shadow_9 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

\c shadow_db_1

CREATE TYPE season AS ENUM ('spring', 'summer', 'autumn', 'winter');

DROP TABLE IF EXISTS t_shadow_0;

CREATE TABLE t_shadow_0 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_1;

CREATE TABLE t_shadow_1 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_2;

CREATE TABLE t_shadow_2 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_3;

CREATE TABLE t_shadow_3 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_4;

CREATE TABLE t_shadow_4 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_5;

CREATE TABLE t_shadow_5 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_6;

CREATE TABLE t_shadow_6 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_7;

CREATE TABLE t_shadow_7 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_8;

CREATE TABLE t_shadow_8 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_9;

CREATE TABLE t_shadow_9 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

\c db_2

CREATE TYPE season AS ENUM ('spring', 'summer', 'autumn', 'winter');

DROP TABLE IF EXISTS t_shadow_0;

CREATE TABLE t_shadow_0 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_1;

CREATE TABLE t_shadow_1 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_2;

CREATE TABLE t_shadow_2 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_3;

CREATE TABLE t_shadow_3 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_4;

CREATE TABLE t_shadow_4 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_5;

CREATE TABLE t_shadow_5 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_6;

CREATE TABLE t_shadow_6 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_7;

CREATE TABLE t_shadow_7 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_8;

CREATE TABLE t_shadow_8 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_9;

CREATE TABLE t_shadow_9 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

\c shadow_db_2

CREATE TYPE season AS ENUM ('spring', 'summer', 'autumn', 'winter');

DROP TABLE IF EXISTS t_shadow_0;

CREATE TABLE t_shadow_0 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_1;

CREATE TABLE t_shadow_1 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_2;

CREATE TABLE t_shadow_2 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_3;

CREATE TABLE t_shadow_3 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_4;

CREATE TABLE t_shadow_4 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_5;

CREATE TABLE t_shadow_5 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_6;

CREATE TABLE t_shadow_6 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_7;

CREATE TABLE t_shadow_7 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_8;

CREATE TABLE t_shadow_8 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_9;

CREATE TABLE t_shadow_9 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

\c db_3

CREATE TYPE season AS ENUM ('spring', 'summer', 'autumn', 'winter');

DROP TABLE IF EXISTS t_shadow_0;

CREATE TABLE t_shadow_0 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_1;

CREATE TABLE t_shadow_1 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_2;

CREATE TABLE t_shadow_2 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_3;

CREATE TABLE t_shadow_3 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_4;

CREATE TABLE t_shadow_4 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_5;

CREATE TABLE t_shadow_5 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_6;

CREATE TABLE t_shadow_6 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_7;

CREATE TABLE t_shadow_7 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_8;

CREATE TABLE t_shadow_8 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_9;

CREATE TABLE t_shadow_9 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

\c shadow_db_3

CREATE TYPE season AS ENUM ('spring', 'summer', 'autumn', 'winter');

DROP TABLE IF EXISTS t_shadow_0;

CREATE TABLE t_shadow_0 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_1;

CREATE TABLE t_shadow_1 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_2;

CREATE TABLE t_shadow_2 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_3;

CREATE TABLE t_shadow_3 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_4;

CREATE TABLE t_shadow_4 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_5;

CREATE TABLE t_shadow_5 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_6;

CREATE TABLE t_shadow_6 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_7;

CREATE TABLE t_shadow_7 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_8;

CREATE TABLE t_shadow_8 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_9;

CREATE TABLE t_shadow_9 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

\c db_4

CREATE TYPE season AS ENUM ('spring', 'summer', 'autumn', 'winter');

DROP TABLE IF EXISTS t_shadow_0;

CREATE TABLE t_shadow_0 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_1;

CREATE TABLE t_shadow_1 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_2;

CREATE TABLE t_shadow_2 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_3;

CREATE TABLE t_shadow_3 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_4;

CREATE TABLE t_shadow_4 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_5;

CREATE TABLE t_shadow_5 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_6;

CREATE TABLE t_shadow_6 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_7;

CREATE TABLE t_shadow_7 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_8;

CREATE TABLE t_shadow_8 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_9;

CREATE TABLE t_shadow_9 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

\c shadow_db_4

CREATE TYPE season AS ENUM ('spring', 'summer', 'autumn', 'winter');

DROP TABLE IF EXISTS t_shadow_0;

CREATE TABLE t_shadow_0 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_1;

CREATE TABLE t_shadow_1 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_2;

CREATE TABLE t_shadow_2 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_3;

CREATE TABLE t_shadow_3 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_4;

CREATE TABLE t_shadow_4 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_5;

CREATE TABLE t_shadow_5 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_6;

CREATE TABLE t_shadow_6 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_7;

CREATE TABLE t_shadow_7 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_8;

CREATE TABLE t_shadow_8 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_9;

CREATE TABLE t_shadow_9 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

\c db_5

CREATE TYPE season AS ENUM ('spring', 'summer', 'autumn', 'winter');

DROP TABLE IF EXISTS t_shadow_0;

CREATE TABLE t_shadow_0 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_1;

CREATE TABLE t_shadow_1 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_2;

CREATE TABLE t_shadow_2 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_3;

CREATE TABLE t_shadow_3 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_4;

CREATE TABLE t_shadow_4 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_5;

CREATE TABLE t_shadow_5 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_6;

CREATE TABLE t_shadow_6 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_7;

CREATE TABLE t_shadow_7 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_8;

CREATE TABLE t_shadow_8 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_9;

CREATE TABLE t_shadow_9 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

\c shadow_db_5

CREATE TYPE season AS ENUM ('spring', 'summer', 'autumn', 'winter');

DROP TABLE IF EXISTS t_shadow_0;

CREATE TABLE t_shadow_0 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_1;

CREATE TABLE t_shadow_1 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_2;

CREATE TABLE t_shadow_2 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_3;

CREATE TABLE t_shadow_3 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_4;

CREATE TABLE t_shadow_4 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_5;

CREATE TABLE t_shadow_5 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_6;

CREATE TABLE t_shadow_6 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_7;

CREATE TABLE t_shadow_7 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_8;

CREATE TABLE t_shadow_8 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_9;

CREATE TABLE t_shadow_9 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

\c db_6

CREATE TYPE season AS ENUM ('spring', 'summer', 'autumn', 'winter');

DROP TABLE IF EXISTS t_shadow_0;

CREATE TABLE t_shadow_0 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_1;

CREATE TABLE t_shadow_1 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_2;

CREATE TABLE t_shadow_2 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_3;

CREATE TABLE t_shadow_3 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_4;

CREATE TABLE t_shadow_4 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_5;

CREATE TABLE t_shadow_5 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_6;

CREATE TABLE t_shadow_6 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_7;

CREATE TABLE t_shadow_7 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_8;

CREATE TABLE t_shadow_8 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_9;

CREATE TABLE t_shadow_9 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

\c shadow_db_6

CREATE TYPE season AS ENUM ('spring', 'summer', 'autumn', 'winter');

DROP TABLE IF EXISTS t_shadow_0;

CREATE TABLE t_shadow_0 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_1;

CREATE TABLE t_shadow_1 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_2;

CREATE TABLE t_shadow_2 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_3;

CREATE TABLE t_shadow_3 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_4;

CREATE TABLE t_shadow_4 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_5;

CREATE TABLE t_shadow_5 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_6;

CREATE TABLE t_shadow_6 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_7;

CREATE TABLE t_shadow_7 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_8;

CREATE TABLE t_shadow_8 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_9;

CREATE TABLE t_shadow_9 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

\c db_7

CREATE TYPE season AS ENUM ('spring', 'summer', 'autumn', 'winter');

DROP TABLE IF EXISTS t_shadow_0;

CREATE TABLE t_shadow_0 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_1;

CREATE TABLE t_shadow_1 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_2;

CREATE TABLE t_shadow_2 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_3;

CREATE TABLE t_shadow_3 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_4;

CREATE TABLE t_shadow_4 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_5;

CREATE TABLE t_shadow_5 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_6;

CREATE TABLE t_shadow_6 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_7;

CREATE TABLE t_shadow_7 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_8;

CREATE TABLE t_shadow_8 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_9;

CREATE TABLE t_shadow_9 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

\c shadow_db_7

CREATE TYPE season AS ENUM ('spring', 'summer', 'autumn', 'winter');

DROP TABLE IF EXISTS t_shadow_0;

CREATE TABLE t_shadow_0 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_1;

CREATE TABLE t_shadow_1 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_2;

CREATE TABLE t_shadow_2 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_3;

CREATE TABLE t_shadow_3 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_4;

CREATE TABLE t_shadow_4 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_5;

CREATE TABLE t_shadow_5 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_6;

CREATE TABLE t_shadow_6 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_7;

CREATE TABLE t_shadow_7 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_8;

CREATE TABLE t_shadow_8 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_9;

CREATE TABLE t_shadow_9 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

\c db_8

CREATE TYPE season AS ENUM ('spring', 'summer', 'autumn', 'winter');

DROP TABLE IF EXISTS t_shadow_0;

CREATE TABLE t_shadow_0 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_1;

CREATE TABLE t_shadow_1 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_2;

CREATE TABLE t_shadow_2 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_3;

CREATE TABLE t_shadow_3 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_4;

CREATE TABLE t_shadow_4 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_5;

CREATE TABLE t_shadow_5 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_6;

CREATE TABLE t_shadow_6 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_7;

CREATE TABLE t_shadow_7 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_8;

CREATE TABLE t_shadow_8 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_9;

CREATE TABLE t_shadow_9 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

\c shadow_db_8

CREATE TYPE season AS ENUM ('spring', 'summer', 'autumn', 'winter');

DROP TABLE IF EXISTS t_shadow_0;

CREATE TABLE t_shadow_0 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_1;

CREATE TABLE t_shadow_1 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_2;

CREATE TABLE t_shadow_2 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_3;

CREATE TABLE t_shadow_3 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_4;

CREATE TABLE t_shadow_4 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_5;

CREATE TABLE t_shadow_5 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_6;

CREATE TABLE t_shadow_6 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_7;

CREATE TABLE t_shadow_7 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_8;

CREATE TABLE t_shadow_8 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_9;

CREATE TABLE t_shadow_9 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

\c db_9

CREATE TYPE season AS ENUM ('spring', 'summer', 'autumn', 'winter');

DROP TABLE IF EXISTS t_shadow_0;

CREATE TABLE t_shadow_0 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_1;

CREATE TABLE t_shadow_1 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_2;

CREATE TABLE t_shadow_2 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_3;

CREATE TABLE t_shadow_3 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_4;

CREATE TABLE t_shadow_4 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_5;

CREATE TABLE t_shadow_5 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_6;

CREATE TABLE t_shadow_6 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_7;

CREATE TABLE t_shadow_7 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_8;

CREATE TABLE t_shadow_8 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_9;

CREATE TABLE t_shadow_9 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

\c shadow_db_9

CREATE TYPE season AS ENUM ('spring', 'summer', 'autumn', 'winter');

DROP TABLE IF EXISTS t_shadow_0;

CREATE TABLE t_shadow_0 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_1;

CREATE TABLE t_shadow_1 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_2;

CREATE TABLE t_shadow_2 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_3;

CREATE TABLE t_shadow_3 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_4;

CREATE TABLE t_shadow_4 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_5;

CREATE TABLE t_shadow_5 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_6;

CREATE TABLE t_shadow_6 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_7;

CREATE TABLE t_shadow_7 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_8;

CREATE TABLE t_shadow_8 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));

DROP TABLE IF EXISTS t_shadow_9;

CREATE TABLE t_shadow_9 (order_id BIGINT NOT NULL, user_id INT NOT NULL, order_name_cipher VARCHAR(200) NOT NULL, type_char CHAR(1) NOT NULL, type_boolean BOOLEAN NOT NULL, type_smallint SMALLINT NOT NULL, type_enum season DEFAULT 'summer', type_decimal NUMERIC(18,2) DEFAULT NULL, type_date DATE DEFAULT NULL, type_time TIME DEFAULT NULL, type_timestamp TIMESTAMP DEFAULT NULL, PRIMARY KEY (order_id));
