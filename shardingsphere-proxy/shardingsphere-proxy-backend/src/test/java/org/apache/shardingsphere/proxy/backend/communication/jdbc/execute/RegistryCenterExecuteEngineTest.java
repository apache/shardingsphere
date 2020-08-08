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

package org.apache.shardingsphere.proxy.backend.communication.jdbc.execute;

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.kernel.context.StandardSchemaContexts;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.execute.engine.RegistryCenterExecuteEngine;
import org.apache.shardingsphere.proxy.backend.response.BackendResponse;
import org.apache.shardingsphere.proxy.backend.response.error.ErrorResponse;
import org.apache.shardingsphere.proxy.backend.response.update.UpdateResponse;
import org.apache.shardingsphere.proxy.backend.schema.ProxySchemaContexts;
import org.apache.shardingsphere.rdl.parser.binder.context.CreateDataSourcesStatementContext;
import org.apache.shardingsphere.rdl.parser.binder.context.CreateShardingRuleStatementContext;
import org.apache.shardingsphere.rdl.parser.statement.rdl.CreateDataSourcesStatement;
import org.apache.shardingsphere.rdl.parser.statement.rdl.CreateShardingRuleStatement;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class RegistryCenterExecuteEngineTest {
    
    private CreateDataSourcesStatementContext dataSourcesContext;
    
    private CreateShardingRuleStatementContext ruleContext;
    
    @Before
    public void setUp() {
        createDataSourcesContext();
        createRuleContext();
    }
    
    private void createDataSourcesContext() {
        dataSourcesContext = mock(CreateDataSourcesStatementContext.class);
        when(dataSourcesContext.getUrls()).thenReturn(Collections.emptyList());
    }
    
    private void createRuleContext() {
        ruleContext = mock(CreateShardingRuleStatementContext.class);
        when(ruleContext.getLogicTable()).thenReturn("t_order");
        when(ruleContext.getDataSources()).thenReturn(Arrays.asList("ds0", "ds1"));
        when(ruleContext.getShardingColumn()).thenReturn("order_id");
        when(ruleContext.getAlgorithmType()).thenReturn("MOD");
        Properties properties = new Properties();
        properties.setProperty("sharding.count", "2");
        when(ruleContext.getAlgorithmProperties()).thenReturn(properties);
    }
    
    @Test
    public void assertExecuteDataSourcesContext() {
        RegistryCenterExecuteEngine executeEngine = new RegistryCenterExecuteEngine("sharding_db", mock(CreateDataSourcesStatement.class));
        BackendResponse response = executeEngine.execute(new ExecutionContext(dataSourcesContext, new LinkedList<>()));
        assertThat(response, instanceOf(ErrorResponse.class));
        setOrchestrationSchemaContexts(true);
        response = executeEngine.execute(new ExecutionContext(dataSourcesContext, new LinkedList<>()));
        assertThat(response, instanceOf(UpdateResponse.class));
    }
    
    @Test
    public void assertExecuteShardingRuleContext() {
        RegistryCenterExecuteEngine executeEngine = new RegistryCenterExecuteEngine("sharding_db", mock(CreateShardingRuleStatement.class));
        BackendResponse response = executeEngine.execute(new ExecutionContext(ruleContext, new LinkedList<>()));
        assertThat(response, instanceOf(ErrorResponse.class));
        setOrchestrationSchemaContexts(true);
        response = executeEngine.execute(new ExecutionContext(ruleContext, new LinkedList<>()));
        assertThat(response, instanceOf(UpdateResponse.class));
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setOrchestrationSchemaContexts(final boolean isOrchestration) {
        Field schemaContexts = ProxySchemaContexts.getInstance().getClass().getDeclaredField("schemaContexts");
        schemaContexts.setAccessible(true);
        if (isOrchestration) {
            schemaContexts.set(ProxySchemaContexts.getInstance(), mock(OrchestrationSchemaContextsFixture.class));
        } else {
            schemaContexts.set(ProxySchemaContexts.getInstance(), new StandardSchemaContexts());
        }
    }
    
    @After
    public void setDown() {
        setOrchestrationSchemaContexts(false);
    }
}
