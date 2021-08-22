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

package org.apache.shardingsphere.proxy.backend.communication.jdbc.statement.impl;

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.sql.Statement;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class MySQLStatementMemoryStrictlyFetchSizeSetterTest {
    
    private static ContextManager originContextManager;
    
    @BeforeClass
    public static void setup() {
        originContextManager = swapContextManager(mock(ContextManager.class, RETURNS_DEEP_STUBS));
    }
    
    @Test
    public void assertSetFetchSize() throws SQLException {
        when(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getProps().<Integer>getValue(ConfigurationPropertyKey.PROXY_BACKEND_QUERY_FETCH_SIZE)).thenReturn(-1);
        Statement statement = mock(Statement.class);
        new MySQLStatementMemoryStrictlyFetchSizeSetter().setFetchSize(statement);
        verify(statement).setFetchSize(Integer.MIN_VALUE);
    }
    
    @Test
    public void assertGetType() {
        assertThat(new MySQLStatementMemoryStrictlyFetchSizeSetter().getType(), is("MySQL"));
    }
    
    @AfterClass
    public static void tearDown() {
        swapContextManager(originContextManager);
    }
    
    @SneakyThrows
    private static ContextManager swapContextManager(final ContextManager newContextManager) {
        Field contextManagerField = ProxyContext.class.getDeclaredField("contextManager");
        contextManagerField.setAccessible(true);
        ContextManager result = (ContextManager) contextManagerField.get(ProxyContext.getInstance());
        contextManagerField.set(ProxyContext.getInstance(), newContextManager);
        return result;
    }
}
