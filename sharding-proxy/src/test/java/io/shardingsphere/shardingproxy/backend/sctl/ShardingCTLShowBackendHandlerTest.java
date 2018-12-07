/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.shardingproxy.backend.sctl;

import io.shardingsphere.core.constant.transaction.TransactionType;
import io.shardingsphere.shardingproxy.backend.jdbc.connection.BackendConnection;
import io.shardingsphere.shardingproxy.transport.mysql.packet.command.CommandResponsePackets;
import io.shardingsphere.shardingproxy.transport.mysql.packet.generic.ErrPacket;
import io.shardingsphere.shardingproxy.transport.mysql.packet.generic.OKPacket;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ShardingCTLShowBackendHandlerTest {
    
    private BackendConnection backendConnection = new BackendConnection(TransactionType.LOCAL);
    
    @Test
    public void assertShowTransactionType() {
        backendConnection.setCurrentSchema("schema");
        ShardingCTLShowBackendHandler backendHandler = new ShardingCTLShowBackendHandler("sctl:show transaction_type", backendConnection);
        CommandResponsePackets actual = backendHandler.execute();
        assertThat(actual.getHeadPacket(), instanceOf(OKPacket.class));
        OKPacket okPacket = (OKPacket) actual.getHeadPacket();
        assertThat(okPacket.getInfo(), is(" current transaction type is: LOCAL"));
    }
    
    @Test
    public void assertShowCachedConnections() {
        backendConnection.setCurrentSchema("schema");
        ShardingCTLShowBackendHandler backendHandler = new ShardingCTLShowBackendHandler("sctl:show cached_connections", backendConnection);
        CommandResponsePackets actual = backendHandler.execute();
        assertThat(actual.getHeadPacket(), instanceOf(OKPacket.class));
        OKPacket okPacket = (OKPacket) actual.getHeadPacket();
        assertThat(okPacket.getInfo(), is(" current channel cached connection size is: 0"));
    }
    
    @Test
    public void assertShowCachedConnectionFailed() {
        backendConnection.setCurrentSchema("schema");
        ShardingCTLShowBackendHandler backendHandler = new ShardingCTLShowBackendHandler("sctl:show cached_connectionss", backendConnection);
        CommandResponsePackets actual = backendHandler.execute();
        assertThat(actual.getHeadPacket(), instanceOf(ErrPacket.class));
        ErrPacket errPacket = (ErrPacket) actual.getHeadPacket();
        assertThat(errPacket.getErrorMessage(), containsString(" could not support this sctl grammar "));
    }
    
    @Test
    public void assertShowCTLFormatError() {
        backendConnection.setCurrentSchema("schema");
        ShardingCTLShowBackendHandler backendHandler = new ShardingCTLShowBackendHandler("sctl:show=xx", backendConnection);
        CommandResponsePackets actual = backendHandler.execute();
        assertThat(actual.getHeadPacket(), instanceOf(ErrPacket.class));
        ErrPacket errPacket = (ErrPacket) actual.getHeadPacket();
        assertThat(errPacket.getErrorMessage(), containsString(" please review your sctl format"));
    }
}
