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

package org.apache.shardingsphere.example.sharding.raw.jdbc.factory;

import org.apache.shardingsphere.example.type.ShardingType;
import org.apache.shardingsphere.driver.api.yaml.YamlShardingSphereDataSourceFactory;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public final class YamlRangeDataSourceFactory {
    
    public static DataSource newInstance(final ShardingType shardingType) throws SQLException, IOException {
        switch (shardingType) {
            case SHARDING_DATABASES:
                return YamlShardingSphereDataSourceFactory.createDataSource(getFile("/META-INF/sharding-databases-range.yaml"));
            case SHARDING_TABLES:
                return YamlShardingSphereDataSourceFactory.createDataSource(getFile("/META-INF/sharding-tables-range.yaml"));
            case SHARDING_DATABASES_AND_TABLES:
                return YamlShardingSphereDataSourceFactory.createDataSource(getFile("/META-INF/sharding-databases-tables-range.yaml"));
            case PRIMARY_REPLICA_REPLICATION:
                return YamlShardingSphereDataSourceFactory.createDataSource(getFile("/META-INF/master-slave.yaml"));
            case SHARDING_PRIMARY_REPLICA_REPLICATION:
                return YamlShardingSphereDataSourceFactory.createDataSource(getFile("/META-INF/sharding-master-slave-range.yaml"));
            default:
                throw new UnsupportedOperationException(shardingType.name());
        }
    }
    
    private static File getFile(final String fileName) {
        return new File(YamlRangeDataSourceFactory.class.getResource(fileName).getFile());
    }
}
