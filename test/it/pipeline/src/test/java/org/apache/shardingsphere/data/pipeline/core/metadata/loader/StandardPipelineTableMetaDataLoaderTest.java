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

package org.apache.shardingsphere.data.pipeline.core.metadata.loader;

import com.zaxxer.hikari.HikariDataSource;
import lombok.SneakyThrows;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSource;
import org.apache.shardingsphere.data.pipeline.core.metadata.model.PipelineColumnMetaData;
import org.apache.shardingsphere.data.pipeline.core.metadata.model.PipelineIndexMetaData;
import org.apache.shardingsphere.data.pipeline.core.metadata.model.PipelineTableMetaData;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StandardPipelineTableMetaDataLoaderTest {
    
    private PipelineDataSource dataSource;
    
    @BeforeEach
    void setUp() {
        dataSource = new PipelineDataSource(createHikariDataSource(), TypedSPILoader.getService(DatabaseType.class, "H2"));
    }
    
    private HikariDataSource createHikariDataSource() {
        HikariDataSource result = new HikariDataSource();
        result.setJdbcUrl("jdbc:h2:mem:standard;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL");
        result.setUsername("root");
        result.setPassword("root");
        result.setMaximumPoolSize(10);
        result.setMinimumIdle(2);
        result.setConnectionTimeout(15L * 1000L);
        result.setIdleTimeout(40L * 1000L);
        return result;
    }
    
    @AfterEach
    @SneakyThrows(SQLException.class)
    void cleanUp() {
        try (Connection connection = dataSource.getConnection()) {
            connection.createStatement().execute("DROP TABLE IF EXISTS t_order");
        }
    }
    
    @Test
    @SneakyThrows(SQLException.class)
    void assertLoadPrimaryKey() {
        try (Connection connection = dataSource.getConnection()) {
            connection.createStatement().execute("CREATE TABLE t_order (order_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(50), PRIMARY KEY (order_id))");
        }
        StandardPipelineTableMetaDataLoader loader = new StandardPipelineTableMetaDataLoader(dataSource);
        PipelineTableMetaData actual = loader.getTableMetaData(null, "t_order");
        assertThat(actual.getPrimaryKeyColumns().get(0), is("order_id"));
        assertThat(actual.getColumnNames().size(), is(3));
        Collection<PipelineIndexMetaData> uniqueIndexes = actual.getUniqueIndexes();
        assertThat(uniqueIndexes.size(), is(1));
        PipelineIndexMetaData indexMetaData = uniqueIndexes.iterator().next();
        assertThat(indexMetaData.getColumns().size(), is(1));
        assertThat(indexMetaData.getColumns().get(0).getName(), is("order_id"));
        assertOrderId(actual.getColumnMetaData("order_id"), true);
        assertUserId(actual.getColumnMetaData("user_id"), false, false);
        assertStatus(actual.getColumnMetaData("status"));
    }
    
    private void assertOrderId(final PipelineColumnMetaData orderIdColumn, final boolean expectedIsPrimaryKey) {
        assertThat(orderIdColumn.getOrdinalPosition(), is(1));
        assertThat(orderIdColumn.getName(), is("order_id"));
        assertThat(orderIdColumn.getDataType(), is(Types.INTEGER));
        assertFalse(orderIdColumn.isNullable());
        assertThat(orderIdColumn.isPrimaryKey(), is(expectedIsPrimaryKey));
        assertTrue(orderIdColumn.isUniqueKey());
    }
    
    private void assertUserId(final PipelineColumnMetaData userIdColumn, final boolean expectedIsPrimaryKey, final boolean expectedIsUniqueKey) {
        assertThat(userIdColumn.getOrdinalPosition(), is(2));
        assertThat(userIdColumn.getName(), is("user_id"));
        assertThat(userIdColumn.getDataType(), is(Types.INTEGER));
        assertFalse(userIdColumn.isNullable());
        assertThat(userIdColumn.isPrimaryKey(), is(expectedIsPrimaryKey));
        assertThat(userIdColumn.isUniqueKey(), is(expectedIsUniqueKey));
    }
    
    private void assertStatus(final PipelineColumnMetaData statusColumn) {
        assertThat(statusColumn.getOrdinalPosition(), is(3));
        assertThat(statusColumn.getName(), is("status"));
        assertThat(statusColumn.getDataType(), is(Types.VARCHAR));
        assertTrue(statusColumn.isNullable());
        assertFalse(statusColumn.isPrimaryKey());
        assertFalse(statusColumn.isUniqueKey());
    }
    
    @Test
    @SneakyThrows(SQLException.class)
    void assertLoadUniqueKey() {
        try (Connection connection = dataSource.getConnection()) {
            connection.createStatement().execute("CREATE TABLE t_order (order_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(50), UNIQUE KEY (order_id))");
        }
        StandardPipelineTableMetaDataLoader loader = new StandardPipelineTableMetaDataLoader(dataSource);
        PipelineTableMetaData actual = loader.getTableMetaData(null, "t_order");
        assertTrue(actual.getPrimaryKeyColumns().isEmpty());
        assertThat(actual.getColumnNames().size(), is(3));
        Collection<PipelineIndexMetaData> uniqueIndexes = actual.getUniqueIndexes();
        PipelineIndexMetaData indexMetaData = uniqueIndexes.iterator().next();
        assertThat(indexMetaData.getColumns().size(), is(1));
        assertThat(indexMetaData.getColumns().get(0).getName(), is("order_id"));
        assertOrderId(actual.getColumnMetaData("order_id"), false);
        assertUserId(actual.getColumnMetaData("user_id"), false, false);
        assertStatus(actual.getColumnMetaData("status"));
    }
    
    @Test
    @SneakyThrows(SQLException.class)
    void assertLoadCompoundPrimaryKey() {
        try (Connection connection = dataSource.getConnection()) {
            connection.createStatement().execute("CREATE TABLE t_order (order_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(50), PRIMARY KEY (order_id, user_id))");
        }
        StandardPipelineTableMetaDataLoader loader = new StandardPipelineTableMetaDataLoader(dataSource);
        PipelineTableMetaData actual = loader.getTableMetaData(null, "t_order");
        assertThat(actual.getColumnNames().size(), is(3));
        assertThat(actual.getPrimaryKeyColumns().size(), is(2));
        Collection<PipelineIndexMetaData> uniqueIndexes = actual.getUniqueIndexes();
        assertThat(uniqueIndexes.size(), is(1));
        PipelineIndexMetaData indexMetaData = uniqueIndexes.iterator().next();
        assertThat(indexMetaData.getColumns().get(0).getName(), is("order_id"));
        assertThat(indexMetaData.getColumns().get(1).getName(), is("user_id"));
        assertOrderId(actual.getColumnMetaData("order_id"), true);
        assertUserId(actual.getColumnMetaData("user_id"), true, true);
        assertStatus(actual.getColumnMetaData("status"));
    }
    
    @Test
    @SneakyThrows(SQLException.class)
    void assertLoadCompoundUniqueKey() {
        try (Connection connection = dataSource.getConnection()) {
            connection.createStatement().execute("CREATE TABLE t_order (order_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(50), UNIQUE KEY (order_id, user_id))");
        }
        StandardPipelineTableMetaDataLoader loader = new StandardPipelineTableMetaDataLoader(dataSource);
        PipelineTableMetaData actual = loader.getTableMetaData(null, "t_order");
        assertTrue(actual.getPrimaryKeyColumns().isEmpty());
        assertThat(actual.getColumnNames().size(), is(3));
        Collection<PipelineIndexMetaData> uniqueIndexes = actual.getUniqueIndexes();
        assertThat(uniqueIndexes.size(), is(1));
        PipelineIndexMetaData indexMetaData = uniqueIndexes.iterator().next();
        assertThat(indexMetaData.getColumns().get(0).getName(), is("order_id"));
        assertThat(indexMetaData.getColumns().get(1).getName(), is("user_id"));
        assertOrderId(actual.getColumnMetaData("order_id"), false);
        assertUserId(actual.getColumnMetaData("user_id"), false, true);
        assertStatus(actual.getColumnMetaData("status"));
    }
}
