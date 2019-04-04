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
    
//    private static final HintType TYPE = HintType.DATABASE_ONLY;
    private static final HintType TYPE = HintType.DATABASE_TABLES;
    
    public static void main(final String[] args) throws SQLException, IOException {
        DataSource dataSource = YamlShardingDataSourceFactory.createDataSource(getFile());
        CommonService commonService = getCommonService(dataSource);
        commonService.initEnvironment();
        try (HintManager hintManager = HintManager.getInstance()) {
            processWithHintValue(dataSource, hintManager);
        }
        commonService.cleanEnvironment();
    }
    
    private static File getFile() {
        switch (TYPE) {
            case DATABASE_ONLY:
                return new File(Thread.currentThread().getClass().getResource("/META-INF/hint-databases-only.yaml").getFile());
            case DATABASE_TABLES:
                return new File(Thread.currentThread().getClass().getResource("/META-INF/hint-databases-tables.yaml").getFile());
            case MASTER_ONLY:
                return new File(Thread.currentThread().getClass().getResource("/META-INF/hint-databases-only.yaml").getFile());
            default:
                throw new UnsupportedOperationException("unsupported type");
        }
    }
    
    private static CommonService getCommonService(final DataSource dataSource) {
        return new CommonServiceImpl(new OrderRepositoryImpl(dataSource), new OrderItemRepositoryImpl(dataSource));
    }
    
    private static void processWithHintValue(final DataSource dataSource, final HintManager hintManager) throws SQLException {
        setHintValue(hintManager);
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("select * from t_order");
            statement.execute("SELECT i.* FROM t_order o, t_order_item i WHERE o.order_id = i.order_id");
            statement.execute("select * from t_order_item");
            statement.execute("INSERT INTO t_order (user_id, status) VALUES (1, 'init')");
        }
    }
    
    private static void setHintValue(final HintManager hintManager) {
        switch (TYPE) {
            case DATABASE_ONLY:
                hintManager.setDatabaseShardingValue(1L);
                return;
            case DATABASE_TABLES:
                hintManager.addDatabaseShardingValue("t_order", 1L);
                hintManager.addTableShardingValue("t_order", 1L);
                return;
            default:
                throw new UnsupportedOperationException("unsupported type");
        }
    }
}

