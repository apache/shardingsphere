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

package io.shardingsphere.example.jdbc.main.nodep.yaml;

import io.shardingsphere.core.api.yaml.YamlShardingDataSourceFactory;
import io.shardingsphere.example.jdbc.fixture.DataRepository;

import javax.sql.DataSource;
import java.io.File;

public class ShardingOnlyWithDatabases {
    
    public static void main(final String[] args) throws Exception {
        DataSource dataSource = YamlShardingDataSourceFactory.createDataSource(getYamlFile());
        new DataRepository(dataSource).demo();
    }
    
    private static File getYamlFile() {
        return new File(ShardingOnlyWithDatabases.class.getResource("/META-INF/nodep/sharding-databases.yaml").getFile());
    }
}
