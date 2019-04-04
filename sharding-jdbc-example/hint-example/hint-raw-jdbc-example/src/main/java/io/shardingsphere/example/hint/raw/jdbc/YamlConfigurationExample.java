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

package io.shardingsphere.example.hint.raw.jdbc;

import io.shardingsphere.example.common.jdbc.repository.OrderItemRepositoryImpl;
import io.shardingsphere.example.common.jdbc.repository.OrderRepositoryImpl;
import io.shardingsphere.example.common.jdbc.service.CommonServiceImpl;
import io.shardingsphere.example.common.service.CommonService;
import org.apache.shardingsphere.api.hint.HintManager;
import org.apache.shardingsphere.shardingjdbc.api.yaml.YamlShardingDataSourceFactory;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/*
 * Please make sure master-slave data sync on MySQL is running correctly. Otherwise this example will query empty data from slave.
 */
public class YamlConfigurationExample {
    
    public static void main(final String[] args) throws SQLException, IOException {
        DataSource dataSource = YamlShardingDataSourceFactory.createDataSource(getFile("/META-INF/hint-databases.yaml"));
        CommonService commonService = getCommonService(dataSource);
        commonService.initEnvironment();
        try (HintManager hintManager = HintManager.getInstance()) {
            processWithHintValue(dataSource, hintManager);
        }
        commonService.cleanEnvironment();
    }
    
    private static File getFile(final String fileName) {
        return new File(Thread.currentThread().getClass().getResource(fileName).getFile());
    }
    
    private static CommonService getCommonService(final DataSource dataSource) {
        return new CommonServiceImpl(new OrderRepositoryImpl(dataSource), new OrderItemRepositoryImpl(dataSource));
    }
    
    private static void processWithHintValue(final DataSource dataSource, final HintManager hintManager) throws SQLException {
        hintManager.addDatabaseShardingValue("t_order", 1L);
        hintManager.addTableShardingValue("t_order", 1L);
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("select * from t_order");
            statement.execute("select * from t_order_item");
        }
    }
}

