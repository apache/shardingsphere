/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

CREATE TABLE IF NOT EXISTS t_encrypt (id INT NOT NULL AUTO_INCREMENT, cipher_pwd VARCHAR(45) NULL, plain_pwd VARCHAR(45), PRIMARY KEY (id));
CREATE TABLE IF NOT EXISTS t_query_encrypt (id INT NOT NULL AUTO_INCREMENT, cipher_pwd VARCHAR(45) NULL, assist_pwd VARCHAR(45) NULL, PRIMARY KEY (id));
CREATE TABLE IF NOT EXISTS t_encrypt_contains_column (id INT NOT NULL AUTO_INCREMENT, cipher_pwd VARCHAR(45) NULL, plain_pwd VARCHAR(45), cipher_pwd2 VARCHAR(45) NULL, plain_pwd2 VARCHAR(45), PRIMARY KEY (id));
