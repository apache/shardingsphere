/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.example.jdbc.yaml;

import io.shardingsphere.example.jdbc.yaml.repository.RawJdbcYamlRepository;
import io.shardingsphere.core.api.MasterSlaveDataSourceFactory;

import javax.sql.DataSource;
import java.io.File;

/*
 * Please make sure master-slave data sync on MySQL is running correctly. Otherwise this example will query empty data from slave.
 */
public class RawJdbcYamlMasterSlaveMain {
    
    public static void main(final String[] args) throws Exception {
        DataSource dataSource = MasterSlaveDataSourceFactory.createDataSource(new File(RawJdbcYamlShardingDatabaseAndTableMain.class.getResource("/META-INF/master-slave.yaml").getFile()));
        new RawJdbcYamlRepository(dataSource).demo();
    }
}
