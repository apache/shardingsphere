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

package org.apache.shardingsphere.data.pipeline.mysql.ingest.incremental.binlog.position;

import org.apache.shardingsphere.data.pipeline.core.ingest.position.DialectIncrementalPositionManager;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDataSource;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MySQLIncrementalPositionManagerTest {
    
    private static final String MYSQL_DATABASE_PRODUCT_NAME = "MySQL";
    
    private static final String MARIADB_DATABASE_PRODUCT_NAME = "MariaDB";
    
    private static final String SHOW_MASTER_STATUS_SQL = "SHOW MASTER STATUS";
    
    private static final String SHOW_BINARY_LOG_STATUS_SQL = "SHOW BINARY LOG STATUS";
    
    private static final String LOG_FILE_NAME = "binlog-000001";
    
    private static final long LOG_POSITION = 4L;
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
    
    private final DialectIncrementalPositionManager incrementalPositionManager = DatabaseTypedSPILoader.getService(DialectIncrementalPositionManager.class, databaseType);
    
    @Test
    void assertInitWithData() {
        MySQLBinlogPosition actual = (MySQLBinlogPosition) incrementalPositionManager.init("binlog-000001#4");
        assertThat(actual.getFilename(), is(LOG_FILE_NAME));
        assertThat(actual.getPosition(), is(LOG_POSITION));
    }
    
    @Test
    void assertInitWithDataFailed() {
        assertThrows(IllegalArgumentException.class, () -> incrementalPositionManager.init("binlog-000001"));
    }
    
    @Test
    void assertInitWithDataSourceByShowMasterStatus() throws SQLException {
        assertInitWithDataSource(MYSQL_DATABASE_PRODUCT_NAME, 8, 3, SHOW_MASTER_STATUS_SQL);
    }
    
    @Test
    void assertInitWithDataSourceByShowBinaryLogStatus() throws SQLException {
        assertInitWithDataSource(MYSQL_DATABASE_PRODUCT_NAME, 8, 4, SHOW_BINARY_LOG_STATUS_SQL);
    }
    
    @Test
    void assertInitWithDataSourceByShowBinaryLogStatusForHigherMajorVersion() throws SQLException {
        assertInitWithDataSource(MYSQL_DATABASE_PRODUCT_NAME, 9, 0, SHOW_BINARY_LOG_STATUS_SQL);
    }
    
    @Test
    void assertInitWithDataSourceByShowMasterStatusForMariaDB() throws SQLException {
        assertInitWithDataSource(MARIADB_DATABASE_PRODUCT_NAME, 11, 4, SHOW_MASTER_STATUS_SQL);
    }
    
    private void assertInitWithDataSource(final String productName, final int majorVersion, final int minorVersion, final String expectedStatusSQL) throws SQLException {
        Connection connection = mock(Connection.class);
        MySQLBinlogPosition actual = (MySQLBinlogPosition) incrementalPositionManager.init(createDataSource(connection, productName, majorVersion, minorVersion, expectedStatusSQL), "");
        assertThat(actual.getFilename(), is(LOG_FILE_NAME));
        assertThat(actual.getPosition(), is(LOG_POSITION));
        verify(connection).prepareStatement(expectedStatusSQL);
    }
    
    private DataSource createDataSource(final Connection connection, final String productName, final int majorVersion, final int minorVersion, final String expectedStatusSQL) throws SQLException {
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        when(databaseMetaData.getDatabaseProductName()).thenReturn(productName);
        when(databaseMetaData.getDatabaseMajorVersion()).thenReturn(majorVersion);
        when(databaseMetaData.getDatabaseMinorVersion()).thenReturn(minorVersion);
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        PreparedStatement positionStatement = mockPositionStatement();
        when(connection.prepareStatement(expectedStatusSQL)).thenReturn(positionStatement);
        return new MockedDataSource(connection);
    }
    
    private PreparedStatement mockPositionStatement() throws SQLException {
        PreparedStatement result = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);
        when(result.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString(1)).thenReturn(LOG_FILE_NAME);
        when(resultSet.getLong(2)).thenReturn(LOG_POSITION);
        return result;
    }
}
