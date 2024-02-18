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

package org.apache.shardingsphere.test.it.data.pipeline.core.dump;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.column.ColumnValueReaderEngine;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNull;

class ColumnValueReaderEngineTest {
    
    @Test
    void assertReadValue() throws SQLException {
        ColumnValueReaderEngine columnValueReaderEngine = new ColumnValueReaderEngine(TypedSPILoader.getService(DatabaseType.class, "H2"));
        try (
                HikariDataSource dataSource = createDataSource(RandomStringUtils.randomAlphanumeric(6));
                Connection connection = dataSource.getConnection()) {
            connection.createStatement().execute("CREATE TABLE t_order (order_id INT PRIMARY KEY, user_id INT, status VARCHAR(12), c_year year)");
            connection.createStatement().executeUpdate("INSERT INTO t_order(order_id, user_id, status, c_year) VALUES (1, 2,'ok', null)");
            ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM t_order");
            resultSet.next();
            assertThat(columnValueReaderEngine.read(resultSet, resultSet.getMetaData(), 1), is(1));
            assertThat(columnValueReaderEngine.read(resultSet, resultSet.getMetaData(), 2), is(2));
            assertThat(columnValueReaderEngine.read(resultSet, resultSet.getMetaData(), 3), is("ok"));
            assertNull(columnValueReaderEngine.read(resultSet, resultSet.getMetaData(), 4));
        }
    }
    
    private static HikariDataSource createDataSource(final String databaseName) {
        HikariDataSource result = new HikariDataSource();
        result.setJdbcUrl(String.format("jdbc:h2:mem:%s;DATABASE_TO_UPPER=false;MODE=MySQL", databaseName));
        result.setUsername("root");
        result.setPassword("root");
        result.setMaximumPoolSize(10);
        result.setMinimumIdle(2);
        return result;
    }
}
