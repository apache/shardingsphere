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

package io.shardingsphere.example.jdbc.orche;

import io.shardingsphere.example.repository.api.service.CommonService;
import io.shardingsphere.example.repository.jdbc.repository.JDBCOrderItemRepositoryImpl;
import io.shardingsphere.example.repository.jdbc.repository.JDBCOrderRepositoryImpl;
import io.shardingsphere.example.repository.jdbc.service.RawPojoService;
import io.shardingsphere.example.type.RegistryCenterType;
import io.shardingsphere.example.type.ShardingType;
import io.shardingsphere.shardingjdbc.orchestration.api.yaml.YamlOrchestrationMasterSlaveDataSourceFactory;
import io.shardingsphere.shardingjdbc.orchestration.api.yaml.YamlOrchestrationShardingDataSourceFactory;
import io.shardingsphere.shardingjdbc.orchestration.internal.datasource.OrchestrationMasterSlaveDataSource;
import io.shardingsphere.shardingjdbc.orchestration.internal.datasource.OrchestrationShardingDataSource;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

/*
 * 1. Please make sure master-slave data sync on MySQL is running correctly. Otherwise this example will query empty data from slave.
 * 2. Please make sure sharding-orchestration-reg-zookeeper-curator in your pom if registryCenterType = RegistryCenterType.ZOOKEEPER.
 * 3. Please make sure sharding-orchestration-reg-etcd in your pom if registryCenterType = RegistryCenterType.ETCD.
 */
public class YamlConfigurationExample {
    
    private static ShardingType shardingType = ShardingType.SHARDING_DATABASES;
//    private static ShardingType shardingType = ShardingType.SHARDING_TABLES;
//    private static ShardingType shardingType = ShardingType.SHARDING_DATABASES_AND_TABLES;
//    private static ShardingType shardingType = ShardingType.MASTER_SLAVE;
//    private static ShardingType shardingType = ShardingType.SHARDING_MASTER_SLAVE;
    
    private static RegistryCenterType registryCenterType = RegistryCenterType.ZOOKEEPER;
//    private static RegistryCenterType registryCenterType = RegistryCenterType.ETCD;
    
    private static boolean loadConfigFromRegCenter = false;
//    private static boolean loadConfigFromRegCenter = true;
    
    public static void main(final String[] args) throws Exception {
        process(getDataSource());
    }
    
    private static DataSource getDataSource() throws IOException, SQLException {
        return ShardingType.MASTER_SLAVE == shardingType
                ? YamlOrchestrationMasterSlaveDataSourceFactory.createDataSource(getYamlFile()) : YamlOrchestrationShardingDataSourceFactory.createDataSource(getYamlFile());
    }
    
    private static File getYamlFile() {
        String result;
        switch (shardingType) {
            case SHARDING_DATABASES:
                result = String.format("/META-INF/%s/%s/sharding-databases.yaml", registryCenterType.name().toLowerCase(), loadConfigFromRegCenter ? "cloud" : "local");
                break;
            case SHARDING_TABLES:
                result = String.format("/META-INF/%s/%s/sharding-tables.yaml", registryCenterType.name().toLowerCase(), loadConfigFromRegCenter ? "cloud" : "local");
                break;
            case SHARDING_DATABASES_AND_TABLES:
                result = String.format("/META-INF/%s/%s/sharding-databases-tables.yaml", registryCenterType.name().toLowerCase(), loadConfigFromRegCenter ? "cloud" : "local");
                break;
            case MASTER_SLAVE:
                result = String.format("/META-INF/%s/%s/master-slave.yaml", registryCenterType.name().toLowerCase(), loadConfigFromRegCenter ? "cloud" : "local");
                break;
            case SHARDING_MASTER_SLAVE:
                result = String.format("/META-INF/%s/%s/sharding-master-slave.yaml", registryCenterType.name().toLowerCase(), loadConfigFromRegCenter ? "cloud" : "local");
                break;
            default:
                throw new UnsupportedOperationException(shardingType.name());
        }
        return new File(YamlConfigurationExample.class.getResource(result).getFile());
    }
    
    private static void process(final DataSource dataSource) throws Exception {
        CommonService commonService = getCommonService(dataSource);
        commonService.initEnvironment();
        commonService.processSuccess(false);
        commonService.cleanEnvironment();
        closeDataSource(dataSource);
    }
    
    private static CommonService getCommonService(final DataSource dataSource) {
        return new RawPojoService(new JDBCOrderRepositoryImpl(dataSource), new JDBCOrderItemRepositoryImpl(dataSource));
    }
    
    private static void closeDataSource(final DataSource dataSource) throws Exception {
        if (dataSource instanceof OrchestrationMasterSlaveDataSource) {
            ((OrchestrationMasterSlaveDataSource) dataSource).close();
        } else {
            ((OrchestrationShardingDataSource) dataSource).close();
        }
    }
}
