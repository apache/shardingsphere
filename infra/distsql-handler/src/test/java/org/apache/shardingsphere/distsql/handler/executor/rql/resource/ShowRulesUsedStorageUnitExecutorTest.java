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

package org.apache.shardingsphere.distsql.handler.executor.rql.resource;

import org.apache.shardingsphere.distsql.handler.fixture.DistSQLHandlerFixtureRule;
import org.apache.shardingsphere.distsql.statement.type.rql.rule.database.ShowRulesUsedStorageUnitStatement;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.FromDatabaseSegment;
import org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDataSource;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShowRulesUsedStorageUnitExecutorTest {
    
    private final ShowRulesUsedStorageUnitExecutor executor = new ShowRulesUsedStorageUnitExecutor();
    
    @Test
    void assertGetRowData() {
        executor.setDatabase(mockDatabase());
        ShowRulesUsedStorageUnitStatement sqlStatement = new ShowRulesUsedStorageUnitStatement("foo_ds", mock(FromDatabaseSegment.class));
        Collection<LocalDataQueryResultRow> rowData = executor.getRows(sqlStatement, mock(ContextManager.class));
        assertThat(rowData.size(), is(1));
        Iterator<LocalDataQueryResultRow> actual = rowData.iterator();
        LocalDataQueryResultRow row = actual.next();
        assertThat(row.getCell(1), is("dist_s_q_l_handler_fixture"));
        assertThat(row.getCell(2), is("foo_tbl"));
    }
    
    private ShardingSphereDatabase mockDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class);
        when(result.getRuleMetaData()).thenReturn(new RuleMetaData(Collections.singleton(new DistSQLHandlerFixtureRule())));
        when(result.getResourceMetaData()).thenReturn(new ResourceMetaData(Collections.singletonMap("foo_ds", new MockedDataSource())));
        return result;
    }
    
    @Test
    void assertGetEmptyRowData() {
        executor.setDatabase(mockEmptyDatabase());
        ShowRulesUsedStorageUnitStatement sqlStatement = new ShowRulesUsedStorageUnitStatement("empty_ds", mock(FromDatabaseSegment.class));
        assertTrue(executor.getRows(sqlStatement, mock(ContextManager.class)).isEmpty());
    }
    
    private ShardingSphereDatabase mockEmptyDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class);
        when(result.getRuleMetaData()).thenReturn(new RuleMetaData(Collections.emptyList()));
        when(result.getResourceMetaData()).thenReturn(new ResourceMetaData(Collections.singletonMap("empty_ds", new MockedDataSource())));
        return result;
    }
}
