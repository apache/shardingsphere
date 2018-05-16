/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.example.jdbc.main.orche.yaml.etcd;

import io.shardingsphere.example.jdbc.fixture.DataRepository;
import io.shardingsphere.example.jdbc.main.orche.yaml.zookeeper.ShardingOnlyWithTables;
import io.shardingsphere.jdbc.orchestration.api.yaml.YamlOrchestrationShardingDataSourceFactory;
import io.shardingsphere.jdbc.orchestration.internal.OrchestrationShardingDataSource;

import javax.sql.DataSource;
import java.io.File;

public class ShardingOnlyWithDatabasesAndTables {
    
    private static final boolean LOAD_CONFIG_FROM_REG_CENTER = false;
    
    public static void main(final String[] args) throws Exception {
        DataSource dataSource = YamlOrchestrationShardingDataSourceFactory.createDataSource(getYamlFile());
        new DataRepository(dataSource).demo();
        ((OrchestrationShardingDataSource) dataSource).close();
    }
    
    private static File getYamlFile() {
        String path = LOAD_CONFIG_FROM_REG_CENTER ? "/META-INF/orche/etcd/cloud/sharding-databases-tables.yaml" : "/META-INF/orche/etcd/local/sharding-databases-tables.yaml";
        return new File(ShardingOnlyWithTables.class.getResource(path).getFile());
    }
}
