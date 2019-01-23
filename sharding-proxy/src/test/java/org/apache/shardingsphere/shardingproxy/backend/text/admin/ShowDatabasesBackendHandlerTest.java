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

package org.apache.shardingsphere.shardingproxy.backend.text.admin;

import org.apache.shardingsphere.shardingproxy.backend.MockGlobalRegistryUtil;
import org.apache.shardingsphere.shardingproxy.backend.ResultPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.constant.ColumnType;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.CommandResponsePackets;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.QueryResponsePackets;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public final class ShowDatabasesBackendHandlerTest {
    
    private ShowDatabasesBackendHandler showDatabasesBackendHandler = new ShowDatabasesBackendHandler();
    
    @Before
    public void setUp() {
        MockGlobalRegistryUtil.setLogicSchemas("schema", 5);
    }
    
    @Test
    public void assertExecuteShowDatabaseBackendHandler() {
        CommandResponsePackets actual = showDatabasesBackendHandler.execute();
        assertThat(actual, instanceOf(QueryResponsePackets.class));
        QueryResponsePackets responsePackets = (QueryResponsePackets) actual;
        assertThat(responsePackets.getColumnCount(), is(1));
        assertThat(responsePackets.getColumnDefinition41Packets().size(), is(1));
        assertThat(responsePackets.getColumnTypes().size(), is(1));
        assertThat(responsePackets.getColumnTypes().iterator().next(), is(ColumnType.MYSQL_TYPE_VARCHAR));
        assertThat(responsePackets.getColumnTypes().iterator().next(), is(ColumnType.MYSQL_TYPE_VARCHAR));
    }
    
    @Test
    public void assertShowDatabaseUsingStream() throws SQLException {
        showDatabasesBackendHandler.execute();
        int sequenceId = 4;
        while (showDatabasesBackendHandler.next()) {
            ResultPacket resultPacket = showDatabasesBackendHandler.getResultValue();
            assertThat(resultPacket.getColumnCount(), is(1));
            assertThat(resultPacket.getColumnTypes().size(), is(1));
            assertThat(resultPacket.getColumnTypes().iterator().next(), is(ColumnType.MYSQL_TYPE_VARCHAR));
            assertThat(resultPacket.getSequenceId(), is(sequenceId));
            assertThat(resultPacket.getData().size(), is(1));
            ++sequenceId;
        }
    }
}
