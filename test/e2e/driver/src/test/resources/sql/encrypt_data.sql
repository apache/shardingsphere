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

DELETE FROM t_encrypt;
DELETE FROM t_query_encrypt;
DELETE FROM t_encrypt_contains_column;

INSERT INTO t_encrypt VALUES(1, 'plainValue');
INSERT INTO t_encrypt VALUES(5, 'plainValue');
INSERT INTO t_query_encrypt VALUES(1, 'plainValue');
INSERT INTO t_query_encrypt VALUES(5, 'plainValue');
INSERT INTO t_encrypt_contains_column VALUES(1, 'plainValue', 'plainValue');
