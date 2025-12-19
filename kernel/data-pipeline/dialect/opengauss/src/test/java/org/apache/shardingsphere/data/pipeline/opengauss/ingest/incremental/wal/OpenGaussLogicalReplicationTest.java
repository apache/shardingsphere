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

package org.apache.shardingsphere.data.pipeline.opengauss.ingest.incremental.wal;

import org.apache.shardingsphere.data.pipeline.api.type.StandardPipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.opengauss.ingest.incremental.wal.decode.OpenGaussLogSequenceNumber;
import org.apache.shardingsphere.database.connector.core.jdbcurl.parser.ConnectionProperties;
import org.apache.shardingsphere.database.connector.core.jdbcurl.parser.ConnectionPropertiesParser;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengauss.PGProperty;
import org.opengauss.jdbc.PgConnection;
import org.opengauss.replication.LogSequenceNumber;
import org.opengauss.replication.PGReplicationStream;
import org.opengauss.replication.fluent.logical.ChainedLogicalStreamBuilder;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OpenGaussLogicalReplicationTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private PgConnection connection;
    
    @Mock
    private ChainedLogicalStreamBuilder chainedLogicalStreamBuilder;
    
    @Mock
    private PGReplicationStream replicationStream;
    
    @SuppressWarnings("JDBCResourceOpenedButNotSafelyClosed")
    @Test
    void assertCreateConnectionRetriesWithHAPort() throws SQLException {
        Map<String, Object> poolProps = new HashMap<>(3, 1F);
        poolProps.put("url", "jdbc:opengauss://127.0.0.1:5432/foo_db");
        poolProps.put("username", "root");
        poolProps.put("password", "root");
        StandardPipelineDataSourceConfiguration sourceConfig = new StandardPipelineDataSourceConfiguration(poolProps);
        Connection expectedConnection = mock(Connection.class);
        ConnectionPropertiesParser parser = mock(ConnectionPropertiesParser.class);
        ConnectionProperties connectionProps = new ConnectionProperties("127.0.0.1", 5432, "foo_db", null, new Properties());
        AtomicReference<Properties> actualProps = new AtomicReference<>();
        AtomicInteger callTimes = new AtomicInteger();
        try (
                MockedStatic<DriverManager> driverManagerMock = mockStatic(DriverManager.class);
                MockedStatic<DatabaseTypedSPILoader> databaseTypedSPILoaderMock = mockStatic(DatabaseTypedSPILoader.class)) {
            databaseTypedSPILoaderMock.when(() -> DatabaseTypedSPILoader.getService(eq(ConnectionPropertiesParser.class), any(DatabaseType.class))).thenReturn(parser);
            when(parser.parse(anyString(), isNull(), isNull())).thenReturn(connectionProps);
            driverManagerMock.when(() -> DriverManager.getConnection(anyString(), any(Properties.class)))
                    .thenAnswer(invocation -> {
                        if (0 == callTimes.getAndIncrement()) {
                            throw new SQLException("HA port not open");
                        }
                        actualProps.set(invocation.getArgument(1));
                        return expectedConnection;
                    });
            Connection actualConnection = new OpenGaussLogicalReplication().createConnection(sourceConfig);
            assertThat(actualConnection, is(expectedConnection));
            Properties props = actualProps.get();
            assertThat(PGProperty.PG_HOST.get(props), is("127.0.0.1"));
            assertThat(PGProperty.PG_DBNAME.get(props), is("foo_db"));
            assertThat(Integer.parseInt(String.valueOf(PGProperty.PG_PORT.get(props))), is(5433));
        }
    }
    
    @SuppressWarnings({"JDBCResourceOpenedButNotSafelyClosed", "resource"})
    @Test
    void assertCreateConnectionThrowsWhenNonHAPort() {
        Map<String, Object> poolProps = new HashMap<>(3, 1F);
        poolProps.put("url", "jdbc:opengauss://localhost:5432/test_db");
        poolProps.put("username", "root");
        poolProps.put("password", "root");
        StandardPipelineDataSourceConfiguration config = new StandardPipelineDataSourceConfiguration(poolProps);
        SQLException expectedException = new SQLException("auth failed");
        try (MockedStatic<DriverManager> driverManagerMock = mockStatic(DriverManager.class)) {
            driverManagerMock.when(() -> DriverManager.getConnection(anyString(), any(Properties.class))).thenThrow(expectedException);
            SQLException actualException = assertThrows(SQLException.class, () -> new OpenGaussLogicalReplication().createConnection(config));
            assertThat(actualException, is(expectedException));
            driverManagerMock.verify(() -> DriverManager.getConnection(anyString(), any(Properties.class)), times(1));
        }
    }
    
    @Test
    void assertCreateReplicationStreamWhenMajorVersionLessThanThree() throws SQLException {
        when(connection.getReplicationAPI().replicationStream().logical()).thenReturn(chainedLogicalStreamBuilder);
        when(chainedLogicalStreamBuilder.withSlotName("slot")).thenReturn(chainedLogicalStreamBuilder);
        when(chainedLogicalStreamBuilder.withSlotOption("include-xids", true)).thenReturn(chainedLogicalStreamBuilder);
        when(chainedLogicalStreamBuilder.withSlotOption("skip-empty-xacts", true)).thenReturn(chainedLogicalStreamBuilder);
        LogSequenceNumber startPosition = LogSequenceNumber.valueOf(100L);
        when(chainedLogicalStreamBuilder.withStartPosition(startPosition)).thenReturn(chainedLogicalStreamBuilder);
        when(chainedLogicalStreamBuilder.start()).thenReturn(replicationStream);
        OpenGaussLogSequenceNumber basePosition = new OpenGaussLogSequenceNumber(startPosition);
        PGReplicationStream actualStream = new OpenGaussLogicalReplication().createReplicationStream(connection, basePosition, "slot", 2);
        assertThat(actualStream, is(replicationStream));
        verify(chainedLogicalStreamBuilder).start();
    }
    
    @Test
    void assertCreateReplicationStreamWhenMajorVersionAtLeastThree() throws SQLException {
        when(connection.getReplicationAPI().replicationStream().logical()).thenReturn(chainedLogicalStreamBuilder);
        when(chainedLogicalStreamBuilder.withSlotName("slot")).thenReturn(chainedLogicalStreamBuilder);
        when(chainedLogicalStreamBuilder.withSlotOption("include-xids", true)).thenReturn(chainedLogicalStreamBuilder);
        when(chainedLogicalStreamBuilder.withSlotOption("skip-empty-xacts", true)).thenReturn(chainedLogicalStreamBuilder);
        when(chainedLogicalStreamBuilder.withSlotOption("parallel-decode-num", 10)).thenReturn(chainedLogicalStreamBuilder);
        when(chainedLogicalStreamBuilder.withSlotOption("decode-style", "j")).thenReturn(chainedLogicalStreamBuilder);
        when(chainedLogicalStreamBuilder.withSlotOption("sending-batch", 0)).thenReturn(chainedLogicalStreamBuilder);
        LogSequenceNumber startPosition = LogSequenceNumber.valueOf(200L);
        when(chainedLogicalStreamBuilder.withStartPosition(startPosition)).thenReturn(chainedLogicalStreamBuilder);
        when(chainedLogicalStreamBuilder.start()).thenReturn(replicationStream);
        OpenGaussLogSequenceNumber basePosition = new OpenGaussLogSequenceNumber(startPosition);
        PGReplicationStream actualStream = new OpenGaussLogicalReplication().createReplicationStream(connection, basePosition, "slot", 3);
        assertThat(actualStream, is(replicationStream));
    }
}
