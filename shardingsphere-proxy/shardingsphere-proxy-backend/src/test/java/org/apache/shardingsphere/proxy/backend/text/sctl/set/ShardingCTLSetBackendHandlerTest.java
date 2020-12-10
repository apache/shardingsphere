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

import org.apache.shardingsphere.infra.auth.memory.MemoryAuthentication;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.context.metadata.impl.StandardMetaDataContexts;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorEngine;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.text.sctl.exception.InvalidShardingCTLFormatException;
import org.apache.shardingsphere.proxy.backend.text.sctl.exception.UnsupportedShardingCTLTypeException;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public final class ShardingCTLSetBackendHandlerTest {
    
    private static final String SCHEMA_PATTERN = "schema_%s";
    
    private final BackendConnection backendConnection = new BackendConnection(TransactionType.LOCAL);
    
    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        Field metaDataContexts = ProxyContext.getInstance().getClass().getDeclaredField("metaDataContexts");
        metaDataContexts.setAccessible(true);
        metaDataContexts.set(ProxyContext.getInstance(), 
                new StandardMetaDataContexts(getMetaDataMap(), mock(ExecutorEngine.class), new MemoryAuthentication(), new ConfigurationProperties(new Properties()), new MySQLDatabaseType()));
    }
    
    private Map<String, ShardingSphereMetaData> getMetaDataMap() {
        Map<String, ShardingSphereMetaData> result = new HashMap<>(10, 1);
        for (int i = 0; i < 10; i++) {
            result.put(String.format(SCHEMA_PATTERN, i), mock(ShardingSphereMetaData.class));
        }
        return result;
    }
    
    @Test
    public void assertSwitchTransactionTypeXA() {
        backendConnection.setCurrentSchema(String.format(SCHEMA_PATTERN, 0));
        ShardingCTLSetBackendHandler shardingCTLBackendHandler = new ShardingCTLSetBackendHandler("sctl:set transaction_type=XA", backendConnection);
        ResponseHeader actual = shardingCTLBackendHandler.execute();
        assertThat(actual, instanceOf(UpdateResponseHeader.class));
        assertThat(backendConnection.getTransactionStatus().getTransactionType(), is(TransactionType.XA));
    }
    
    @Test
    public void assertSwitchTransactionTypeBASE() {
        backendConnection.setCurrentSchema(String.format(SCHEMA_PATTERN, 0));
        ShardingCTLSetBackendHandler shardingCTLBackendHandler = new ShardingCTLSetBackendHandler("sctl:set  transaction_type=BASE", backendConnection);
        ResponseHeader actual = shardingCTLBackendHandler.execute();
        assertThat(actual, instanceOf(UpdateResponseHeader.class));
        assertThat(backendConnection.getTransactionStatus().getTransactionType(), is(TransactionType.BASE));
    }
    
    @Test
    public void assertSwitchTransactionTypeLOCAL() {
        backendConnection.setCurrentSchema(String.format(SCHEMA_PATTERN, 0));
        ShardingCTLSetBackendHandler shardingCTLBackendHandler = new ShardingCTLSetBackendHandler("sctl:set transaction_type=LOCAL", backendConnection);
        ResponseHeader actual = shardingCTLBackendHandler.execute();
        assertThat(actual, instanceOf(UpdateResponseHeader.class));
        assertThat(backendConnection.getTransactionStatus().getTransactionType(), is(TransactionType.LOCAL));
    }
    
    @Test(expected = UnsupportedShardingCTLTypeException.class)
    public void assertSwitchTransactionTypeFailed() {
        backendConnection.setCurrentSchema(String.format(SCHEMA_PATTERN, 0));
        new ShardingCTLSetBackendHandler("sctl:set transaction_type=XXX", backendConnection).execute();
    }
    
    @Test(expected = UnsupportedShardingCTLTypeException.class)
    public void assertNotSupportedSCTL() {
        new ShardingCTLSetBackendHandler("sctl:set @@session=XXX", backendConnection).execute();
    }
    
    @Test(expected = InvalidShardingCTLFormatException.class)
    public void assertFormatErrorSCTL() {
        new ShardingCTLSetBackendHandler("sctl:set yyyyy", backendConnection).execute();
    }
}
