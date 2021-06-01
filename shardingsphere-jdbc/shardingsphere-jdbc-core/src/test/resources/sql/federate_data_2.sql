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

DELETE FROM t_order_item_federate;
DELETE FROM t_order_item_federate_sharding_0;
DELETE FROM t_order_item_federate_sharding_1;
DELETE FROM t_user_encrypt_federate;
DELETE FROM t_user_encrypt_federate_sharding_0;
DELETE FROM t_user_encrypt_federate_sharding_1;

INSERT INTO t_order_item_federate VALUES(100000, 1000, 10, 'init');
INSERT INTO t_order_item_federate VALUES(100001, 1000, 10, 'init');
INSERT INTO t_order_item_federate VALUES(100100, 1001, 10, 'init');
INSERT INTO t_order_item_federate VALUES(100101, 1001, 10, 'init');

INSERT INTO t_order_item_federate_sharding_0 VALUES(1000, 10000, 10, 'init', 't_order_item_federate_sharding');
INSERT INTO t_order_item_federate_sharding_1 VALUES(1001, 10001, 11, 'init', 't_order_item_federate_sharding');
INSERT INTO t_order_item_federate_sharding_0 VALUES(1010, 10001, 10, 'init', 't_order_item_federate_sharding');
INSERT INTO t_order_item_federate_sharding_1 VALUES(1011, 10001, 10, 'init', 't_order_item_federate_sharding');

INSERT INTO t_user_encrypt_federate VALUES(0, 'plain password1', 'U2FsdGVkX19XstrYErommVKzbiaDrBzs5hkJnfw2iqY=', 'Rachel');
INSERT INTO t_user_encrypt_federate VALUES(1, 'plain password2', 'U2FsdGVkX1+qxFaxftJmfwfUT8Q8e8QnNesNZjP9jLo=', 'Monica');
INSERT INTO t_user_encrypt_federate VALUES(2, 'plain password3', 'U2FsdGVkX19d4VBBrjaSC7QX9tsdNFcpMNaZaxmJyYs=', 'Phoebe');
INSERT INTO t_user_encrypt_federate VALUES(3, 'plain password4', 'U2FsdGVkX18viyM9JBybwvkks0qCmP56vojYnniqZOo=', 'Ross');
INSERT INTO t_user_encrypt_federate VALUES(4, 'plain password5', 'U2FsdGVkX1+Lbc0iRrg/vk5BDQ+hHqbFEN7TFXztjro=', 'Chandler');
INSERT INTO t_user_encrypt_federate VALUES(5, 'plain password6', 'U2FsdGVkX1/liIWOUCQ2W6sUgkGDSKZ/3QEiMTZtIsg=', 'Joey');

INSERT INTO t_user_encrypt_federate_sharding_0 VALUES(0, 'plain passwordA', 'U2FsdGVkX1/TAZ4ul/UUIbvMJvbZ4SLibJzd7pwmDOM=', 'Rachel');
INSERT INTO t_user_encrypt_federate_sharding_1 VALUES(1, 'plain passwordB', 'U2FsdGVkX1/EWOAnnTvPQIsdleGxw01nnnEa2VUk8vo=', 'Monica');
INSERT INTO t_user_encrypt_federate_sharding_0 VALUES(2, 'plain passwordC', 'U2FsdGVkX19nXmysCFFcVtUFriy5ev7s0RsNT1XNJKg=', 'Phoebe');
INSERT INTO t_user_encrypt_federate_sharding_1 VALUES(3, 'plain passwordD', 'U2FsdGVkX19KwrlD1BIcVtxGOSXGguAUCb2SydUhqRc=', 'Ross');
INSERT INTO t_user_encrypt_federate_sharding_0 VALUES(4, 'plain passwordE', 'U2FsdGVkX1+70hUvvGH16ddUxpK1s5sJXQLQRNbcNeE=', 'Chandler');
INSERT INTO t_user_encrypt_federate_sharding_1 VALUES(5, 'plain passwordF', 'U2FsdGVkX191hCfwkImzIRP1DIAK2M9FLgQSFHa1zlE=', 'Joey');
