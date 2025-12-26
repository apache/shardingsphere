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

package org.apache.shardingsphere.proxy.frontend.mysql.command.admin.ping;

import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLStatusFlag;
import org.apache.shardingsphere.database.protocol.mysql.packet.generic.MySQLOKPacket;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.session.transaction.TransactionStatus;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MySQLComPingExecutorTest {
    
    @Test
    void assertExecute() {
        MySQLComPingExecutor executor = new MySQLComPingExecutor(mockConnectionSession());
        Collection<DatabasePacket> actual = executor.execute();
        assertThat(actual.size(), is(1));
        DatabasePacket packet = actual.iterator().next();
        assertThat(((MySQLOKPacket) packet).getStatusFlag(), is(MySQLStatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue()));
    }
    
    private static ConnectionSession mockConnectionSession() {
        ConnectionSession result = mock(ConnectionSession.class);
        when(result.isAutoCommit()).thenReturn(true);
        when(result.getTransactionStatus()).thenReturn(new TransactionStatus());
        return result;
    }
}
