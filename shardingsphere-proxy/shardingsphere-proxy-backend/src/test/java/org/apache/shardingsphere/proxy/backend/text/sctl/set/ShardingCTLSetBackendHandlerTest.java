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

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.auth.Authentication;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.context.schema.SchemaContext;
import org.apache.shardingsphere.infra.context.schema.impl.StandardSchemaContexts;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorKernel;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.BackendResponse;
import org.apache.shardingsphere.proxy.backend.response.update.UpdateResponse;
import org.apache.shardingsphere.proxy.backend.text.sctl.exception.InvalidShardingCTLFormatException;
import org.apache.shardingsphere.proxy.backend.text.sctl.exception.UnsupportedShardingCTLTypeException;
import org.apache.shardingsphere.rdl.parser.engine.ShardingSphereSQLParserEngine;
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
    @SneakyThrows(ReflectiveOperationException.class)
    public void setUp() {
        Field schemaContexts = ProxyContext.getInstance().getClass().getDeclaredField("schemaContexts");
        schemaContexts.setAccessible(true);
        schemaContexts.set(ProxyContext.getInstance(), new StandardSchemaContexts(getSchemaContextMap(), 
                mock(ShardingSphereSQLParserEngine.class), mock(ExecutorKernel.class), new Authentication(), new ConfigurationProperties(new Properties()), new MySQLDatabaseType()));
    }
    
    private Map<String, SchemaContext> getSchemaContextMap() {
        Map<String, SchemaContext> result = new HashMap<>(10, 1);
        for (int i = 0; i < 10; i++) {
            result.put(String.format(SCHEMA_PATTERN, i), mock(SchemaContext.class));
        }
        return result;
    }
    
    @Test
    public void assertSwitchTransactionTypeXA() {
        backendConnection.setCurrentSchema(String.format(SCHEMA_PATTERN, 0));
        ShardingCTLSetBackendHandler shardingCTLBackendHandler = new ShardingCTLSetBackendHandler("sctl:set transaction_type=XA", backendConnection);
        BackendResponse actual = shardingCTLBackendHandler.execute();
        assertThat(actual, instanceOf(UpdateResponse.class));
        assertThat(backendConnection.getTransactionStatus().getTransactionType(), is(TransactionType.XA));
    }
    
    @Test
    public void assertSwitchTransactionTypeBASE() {
        backendConnection.setCurrentSchema(String.format(SCHEMA_PATTERN, 0));
        ShardingCTLSetBackendHandler shardingCTLBackendHandler = new ShardingCTLSetBackendHandler("sctl:set  transaction_type=BASE", backendConnection);
        BackendResponse actual = shardingCTLBackendHandler.execute();
        assertThat(actual, instanceOf(UpdateResponse.class));
        assertThat(backendConnection.getTransactionStatus().getTransactionType(), is(TransactionType.BASE));
    }
    
    @Test
    public void assertSwitchTransactionTypeLOCAL() {
        backendConnection.setCurrentSchema(String.format(SCHEMA_PATTERN, 0));
        ShardingCTLSetBackendHandler shardingCTLBackendHandler = new ShardingCTLSetBackendHandler("sctl:set transaction_type=LOCAL", backendConnection);
        BackendResponse actual = shardingCTLBackendHandler.execute();
        assertThat(actual, instanceOf(UpdateResponse.class));
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
