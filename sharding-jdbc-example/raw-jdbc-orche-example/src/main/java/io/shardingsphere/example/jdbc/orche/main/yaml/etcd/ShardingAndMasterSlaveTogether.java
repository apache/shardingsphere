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

package io.shardingsphere.example.jdbc.orche.main.yaml.etcd;

import io.shardingsphere.example.repository.api.service.CommonService;
import io.shardingsphere.example.repository.jdbc.repository.JDBCOrderItemRepositoryImpl;
import io.shardingsphere.example.repository.jdbc.repository.JDBCOrderRepositoryImpl;
import io.shardingsphere.example.repository.jdbc.service.RawPojoService;
import io.shardingsphere.shardingjdbc.orchestration.api.yaml.YamlOrchestrationShardingDataSourceFactory;
import io.shardingsphere.shardingjdbc.orchestration.internal.datasource.OrchestrationShardingDataSource;

import javax.sql.DataSource;
import java.io.File;

public class ShardingAndMasterSlaveTogether {
    
    private static final boolean LOAD_CONFIG_FROM_REG_CENTER = false;
    
    public static void main(final String[] args) throws Exception {
        DataSource dataSource = YamlOrchestrationShardingDataSourceFactory.createDataSource(getYamlFile());
        CommonService commonService = new RawPojoService(new JDBCOrderRepositoryImpl(dataSource), new JDBCOrderItemRepositoryImpl(dataSource));
        commonService.initEnvironment();
        commonService.processSuccess();
        commonService.cleanEnvironment();
        ((OrchestrationShardingDataSource) dataSource).close();
    }
    
    private static File getYamlFile() {
        String path = LOAD_CONFIG_FROM_REG_CENTER ? "/META-INF/etcd/cloud/sharding-master-slave.yaml" : "/META-INF/etcd/local/sharding-master-slave.yaml";
        return new File(ShardingAndMasterSlaveTogether.class.getResource(path).getFile());
    }
}
