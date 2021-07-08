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

package org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.binary.bind;

import org.apache.shardingsphere.db.protocol.postgresql.constant.PostgreSQLBinaryColumnType;
import org.apache.shardingsphere.db.protocol.postgresql.constant.PostgreSQLValueFormat;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.PostgreSQLCommandPacketType;
import org.apache.shardingsphere.db.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class PostgreSQLComBindPacketTest {
    
    @Mock
    private PostgreSQLPacketPayload payload;
    
    @Before
    public void init() {
        when(payload.readInt4()).thenReturn(1);
        when(payload.readStringNul()).thenReturn("");
        when(payload.readStringNul()).thenReturn("sts-id");
        when(payload.readInt2()).thenReturn(1);
    }
    
    @Test
    public void assertWrite() {
        when(payload.readInt2()).thenReturn(1);
        when(payload.readInt4()).thenReturn(1);
        PostgreSQLComBindPacket bindPacket = new PostgreSQLComBindPacket(payload);
        bindPacket.write(payload);
        bindPacket.init(Collections.singletonList(PostgreSQLBinaryColumnType.POSTGRESQL_TYPE_INT8));
        assertThat(bindPacket.getParameters().size(), is(1));
        assertThat(bindPacket.getResultFormats().size(), is(1));
        assertThat(bindPacket.getResultFormats().get(0), is(PostgreSQLValueFormat.BINARY));
    }
    
    @Test
    public void assertWriteWithEmptySql() {
        PostgreSQLComBindPacket bindPacket = new PostgreSQLComBindPacket(payload);
        bindPacket.write(payload);
        assertThat(bindPacket.getParameters().size(), is(0));
    }
    
    @Test
    public void getMessageType() {
        PostgreSQLComBindPacket bindPacket = new PostgreSQLComBindPacket(payload);
        assertThat(bindPacket.getIdentifier(), is(PostgreSQLCommandPacketType.BIND_COMMAND));
    }
}
