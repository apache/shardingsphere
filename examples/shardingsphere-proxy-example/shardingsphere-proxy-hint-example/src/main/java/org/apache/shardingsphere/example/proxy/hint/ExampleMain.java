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

package org.apache.shardingsphere.example.proxy.hint;

import org.apache.shardingsphere.example.core.api.service.ExampleService;
import org.apache.shardingsphere.example.core.jdbc.service.OrderServiceImpl;
import org.apache.shardingsphere.example.proxy.hint.factory.YamlDataSourceFactory;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class ExampleMain {
    
    private static final HintType TYPE = HintType.DATABASE_TABLES;
//    private static final HintType TYPE = HintType.DATABASE_ONLY;
//    private static final HintType TYPE = HintType.MASTER_ONLY;
    
    public static void main(final String[] args) throws SQLException, IOException {
        DataSource dataSource = getDataSource();
        ExampleService exampleService = getExampleService(dataSource);
        exampleService.initEnvironment();
        processWithHintValue(dataSource);
        exampleService.cleanEnvironment();
    }
    
    private static DataSource getDataSource() throws IOException {
        switch (TYPE) {
            case DATABASE_TABLES:
                return YamlDataSourceFactory.createDataSource(getFile("/META-INF/hint-databases-tables.yaml"));
            case DATABASE_ONLY:
                return YamlDataSourceFactory.createDataSource(getFile("/META-INF/hint-databases-only.yaml"));
            case MASTER_ONLY:
                return YamlDataSourceFactory.createDataSource(getFile("/META-INF/hint-master-only.yaml"));
            default:
                throw new UnsupportedOperationException("unsupported type");
        }
    }
    
    private static File getFile(final String configFile) {
        return new File(ExampleMain.class.getResource(configFile).getFile());
    }
    
    private static ExampleService getExampleService(final DataSource dataSource) {
        return new OrderServiceImpl(dataSource);
    }
    
    private static void processWithHintValue(final DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            setHintValue(statement);
            statement.execute("select * from t_order");
            statement.execute("SELECT i.* FROM t_order o, t_order_item i WHERE o.order_id = i.order_id");
            statement.execute("select * from t_order_item");
            statement.execute("INSERT INTO t_order (user_id, address_id, status) VALUES (1, 1, 'init')");
        }
    }
    
    private static void setHintValue(final Statement statement) throws SQLException {
        switch (TYPE) {
            case DATABASE_TABLES:
                statement.execute("sctl:hint addDatabaseShardingValue t_order=1");
                statement.execute("sctl:hint addTableShardingValue t_order=1");
                return;
            case DATABASE_ONLY:
                statement.execute("sctl:hint set DatabaseShardingValue=1");
                return;
            case MASTER_ONLY:
                statement.execute("sctl:hint set MASTER_ONLY=true");
                return;
            default:
                throw new UnsupportedOperationException("unsupported type");
        }
    }
}
