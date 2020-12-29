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

package org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.binary.parse;

import org.apache.shardingsphere.db.protocol.postgresql.constant.PostgreSQLBinaryColumnType;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.binary.PostgreSQLBinaryStatementParameterType;
import org.apache.shardingsphere.db.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class PostgreSQLComParsePacketTest {
    
    @Mock
    private PostgreSQLPacketPayload payload;
    
    @Test
    public void assertNewInstance() {
        when(payload.readInt2()).thenReturn(1);
        when(payload.readInt4()).thenReturn(0);
        when(payload.readStringNul()).thenReturn("sql");
        PostgreSQLComParsePacket actual = new PostgreSQLComParsePacket(payload);
        actual.write(payload);
        assertThat(actual.getMessageType(), is('P'));
        assertThat(actual.getSql(), is("sql"));
        assertThat(actual.getStatementId(), is("sql"));
        List<PostgreSQLBinaryStatementParameterType> types = actual.getBinaryStatementParameterTypes();
        assertNotNull(types);
        assertThat(types.size(), equalTo(1));
        assertThat(types.get(0).getColumnType(), is(PostgreSQLBinaryColumnType.POSTGRESQL_TYPE_UNSPECIFIED));
        assertTrue(actual.toString().startsWith("PostgreSQLComParsePacket(statementId=sql, sql=sql, binaryStatementParameterTypes=["));
    }
}
