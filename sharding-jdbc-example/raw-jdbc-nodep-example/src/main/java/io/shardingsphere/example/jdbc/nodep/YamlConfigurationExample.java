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

package io.shardingsphere.example.jdbc.nodep;

import io.shardingsphere.example.repository.api.service.CommonService;
import io.shardingsphere.example.repository.jdbc.repository.JDBCOrderItemRepositoryImpl;
import io.shardingsphere.example.repository.jdbc.repository.JDBCOrderRepositoryImpl;
import io.shardingsphere.example.repository.jdbc.service.RawPojoService;
import io.shardingsphere.example.type.ShardingType;
import org.apache.shardingsphere.shardingjdbc.api.yaml.YamlMasterSlaveDataSourceFactory;
import org.apache.shardingsphere.shardingjdbc.api.yaml.YamlShardingDataSourceFactory;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

/*
 * Please make sure master-slave data sync on MySQL is running correctly. Otherwise this example will query empty data from slave.
 */
public class YamlConfigurationExample {
    
    private static ShardingType type = ShardingType.SHARDING_DATABASES;
//    private static ShardingType type = ShardingType.SHARDING_TABLES;
//    private static ShardingType type = ShardingType.SHARDING_DATABASES_AND_TABLES;
//    private static ShardingType type = ShardingType.MASTER_SLAVE;
//    private static ShardingType type = ShardingType.SHARDING_MASTER_SLAVE;
    
    public static void main(final String[] args) throws SQLException, IOException {
        process(getDataSource());
    }
    
    private static DataSource getDataSource() throws IOException, SQLException {
        return ShardingType.MASTER_SLAVE == type ? YamlMasterSlaveDataSourceFactory.createDataSource(getYamlFile()) : YamlShardingDataSourceFactory.createDataSource(getYamlFile());
    }
    
    private static File getYamlFile() {
        String result;
        switch (type) {
            case SHARDING_DATABASES:
                result = "/META-INF/sharding-databases.yaml";
                break;
            case SHARDING_TABLES:
                result = "/META-INF/sharding-tables.yaml";
                break;
            case SHARDING_DATABASES_AND_TABLES:
                result = "/META-INF/sharding-databases-tables.yaml";
                break;
            case MASTER_SLAVE:
                result = "/META-INF/master-slave.yaml";
                break;
            case SHARDING_MASTER_SLAVE:
                result = "/META-INF/sharding-master-slave.yaml";
                break;
            default:
                throw new UnsupportedOperationException(type.name());
        }
        return new File(YamlConfigurationExample.class.getResource(result).getFile());
    }
    
    private static void process(final DataSource dataSource) {
        CommonService commonService = getCommonService(dataSource);
        commonService.initEnvironment();
        commonService.processSuccess(false);
        commonService.cleanEnvironment();
    }
    
    private static CommonService getCommonService(final DataSource dataSource) {
        return new RawPojoService(new JDBCOrderRepositoryImpl(dataSource), new JDBCOrderItemRepositoryImpl(dataSource));
    }
}
