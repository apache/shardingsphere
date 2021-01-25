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

package org.apache.shardingsphere.example.shadow.table.raw.jdbc;

import org.apache.shardingsphere.example.core.api.ExampleExecuteTemplate;
import org.apache.shardingsphere.example.core.api.service.ExampleService;
import org.apache.shardingsphere.example.core.jdbc.repository.ShadowUserRepositoryImpl;
import org.apache.shardingsphere.example.core.jdbc.service.ShadowUserServiceImpl;
import org.apache.shardingsphere.example.shadow.table.raw.jdbc.factory.YamlDataSourceFactory;
import org.apache.shardingsphere.example.type.ShardingType;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;

public final class YamlConfigurationExampleMain {
    
    private static ShardingType shardingType = ShardingType.SHADOW;
//    private static ShardingType shardingType = ShardingType.REPLICA_QUERY_SHADOW;
//    private static ShardingType shardingType = ShardingType.ENCRYPT_SHADOW;
//    private static ShardingType shardingType = ShardingType.SHARDING_SHADOW_DATABASES;
    
    public static void main(final String[] args) throws SQLException, IOException {
        DataSource dataSource = YamlDataSourceFactory.newInstance(shardingType);
        ExampleExecuteTemplate.run(getExampleService(dataSource));
    }
    
    private static ExampleService getExampleService(final DataSource dataSource) {
        return new ShadowUserServiceImpl(new ShadowUserRepositoryImpl(dataSource));
    }
}
