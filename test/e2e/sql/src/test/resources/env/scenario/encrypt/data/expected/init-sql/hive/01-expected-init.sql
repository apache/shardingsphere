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

DROP DATABASE IF EXISTS expected_dataset CASCADE;
CREATE DATABASE IF NOT EXISTS expected_dataset;

CREATE TABLE expected_dataset.t_order (
  order_id INT,
  user_id INT,
  status STRING,
  merchant_id INT,
  remark STRING,
  creation_date DATE
);

CREATE TABLE expected_dataset.t_order_item (
  item_id INT,
  order_id INT,
  user_id INT,
  product_id INT,
  quantity INT,
  creation_date DATE
);

CREATE TABLE expected_dataset.t_user (
  user_id INT,
  user_name STRING,
  password STRING,
  email STRING,
  telephone STRING,
  creation_date DATE
);

CREATE TABLE expected_dataset.t_merchant (
  merchant_id INT,
  country_id SMALLINT,
  merchant_name STRING,
  business_code STRING,
  telephone STRING,
  creation_date DATE
);
