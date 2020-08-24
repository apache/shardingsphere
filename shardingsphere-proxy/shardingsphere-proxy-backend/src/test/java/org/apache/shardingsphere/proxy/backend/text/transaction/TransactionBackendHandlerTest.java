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

package org.apache.shardingsphere.proxy.backend.text.transaction;

import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.response.BackendResponse;
import org.apache.shardingsphere.proxy.backend.response.update.UpdateResponse;
import org.apache.shardingsphere.transaction.core.TransactionOperationType;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public final class TransactionBackendHandlerTest {
    
    private final BackendConnection backendConnection = new BackendConnection(TransactionType.LOCAL);
    
    @Test
    public void assertExecute() {
        TransactionBackendHandler transactionBackendHandler = new TransactionBackendHandler(TransactionOperationType.BEGIN, backendConnection);
        BackendResponse actual = transactionBackendHandler.execute();
        assertThat(actual, instanceOf(UpdateResponse.class));
    }
}
