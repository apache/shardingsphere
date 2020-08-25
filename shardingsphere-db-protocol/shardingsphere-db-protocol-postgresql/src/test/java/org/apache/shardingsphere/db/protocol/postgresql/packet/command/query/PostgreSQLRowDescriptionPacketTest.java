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

package org.apache.shardingsphere.db.protocol.postgresql.packet.command.query;

import org.apache.shardingsphere.db.protocol.postgresql.packet.command.PostgreSQLCommandPacketType;
import org.apache.shardingsphere.db.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.Types;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class PostgreSQLRowDescriptionPacketTest {
    
    @Mock
    private PostgreSQLPacketPayload payload;
    
    @Test
    public void assertWrite() {
        PostgreSQLColumnDescription description = new PostgreSQLColumnDescription("name", 1, Types.VARCHAR, 4, null);
        PostgreSQLRowDescriptionPacket packet = new PostgreSQLRowDescriptionPacket(1, Arrays.asList(description));
        packet.write(payload);
        verify(payload, times(2)).writeInt2(1);
        verify(payload).writeStringNul("name");
        verify(payload).writeInt4(0);
        verify(payload, times(2)).writeInt2(1);
        verify(payload).writeInt4(1043);
        verify(payload).writeInt2(4);
        verify(payload).writeInt4(-1);
        verify(payload).writeInt2(0);
    }
    
    @Test
    public void getMessageType() {
        PostgreSQLRowDescriptionPacket packet = new PostgreSQLRowDescriptionPacket(0, Arrays.asList());
        assertThat(packet.getMessageType(), is(PostgreSQLCommandPacketType.ROW_DESCRIPTION.getValue()));
    }
}
