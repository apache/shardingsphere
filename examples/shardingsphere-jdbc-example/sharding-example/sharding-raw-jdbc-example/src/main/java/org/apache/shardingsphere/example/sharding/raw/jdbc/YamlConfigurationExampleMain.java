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

package org.apache.shardingsphere.example.sharding.raw.jdbc;

import org.apache.shardingsphere.example.core.api.ExampleExecuteTemplate;
import org.apache.shardingsphere.example.core.api.service.ExampleService;
import org.apache.shardingsphere.example.core.jdbc.service.OrderServiceImpl;
import org.apache.shardingsphere.example.sharding.raw.jdbc.factory.YamlDataSourceFactory;
import org.apache.shardingsphere.example.type.ShardingType;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;

/*
 * Please make sure primary-replica-replication data sync on MySQL is running correctly. Otherwise this example will query empty data from replica.
 */
public final class YamlConfigurationExampleMain {
    
    private static ShardingType shardingType = ShardingType.SHARDING_DATABASES;
//    private static ShardingType shardingType = ShardingType.SHARDING_TABLES;
//    private static ShardingType shardingType = ShardingType.SHARDING_DATABASES_AND_TABLES;
//    private static ShardingType shardingType = ShardingType.PRIMARY_REPLICA_REPLICATION;
//    private static ShardingType shardingType = ShardingType.SHARDING_PRIMARY_REPLICA_REPLICATION;
    
    public static void main(final String[] args) throws SQLException, IOException {
        DataSource dataSource = YamlDataSourceFactory.newInstance(shardingType);
        ExampleExecuteTemplate.run(getExampleService(dataSource));
    }
    
    private static ExampleService getExampleService(final DataSource dataSource) {
        return new OrderServiceImpl(dataSource);
    }
}
