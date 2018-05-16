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

package io.shardingsphere.example.jdbc.main.orche.yaml.zookeeper;

import io.shardingsphere.example.jdbc.fixture.DataRepository;
import io.shardingsphere.jdbc.orchestration.api.OrchestrationShardingDataSourceFactory;
import io.shardingsphere.jdbc.orchestration.api.yaml.YamlOrchestrationShardingDataSourceFactory;

import javax.sql.DataSource;
import java.io.File;

public final class OrchestrationYamlShardingTableMain {
    
    public static void main(final String[] args) throws Exception {
        DataSource dataSource = YamlOrchestrationShardingDataSourceFactory.createDataSource(new File(
                OrchestrationYamlShardingTableMain.class.getResource("/META-INF/orche/zookeeper/yamlShardingTableByLocalConfig.yaml").getFile()));
//        DataSource dataSource = YamlOrchestrationShardingDataSourceFactory.createDataSource(new File(
//                OrchestrationYamlShardingMain.class.getResource("/META-INF/orche/yamlShardingDatabaseAndTableByCloudConfig.yaml").getFile()));
        new DataRepository(dataSource).demo();
        OrchestrationShardingDataSourceFactory.closeQuietly(dataSource);   }
}
