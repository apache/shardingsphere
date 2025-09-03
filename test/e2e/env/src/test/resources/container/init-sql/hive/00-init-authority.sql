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

-- Create test user for Hive
-- Note: Hive doesn't support traditional user creation like RDBMS
-- This is mainly for documentation and consistency with other database init scripts
-- Hive authentication is typically handled at the Hadoop/HDFS level

-- Create default database if it doesn't exist
CREATE DATABASE IF NOT EXISTS encrypt;

-- Create test database for E2E testing
--CREATE DATABASE IF NOT EXISTS test_user;

-- Create databases for encrypt scenario
CREATE DATABASE IF NOT EXISTS expected_dataset;






