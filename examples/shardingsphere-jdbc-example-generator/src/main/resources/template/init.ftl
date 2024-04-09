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

DROP DATABASE IF EXISTS demo_ds;
DROP DATABASE IF EXISTS demo_ds_0;
DROP DATABASE IF EXISTS demo_ds_1;

DROP DATABASE IF EXISTS demo_write_ds;
DROP DATABASE IF EXISTS demo_read_ds_0;
DROP DATABASE IF EXISTS demo_read_ds_1;

DROP DATABASE IF EXISTS demo_write_ds_0;
DROP DATABASE IF EXISTS demo_write_ds_0_read_0;
DROP DATABASE IF EXISTS demo_write_ds_0_read_1;
DROP DATABASE IF EXISTS demo_write_ds_1;
DROP DATABASE IF EXISTS demo_write_ds_1_read_0;
DROP DATABASE IF EXISTS demo_write_ds_1_read_1;

DROP DATABASE IF EXISTS shadow_demo_ds;
DROP DATABASE IF EXISTS shadow_demo_ds_0;
DROP DATABASE IF EXISTS shadow_demo_ds_1;

DROP DATABASE IF EXISTS demo_shadow_write_ds;
DROP DATABASE IF EXISTS demo_shadow_read_ds;
DROP DATABASE IF EXISTS demo_read_ds;

CREATE DATABASE demo_ds;
CREATE DATABASE demo_ds_0;
CREATE DATABASE demo_ds_1;

CREATE DATABASE demo_write_ds;
CREATE DATABASE demo_read_ds_0;
CREATE DATABASE demo_read_ds_1;

CREATE DATABASE demo_write_ds_0;
CREATE DATABASE demo_write_ds_0_read_0;
CREATE DATABASE demo_write_ds_0_read_1;
CREATE DATABASE demo_write_ds_1;
CREATE DATABASE demo_write_ds_1_read_0;
CREATE DATABASE demo_write_ds_1_read_1;

CREATE DATABASE shadow_demo_ds;
CREATE DATABASE shadow_demo_ds_0;
CREATE DATABASE shadow_demo_ds_1;

CREATE DATABASE demo_shadow_write_ds;
CREATE DATABASE demo_shadow_read_ds;
CREATE DATABASE demo_read_ds;


-- Execute SQL Script through logical-database connection , eg: sharding_db. Docker samples configuration in the 'conf/database-sharding.yaml' file
-- CREATE TABLE t_order ("order_id" serial4, "user_id" int4 NOT NULL, PRIMARY KEY ("order_id"));
-- CREATE TABLE t_order_item ("order_item_id" serial4, "order_id" int4 NOT NULL, "user_id" int4 NOT NULL, "status" varchar(50), PRIMARY KEY ("order_item_id"));
