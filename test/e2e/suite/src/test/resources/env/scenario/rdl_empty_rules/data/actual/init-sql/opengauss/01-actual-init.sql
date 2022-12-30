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

CREATE DATABASE rdl_ds_0;
CREATE DATABASE rdl_ds_1;
CREATE DATABASE rdl_ds_2;

GRANT ALL PRIVILEGES ON DATABASE rdl_ds_0 TO test_user;
GRANT ALL PRIVILEGES ON DATABASE rdl_ds_1 TO test_user;
GRANT ALL PRIVILEGES ON DATABASE rdl_ds_2 TO test_user;

\c rdl_ds_0

DROP TABLE IF EXISTS t_user_0;
DROP TABLE IF EXISTS t_user_1;

CREATE TABLE t_user_0 (user_id INT NOT NULL, username VARCHAR(20) NOT NULL, phone VARCHAR(20) NULL, PRIMARY KEY (user_id));
CREATE TABLE t_user_1 (user_id INT NOT NULL, username VARCHAR(20) NOT NULL, phone VARCHAR(20) NULL, PRIMARY KEY (user_id));
