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

package org.apache.shardingsphere.scaling.core.fixture;

public final class FixtureShardingSphereJDBCConfiguration {
    
    public static final String DATA_SOURCE = "dataSources:\n ds_0:\n  dataSourceClassName: com.zaxxer.hikari.HikariDataSource\n  props:\n    "
            + "jdbcUrl: jdbc:h2:mem:test_db_1;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL\n    username: root\n    password: 'password'\n    "
            + "connectionTimeout: 30000\n    idleTimeout: 60000\n    maxLifetime: 1800000\n    maxPoolSize: 50\n    minPoolSize: 1\n    maintenanceIntervalMilliseconds: 30000\n    "
            + "readOnly: false\n";
    
    public static final String RULE = "rules:\n- !SHARDING\n  defaultDatabaseStrategy:\n    standard:\n      shardingAlgorithmName: inline\n      shardingColumn: user_id\n  "
            + "tables:\n    t1:\n      actualDataNodes: ds_0.t1\n      keyGenerateStrategy:\n        column: order_id\n      "
            + "logicTable: t1\n      tableStrategy:\n        standard:\n          shardingAlgorithmName: inline\n          shardingColumn: order_id\n    "
            + "t2:\n      actualDataNodes: ds_0.t2\n      keyGenerateStrategy:\n        column: order_item_id\n      "
            + "logicTable: t2\n      tableStrategy:\n        standard:\n          shardingAlgorithmName: inline\n          shardingColumn: order_id";
}
