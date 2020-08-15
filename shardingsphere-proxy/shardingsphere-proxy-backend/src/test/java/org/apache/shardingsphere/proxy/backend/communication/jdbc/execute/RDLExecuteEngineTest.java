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
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.kernel.context.SchemaContext;
import org.apache.shardingsphere.kernel.context.StandardSchemaContexts;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.execute.engine.RDLExecuteEngine;
import org.apache.shardingsphere.proxy.backend.response.BackendResponse;
import org.apache.shardingsphere.proxy.backend.response.error.ErrorResponse;
import org.apache.shardingsphere.proxy.backend.response.update.UpdateResponse;
import org.apache.shardingsphere.proxy.backend.schema.ProxySchemaContexts;
import org.apache.shardingsphere.rdl.parser.binder.context.CreateDataSourcesStatementContext;
import org.apache.shardingsphere.rdl.parser.binder.context.CreateShardingRuleStatementContext;
import org.apache.shardingsphere.rdl.parser.statement.rdl.CreateDataSourcesStatement;
import org.apache.shardingsphere.rdl.parser.statement.rdl.CreateShardingRuleStatement;
import org.apache.shardingsphere.rdl.parser.statement.rdl.TableRuleSegment;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public final class RDLExecuteEngineTest {
    
    private CreateDataSourcesStatementContext dataSourcesContext;
    
    private CreateShardingRuleStatementContext ruleContext;
    
    @Before
    public void setUp() {
        createDataSourcesContext();
        createRuleContext();
    }
    
    private void createDataSourcesContext() {
        dataSourcesContext = new CreateDataSourcesStatementContext(new CreateDataSourcesStatement(Collections.emptyList()), new MySQLDatabaseType());
    }
    
    private void createRuleContext() {
        TableRuleSegment segment = new TableRuleSegment();
        segment.setLogicTable("t_order");
        segment.setDataSources(Arrays.asList("ds0", "ds1"));
        segment.setShardingColumn("order_id");
        segment.setAlgorithmType("MOD");
        segment.setProperties(Collections.singleton("2"));
        ruleContext = new CreateShardingRuleStatementContext(new CreateShardingRuleStatement(Collections.singleton(segment)));
    }
    
    @Test
    public void assertExecuteDataSourcesContext() {
        SchemaContext context = new SchemaContext("sharding_db", null, null);
        RDLExecuteEngine executeEngine = new RDLExecuteEngine(context, mock(CreateDataSourcesStatement.class));
        BackendResponse response = executeEngine.execute(new ExecutionContext(dataSourcesContext, new LinkedList<>()));
        assertThat(response, instanceOf(ErrorResponse.class));
        setOrchestrationSchemaContexts(true);
        response = executeEngine.execute(new ExecutionContext(dataSourcesContext, new LinkedList<>()));
        assertThat(response, instanceOf(UpdateResponse.class));
    }
    
    @Test
    public void assertExecuteShardingRuleContext() {
        SchemaContext context = new SchemaContext("sharding_db", null, null);
        RDLExecuteEngine executeEngine = new RDLExecuteEngine(context, mock(CreateShardingRuleStatement.class));
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
