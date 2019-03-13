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

package io.shardingsphere.quickstart.sharding.data.jdbc;

import io.shardingsphere.quickstart.common.jdbc.repository.OrderItemRepositoryImpl;
import io.shardingsphere.quickstart.common.jdbc.repository.OrderRepositoryImpl;
import io.shardingsphere.quickstart.common.jdbc.service.CommonServiceImpl;
import io.shardingsphere.quickstart.common.service.CommonService;
import io.shardingsphere.quickstart.sharding.data.jdbc.factory.YamlDataSourceFactory;
import io.shardingsphere.quickstart.type.ShardingType;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;

/*
 * Please make sure master-slave data sync on MySQL is running correctly. Otherwise this example will query empty data from slave.
 */
public class YamlConfigurationExample {
    
    private static ShardingType shardingType = ShardingType.SHARDING_DATABASES;
//    private static ShardingType shardingType = ShardingType.SHARDING_TABLES;
//    private static ShardingType shardingType = ShardingType.SHARDING_DATABASES_AND_TABLES;
//    private static ShardingType shardingType = ShardingType.MASTER_SLAVE;
//    private static ShardingType shardingType = ShardingType.SHARDING_MASTER_SLAVE;
    
    public static void main(final String[] args) throws SQLException, IOException {
        DataSource dataSource = YamlDataSourceFactory.newInstance(shardingType);
        CommonService commonService = getCommonService(dataSource);
        commonService.initEnvironment();
        commonService.processSuccess();
        commonService.cleanEnvironment();
    }
    
    private static CommonService getCommonService(final DataSource dataSource) {
        return new CommonServiceImpl(new OrderRepositoryImpl(dataSource), new OrderItemRepositoryImpl(dataSource));
    }
}
