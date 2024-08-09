#!/bin/bash
#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

nohup bash -c "/opt/shardingsphere-proxy/bin/start.sh -g" </dev/null &>/dev/null &
sleep 20

for ((i=1; i<=10; i++))
do mysql -uroot -h127.0.0.1 -proot -P3307 -e "USE sharding_db;INSERT INTO t_order (order_id, user_id, status) VALUES (${i}, ${i}, \"INSERT_TEST\");"
done
mysql -uroot -h127.0.0.1 -proot -P3307 -e "USE sharding_db;set autocommit=0;INSERT INTO t_order (order_id, user_id, status) VALUES (1000, 1000, \"ROLL_BACK\");rollback"
mysql -uroot -h127.0.0.1 -proot -P3307 -e "USE sharding_db;set autocommit=0;UPDATE t_order SET status = 1000 WHERE order_id =1000;commit;"
mysql -uroot -h127.0.0.1 -proot -P3307 -e "USE sharding_db;SELECT * FROM t_order;"
for ((i=1; i<=10; i++))
do mysql -uroot -h127.0.0.1 -proot -P3307 -e "USE sharding_db;DELETE FROM t_order WHERE order_id=${i};"
done
set +e
mysql -uroot -h127.0.0.1 -proot -P3307 -e "USE sharding_db;SELECT * FROM non_existent_table;"
set -e
