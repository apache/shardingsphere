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

package io.shardingsphere.example.jdbc.nodep.factory;

import io.shardingsphere.example.jdbc.nodep.config.MasterSlaveConfiguration;
import io.shardingsphere.example.jdbc.nodep.config.ShardingDatabasesAndTablesConfigurationPrecise;
import io.shardingsphere.example.jdbc.nodep.config.ShardingDatabasesConfigurationPrecise;
import io.shardingsphere.example.jdbc.nodep.config.ShardingMasterSlaveConfigurationPrecise;
import io.shardingsphere.example.jdbc.nodep.config.ShardingTablesConfigurationPrecise;
import io.shardingsphere.example.type.ShardingType;

import javax.sql.DataSource;
import java.sql.SQLException;

public class DataSourceFactory {
    
    public static DataSource newInstance(final ShardingType shardingType) throws SQLException {
        switch (shardingType) {
            case SHARDING_DATABASES:
                return new ShardingDatabasesConfigurationPrecise().getDataSource();
            case SHARDING_TABLES:
                return new ShardingTablesConfigurationPrecise().getDataSource();
            case SHARDING_DATABASES_AND_TABLES:
                return new ShardingDatabasesAndTablesConfigurationPrecise().getDataSource();
            case MASTER_SLAVE:
                return new MasterSlaveConfiguration().getDataSource();
            case SHARDING_MASTER_SLAVE:
                return new ShardingMasterSlaveConfigurationPrecise().getDataSource();
            default:
                throw new UnsupportedOperationException(shardingType.name());
        }
    }
}
