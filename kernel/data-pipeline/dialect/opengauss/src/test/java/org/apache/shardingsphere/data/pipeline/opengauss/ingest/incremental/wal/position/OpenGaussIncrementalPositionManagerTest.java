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

package org.apache.shardingsphere.data.pipeline.opengauss.ingest.incremental.wal.position;

import org.apache.shardingsphere.data.pipeline.core.ingest.position.DialectIncrementalPositionManager;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.incremental.wal.WALPosition;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.incremental.wal.position.slot.PostgreSQLSlotManager;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengauss.replication.LogSequenceNumber;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OpenGaussIncrementalPositionManagerTest {
    
    private static final String LSN = "0/14EFDB8";
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "openGauss");
    
    private final DialectIncrementalPositionManager incrementalPositionManager = DatabaseTypedSPILoader.getService(DialectIncrementalPositionManager.class, databaseType);
    
    private DataSource dataSource;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Connection connection;
    
    @Mock
    private PostgreSQLSlotManager slotManager;
    
    @BeforeEach
    void setUp() throws ReflectiveOperationException {
        dataSource = new MockedDataSource(connection);
        Plugins.getMemberAccessor().set(OpenGaussIncrementalPositionManager.class.getDeclaredField("slotManager"), incrementalPositionManager, slotManager);
    }
    
    @Test
    void assertInitWithData() {
        WALPosition actual = (WALPosition) incrementalPositionManager.init(LSN);
        assertThat(actual.getLogSequenceNumber().toString(), is("OpenGaussLogSequenceNumber(logSequenceNumber=LSN{0/14EFDB8})"));
    }
    
    @Test
    void assertInitWithDataSource() throws SQLException {
        PreparedStatement preparedStatement = mockPreparedStatement();
        when(connection.prepareStatement("SELECT PG_CURRENT_XLOG_LOCATION()")).thenReturn(preparedStatement);
        WALPosition actual = (WALPosition) incrementalPositionManager.init(dataSource, "");
        assertThat(actual.getLogSequenceNumber().get(), is(LogSequenceNumber.valueOf(LSN)));
        verify(slotManager).create(connection, "");
    }
    
    private PreparedStatement mockPreparedStatement() throws SQLException {
        PreparedStatement result = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);
        when(result.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString(1)).thenReturn(LSN);
        return result;
    }
    
    @Test
    void assertDestroy() throws SQLException {
        incrementalPositionManager.destroy(dataSource, "");
        verify(slotManager).dropIfExisted(connection, "");
    }
}
