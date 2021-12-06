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

package org.apache.shardingsphere.example.sharding.raw.jdbc;

import org.apache.shardingsphere.driver.api.yaml.YamlShardingSphereDataSourceFactory;
import org.apache.shardingsphere.example.core.api.service.ExampleService;
import org.apache.shardingsphere.example.core.jdbc.service.OrderServiceImpl;
import org.apache.shardingsphere.example.type.ShardingType;
import org.apache.shardingsphere.infra.hint.HintManager;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class ShardingHintRawExample {

    private static final ShardingType TYPE = ShardingType.SHARDING_HINT_DATABASES_ONLY;
//    private static final ShardingType TYPE = ShardingType.SHARDING_HINT_DATABASES_TABLES;

    public static void main(final String[] args) throws SQLException, IOException {
        DataSource dataSource = getDataSource();
        ExampleService exampleService = getExampleService(dataSource);
        exampleService.initEnvironment();
        processWithHintValue(dataSource);
        exampleService.cleanEnvironment();
    }
    
    private static DataSource getDataSource() throws IOException, SQLException {
        switch (TYPE) {
            case SHARDING_HINT_DATABASES_ONLY:
                return YamlShardingSphereDataSourceFactory.createDataSource(getFile("/META-INF/sharding-hint-databases-only.yaml"));
            case SHARDING_HINT_DATABASES_TABLES:
                return YamlShardingSphereDataSourceFactory.createDataSource(getFile("/META-INF/sharding-hint-databases-tables.yaml"));
            default:
                throw new UnsupportedOperationException("unsupported type");
        }
    }
    
    private static File getFile(final String configFile) {
        return new File(ShardingHintRawExample.class.getResource(configFile).getFile());
    }
    
    private static ExampleService getExampleService(final DataSource dataSource) {
        return new OrderServiceImpl(dataSource);
    }
    
    private static void processWithHintValue(final DataSource dataSource) throws SQLException {
        try (HintManager hintManager = HintManager.getInstance();
             Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            setHintValue(hintManager);
            statement.execute("select * from t_order");
            statement.execute("SELECT i.* FROM t_order o, t_order_item i WHERE o.order_id = i.order_id");
            statement.execute("select * from t_order_item");
            statement.execute("INSERT INTO t_order (user_id, address_id, status) VALUES (1, 1, 'init')");
        }
    }
    
    private static void setHintValue(final HintManager hintManager) {
        switch (TYPE) {
            case SHARDING_HINT_DATABASES_ONLY:
                hintManager.addDatabaseShardingValue("t_order", 2L);
                hintManager.addTableShardingValue("t_order", 1L);
                return;
            case SHARDING_HINT_DATABASES_TABLES:
                hintManager.setDatabaseShardingValue(1L);
                return;
            default:
                throw new UnsupportedOperationException("unsupported type");
        }
    }
}

