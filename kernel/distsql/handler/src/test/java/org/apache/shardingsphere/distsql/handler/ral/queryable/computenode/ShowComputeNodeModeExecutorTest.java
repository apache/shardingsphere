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

package org.apache.shardingsphere.distsql.handler.ral.queryable.computenode;

import org.apache.shardingsphere.distsql.handler.engine.query.DistSQLQueryExecutor;
import org.apache.shardingsphere.distsql.statement.type.ral.queryable.show.ShowComputeNodeModeStatement;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.config.mode.PersistRepositoryConfiguration;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShowComputeNodeModeExecutorTest {
    
    private final ShowComputeNodeModeExecutor executor = (ShowComputeNodeModeExecutor) TypedSPILoader.getService(DistSQLQueryExecutor.class, ShowComputeNodeModeStatement.class);
    
    @Test
    void assertGetColumnNames() {
        assertThat(executor.getColumnNames(mock(ShowComputeNodeModeStatement.class)), is(Arrays.asList("type", "repository", "props")));
    }
    
    @Test
    void assertExecute() {
        Properties props = new Properties();
        props.setProperty("key", "value1,value2");
        PersistRepositoryConfiguration repositoryConfig = mock(PersistRepositoryConfiguration.class);
        when(repositoryConfig.getType()).thenReturn("ZooKeeper");
        when(repositoryConfig.getProps()).thenReturn(props);
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getComputeNodeInstanceContext().getModeConfiguration()).thenReturn(new ModeConfiguration("Cluster", repositoryConfig));
        Collection<LocalDataQueryResultRow> actual = executor.getRows(mock(ShowComputeNodeModeStatement.class), contextManager);
        LocalDataQueryResultRow row = actual.iterator().next();
        assertThat(row.getCell(1), is("Cluster"));
        assertThat(row.getCell(2), is("ZooKeeper"));
        assertThat(row.getCell(3), is("{\"key\":\"value1,value2\"}"));
    }
    
    @Test
    void assertExecuteWithNullRepository() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getComputeNodeInstanceContext().getModeConfiguration()).thenReturn(new ModeConfiguration("Standalone", null));
        Collection<LocalDataQueryResultRow> actual = executor.getRows(mock(ShowComputeNodeModeStatement.class), contextManager);
        LocalDataQueryResultRow row = actual.iterator().next();
        assertThat(row.getCell(1), is("Standalone"));
        assertThat(row.getCell(2), is(""));
        assertThat(row.getCell(3), is(""));
    }
}
