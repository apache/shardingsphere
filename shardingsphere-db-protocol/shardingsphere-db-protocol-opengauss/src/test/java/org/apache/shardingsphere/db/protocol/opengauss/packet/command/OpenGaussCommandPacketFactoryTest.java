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

package org.apache.shardingsphere.db.protocol.opengauss.packet.command;

import org.apache.shardingsphere.db.protocol.opengauss.packet.command.query.binary.bind.OpenGaussComBatchBindPacket;
import org.apache.shardingsphere.db.protocol.packet.CommandPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.PostgreSQLCommandPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.PostgreSQLCommandPacketType;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.binary.PostgreSQLBinaryStatementRegistry;
import org.apache.shardingsphere.db.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class OpenGaussCommandPacketFactoryTest {
    
    @Mock
    private PostgreSQLPacketPayload payload;
    
    @Test
    public void assertNewOpenGaussComBatchBindPacket() {
        PostgreSQLBinaryStatementRegistry.getInstance().register(1);
        PostgreSQLBinaryStatementRegistry.getInstance().register(1, "assertNewOpenGaussComBatchBindPacket", "", mock(SQLStatement.class), Collections.emptyList());
        when(payload.readStringNul()).thenReturn("assertNewOpenGaussComBatchBindPacket");
        CommandPacket actual = OpenGaussCommandPacketFactory.newInstance(OpenGaussCommandPacketType.BATCH_BIND_COMMAND, payload, 1);
        assertThat(actual, instanceOf(OpenGaussComBatchBindPacket.class));
    }
    
    @Test
    public void assertNewPostgreSQLPacket() {
        CommandPacket actual = OpenGaussCommandPacketFactory.newInstance(mock(PostgreSQLCommandPacketType.class), payload, 1);
        assertTrue(actual instanceof PostgreSQLCommandPacket);
        assertFalse(actual instanceof OpenGaussCommandPacket);
    }
}
