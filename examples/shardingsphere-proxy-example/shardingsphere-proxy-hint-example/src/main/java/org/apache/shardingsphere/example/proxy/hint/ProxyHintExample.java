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

public final class ProxyHintExample {
    
    private static final HintType TYPE = HintType.DATABASE_TABLES;
//    private static final HintType TYPE = HintType.DATABASE_ONLY;
//    private static final HintType TYPE = HintType.WRITE_ONLY;
//    private static final HintType TYPE = HintType.SQL_HINT_DATASOURCE;
    
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
            case WRITE_ONLY:
                return YamlDataSourceFactory.createDataSource(getFile("/META-INF/hint-write-only.yaml"));
            case SQL_HINT_DATASOURCE:
                return YamlDataSourceFactory.createDataSource(getFile("/META-INF/sql-hint-data-source.yaml"));
            default:
                throw new UnsupportedOperationException("unsupported type");
        }
    }
    
    private static File getFile(final String configFile) {
        return new File(ProxyHintExample.class.getResource(configFile).getFile());
    }
    
    private static ExampleService getExampleService(final DataSource dataSource) {
        return new OrderServiceImpl(dataSource);
    }
    
    private static void processWithHintValue(final DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            if (TYPE == HintType.SQL_HINT_DATASOURCE) {
                statement.execute("/* ShardingSphere hint: dataSourceName=ds_1 */select * from t_order");
                statement.execute("/* ShardingSphere hint: dataSourceName=ds_1 */SELECT i.* FROM t_order o, t_order_item i WHERE o.order_id = i.order_id");
                statement.execute("/* ShardingSphere hint: dataSourceName=ds_1 */select * from t_order_item");
                statement.execute("/* ShardingSphere hint: dataSourceName=ds_1 */INSERT INTO t_order (user_id, address_id, status) VALUES (1, 1, 'init')");
            } else {
                setHintValue(statement);
                statement.execute("select * from t_order");
                statement.execute("SELECT i.* FROM t_order o, t_order_item i WHERE o.order_id = i.order_id");
                statement.execute("select * from t_order_item");
                statement.execute("INSERT INTO t_order (user_id, address_id, status) VALUES (1, 1, 'init')");
            }
        }
    }
    
    private static void setHintValue(final Statement statement) throws SQLException {
        switch (TYPE) {
            case DATABASE_TABLES:
                statement.execute("add sharding hint database_value t_order=1");
                statement.execute("add sharding hint table_value t_order=1");
                return;
            case DATABASE_ONLY:
                statement.execute("set sharding hint database_value=1");
                return;
            case WRITE_ONLY:
                statement.execute("set readwrite_splitting hint source=write");
                return;
            default:
                throw new UnsupportedOperationException("unsupported type");
        }
    }
}
