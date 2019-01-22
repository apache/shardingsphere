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

package org.apache.shardingsphere.shardingproxy.backend.sctl;

import org.apache.shardingsphere.shardingproxy.backend.engine.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.CommandResponsePackets;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.generic.ErrPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.generic.OKPacket;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class ShardingCTLSetBackendHandlerTest {
    
    private BackendConnection backendConnection = new BackendConnection(TransactionType.LOCAL);
    
    @Test
    public void assertSwitchTransactionTypeXA() {
        backendConnection.setCurrentSchema("schema");
        ShardingCTLSetBackendHandler shardingCTLBackendHandler = new ShardingCTLSetBackendHandler("sctl:set transaction_type=XA", backendConnection);
        CommandResponsePackets actual = shardingCTLBackendHandler.execute();
        assertThat(actual.getHeadPacket(), instanceOf(OKPacket.class));
        assertThat(backendConnection.getTransactionType(), is(TransactionType.XA));
    }
    
    @Test
    public void assertSwitchTransactionTypeBASE() {
        backendConnection.setCurrentSchema("schema");
        ShardingCTLSetBackendHandler shardingCTLBackendHandler = new ShardingCTLSetBackendHandler("sctl:set  transaction_type=BASE", backendConnection);
        CommandResponsePackets actual = shardingCTLBackendHandler.execute();
        assertThat(actual.getHeadPacket(), instanceOf(OKPacket.class));
        assertThat(backendConnection.getTransactionType(), is(TransactionType.BASE));
    }
    
    @Test
    public void assertSwitchTransactionTypeLOCAL() {
        backendConnection.setCurrentSchema("schema");
        ShardingCTLSetBackendHandler shardingCTLBackendHandler = new ShardingCTLSetBackendHandler("sctl:set transaction_type=LOCAL", backendConnection);
        CommandResponsePackets actual = shardingCTLBackendHandler.execute();
        assertThat(actual.getHeadPacket(), instanceOf(OKPacket.class));
        assertThat(backendConnection.getTransactionType(), is(TransactionType.LOCAL));
    }
    
    @Test
    public void assertSwitchTransactionTypeFailed() {
        backendConnection.setCurrentSchema("schema");
        ShardingCTLSetBackendHandler shardingCTLBackendHandler = new ShardingCTLSetBackendHandler("sctl:set transaction_type=XXX", backendConnection);
        CommandResponsePackets actual = shardingCTLBackendHandler.execute();
        assertThat(actual.getHeadPacket(), instanceOf(ErrPacket.class));
        assertThat(backendConnection.getTransactionType(), is(TransactionType.LOCAL));
    }
    
    @Test
    public void assertNotSupportedSCTL() {
        ShardingCTLSetBackendHandler shardingCTLBackendHandler = new ShardingCTLSetBackendHandler("sctl:set @@session=XXX", backendConnection);
        CommandResponsePackets actual = shardingCTLBackendHandler.execute();
        assertThat(actual.getHeadPacket(), instanceOf(ErrPacket.class));
    }
    
    @Test
    public void assertFormatErrorSCTL() {
        ShardingCTLSetBackendHandler shardingCTLBackendHandler = new ShardingCTLSetBackendHandler("sctl:set yyyyy", backendConnection);
        CommandResponsePackets actual = shardingCTLBackendHandler.execute();
        assertThat(actual.getHeadPacket(), instanceOf(ErrPacket.class));
    }
}
