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

package org.apache.shardingsphere.proxy.backend.text.sctl.set;

import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.response.BackendResponse;
import org.apache.shardingsphere.proxy.backend.response.error.ErrorResponse;
import org.apache.shardingsphere.proxy.backend.response.update.UpdateResponse;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class ShardingCTLSetBackendHandlerTest {
    
    private final BackendConnection backendConnection = new BackendConnection(TransactionType.LOCAL);
    
    @Test
    public void assertSwitchTransactionTypeXA() {
        backendConnection.setCurrentSchema("schema");
        ShardingCTLSetBackendHandler shardingCTLBackendHandler = new ShardingCTLSetBackendHandler("sctl:set transaction_type=XA", backendConnection);
        BackendResponse actual = shardingCTLBackendHandler.execute();
        assertThat(actual, instanceOf(UpdateResponse.class));
        assertThat(backendConnection.getTransactionType(), is(TransactionType.XA));
    }
    
    @Test
    public void assertSwitchTransactionTypeBASE() {
        backendConnection.setCurrentSchema("schema");
        ShardingCTLSetBackendHandler shardingCTLBackendHandler = new ShardingCTLSetBackendHandler("sctl:set  transaction_type=BASE", backendConnection);
        BackendResponse actual = shardingCTLBackendHandler.execute();
        assertThat(actual, instanceOf(UpdateResponse.class));
        assertThat(backendConnection.getTransactionType(), is(TransactionType.BASE));
    }
    
    @Test
    public void assertSwitchTransactionTypeLOCAL() {
        backendConnection.setCurrentSchema("schema");
        ShardingCTLSetBackendHandler shardingCTLBackendHandler = new ShardingCTLSetBackendHandler("sctl:set transaction_type=LOCAL", backendConnection);
        BackendResponse actual = shardingCTLBackendHandler.execute();
        assertThat(actual, instanceOf(UpdateResponse.class));
        assertThat(backendConnection.getTransactionType(), is(TransactionType.LOCAL));
    }
    
    @Test
    public void assertSwitchTransactionTypeFailed() {
        backendConnection.setCurrentSchema("schema");
        ShardingCTLSetBackendHandler shardingCTLBackendHandler = new ShardingCTLSetBackendHandler("sctl:set transaction_type=XXX", backendConnection);
        BackendResponse actual = shardingCTLBackendHandler.execute();
        assertThat(actual, instanceOf(ErrorResponse.class));
        assertThat(backendConnection.getTransactionType(), is(TransactionType.LOCAL));
    }
    
    @Test
    public void assertNotSupportedSCTL() {
        ShardingCTLSetBackendHandler shardingCTLBackendHandler = new ShardingCTLSetBackendHandler("sctl:set @@session=XXX", backendConnection);
        BackendResponse actual = shardingCTLBackendHandler.execute();
        assertThat(actual, instanceOf(ErrorResponse.class));
    }
    
    @Test
    public void assertFormatErrorSCTL() {
        ShardingCTLSetBackendHandler shardingCTLBackendHandler = new ShardingCTLSetBackendHandler("sctl:set yyyyy", backendConnection);
        BackendResponse actual = shardingCTLBackendHandler.execute();
        assertThat(actual, instanceOf(ErrorResponse.class));
    }
}
