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

package org.apache.shardingsphere.database.protocol.postgresql.packet.command.query;

import org.apache.shardingsphere.database.connector.core.resultset.ResultSetMapper;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Types;
import java.time.OffsetDateTime;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PostgreSQLDataRowPacketResultSetMapperITTest {
    
    @Test
    void assertTimestampTzValueFromResultSetMapperIsWrittenByDataRowPacketAsText() throws Exception {
        ResultSet resultSet = mock(ResultSet.class);
        ResultSetMetaData metaData = mock(ResultSetMetaData.class);
        when(resultSet.getMetaData()).thenReturn(metaData);
        
        when(metaData.getColumnType(1)).thenReturn(Types.TIMESTAMP_WITH_TIMEZONE);
        
        OffsetDateTime expected = OffsetDateTime.parse("2020-01-01T10:20:30+08:00");
        when(resultSet.getObject(1)).thenReturn(expected);
        when(resultSet.wasNull()).thenReturn(false);
        
        DatabaseType postgreSQL = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
        Object mapped = new ResultSetMapper(postgreSQL).load(resultSet, 1);
        PostgreSQLPacketPayload payload = mock(PostgreSQLPacketPayload.class);
        when(payload.getCharset()).thenReturn(StandardCharsets.UTF_8);
        PostgreSQLDataRowPacket packet = new PostgreSQLDataRowPacket(Collections.singleton(mapped), Collections.singleton(Types.TIMESTAMP_WITH_TIMEZONE), "+05:30");
        packet.write(payload);
        
        byte[] expectedBytes = "2020-01-01 07:50:30+05:30".getBytes(StandardCharsets.UTF_8);
        
        verify(payload).writeInt2(1);
        verify(payload).writeInt4(expectedBytes.length);
        
        ArgumentCaptor<byte[]> bytesCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(payload).writeBytes(bytesCaptor.capture());
        assertThat(bytesCaptor.getValue(), is(expectedBytes));
    }
    
}
