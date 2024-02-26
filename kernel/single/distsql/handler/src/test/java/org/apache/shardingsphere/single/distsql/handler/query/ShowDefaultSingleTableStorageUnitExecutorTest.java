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

package org.apache.shardingsphere.single.distsql.handler.query;

import org.apache.shardingsphere.distsql.handler.engine.DistSQLConnectionContext;
import org.apache.shardingsphere.distsql.handler.engine.query.DistSQLQueryExecuteEngine;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.single.api.config.SingleRuleConfiguration;
import org.apache.shardingsphere.single.distsql.statement.rql.ShowDefaultSingleTableStorageUnitStatement;
import org.apache.shardingsphere.single.rule.SingleRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShowDefaultSingleTableStorageUnitExecutorTest {
    
    private DistSQLQueryExecuteEngine engine;
    
    @BeforeEach
    void setUp() {
        engine = new DistSQLQueryExecuteEngine(mock(ShowDefaultSingleTableStorageUnitStatement.class), "foo_db", mockContextManager(), mock(DistSQLConnectionContext.class));
    }
    
    private ContextManager mockContextManager() {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(result.getDatabase("foo_db")).thenReturn(database);
        SingleRule rule = mock(SingleRule.class);
        when(rule.getConfiguration()).thenReturn(new SingleRuleConfiguration(Collections.emptyList(), "foo_ds"));
        when(database.getRuleMetaData().findSingleRule(SingleRule.class)).thenReturn(Optional.of(rule));
        return result;
    }
    
    @Test
    void assertGetRowData() throws SQLException {
        engine.executeQuery();
        Collection<LocalDataQueryResultRow> actual = engine.getRows();
        assertThat(actual.size(), is(1));
        Iterator<LocalDataQueryResultRow> rowData = actual.iterator();
        String defaultSingleTableStorageUnit = (String) rowData.next().getCell(1);
        assertThat(defaultSingleTableStorageUnit, is("foo_ds"));
    }
}
