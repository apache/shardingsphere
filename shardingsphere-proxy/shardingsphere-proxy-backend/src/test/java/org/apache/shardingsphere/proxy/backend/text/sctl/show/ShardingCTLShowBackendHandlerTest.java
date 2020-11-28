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

package org.apache.shardingsphere.proxy.backend.text.sctl.show;

import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.response.BackendResponse;
import org.apache.shardingsphere.proxy.backend.response.query.QueryResponse;
import org.apache.shardingsphere.proxy.backend.text.sctl.exception.InvalidShardingCTLFormatException;
import org.apache.shardingsphere.proxy.backend.text.sctl.exception.UnsupportedShardingCTLTypeException;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class ShardingCTLShowBackendHandlerTest {
    
    private final BackendConnection backendConnection = new BackendConnection(TransactionType.LOCAL);
    
    @Test
    public void assertShowTransactionType() throws SQLException {
        backendConnection.setCurrentSchema("schema");
        ShardingCTLShowBackendHandler backendHandler = new ShardingCTLShowBackendHandler("sctl:show transaction_type", backendConnection);
        BackendResponse actual = backendHandler.execute();
        assertThat(actual, instanceOf(QueryResponse.class));
        assertThat(((QueryResponse) actual).getQueryHeaders().size(), is(1));
        backendHandler.next();
        Collection<Object> rowData = backendHandler.getRowData();
        assertThat(rowData.iterator().next(), is("LOCAL"));
    }
    
    @Test
    public void assertShowCachedConnections() throws SQLException {
        backendConnection.setCurrentSchema("schema");
        ShardingCTLShowBackendHandler backendHandler = new ShardingCTLShowBackendHandler("sctl:show cached_connections", backendConnection);
        BackendResponse actual = backendHandler.execute();
        assertThat(actual, instanceOf(QueryResponse.class));
        assertThat(((QueryResponse) actual).getQueryHeaders().size(), is(1));
        backendHandler.next();
        Collection<Object> rowData = backendHandler.getRowData();
        assertThat(rowData.iterator().next(), is(0));
    }
    
    @Test(expected = UnsupportedShardingCTLTypeException.class)
    public void assertShowCachedConnectionFailed() {
        backendConnection.setCurrentSchema("schema");
        new ShardingCTLShowBackendHandler("sctl:show cached_connectionss", backendConnection).execute();
    }
    
    @Test(expected = InvalidShardingCTLFormatException.class)
    public void assertShowCTLFormatError() {
        backendConnection.setCurrentSchema("schema");
        new ShardingCTLShowBackendHandler("sctl:show=xx", backendConnection).execute();
    }
}
