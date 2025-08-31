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

package org.apache.shardingsphere.proxy.frontend.postgresql.command.query.extended;

import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.PostgreSQLPacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.PostgreSQLAggregatedResponsesPacket;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PostgreSQLAggregatedCommandExecutorTest {
    
    @Test
    void assertExecute() throws SQLException {
        int commandCount = 16;
        List<CommandExecutor> executors = new ArrayList<>(commandCount);
        for (int i = 0; i < commandCount; i++) {
            CommandExecutor executor = mock(CommandExecutor.class);
            when(executor.execute()).thenReturn(Collections.singleton(mock(PostgreSQLPacket.class)));
            executors.add(executor);
        }
        PostgreSQLAggregatedCommandExecutor actualExecutor = new PostgreSQLAggregatedCommandExecutor(executors);
        Collection<DatabasePacket> actualPackets = actualExecutor.execute();
        assertThat(actualPackets.size(), is(1));
        assertThat(actualPackets.iterator().next(), isA(PostgreSQLAggregatedResponsesPacket.class));
    }
}
