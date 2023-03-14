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

package org.apache.shardingsphere.dbdiscovery.distsql.handler.query;

import org.apache.shardingsphere.dbdiscovery.distsql.parser.statement.CountDatabaseDiscoveryRuleStatement;
import org.apache.shardingsphere.dbdiscovery.rule.DatabaseDiscoveryRule;
import org.apache.shardingsphere.distsql.handler.query.RQLExecutor;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class CountDatabaseDiscoveryRuleExecutorTest {
    
    @Test
    public void assertGetRowData() {
        RQLExecutor<CountDatabaseDiscoveryRuleStatement> executor = new CountDatabaseDiscoveryRuleExecutor();
        Collection<LocalDataQueryResultRow> actual = executor.getRows(mockDatabase(), mock(CountDatabaseDiscoveryRuleStatement.class));
        assertThat(actual.size(), is(1));
        Iterator<LocalDataQueryResultRow> iterator = actual.iterator();
        LocalDataQueryResultRow row = iterator.next();
        assertThat(row.getCell(1), is("db_discovery"));
        assertThat(row.getCell(2), is("db_1"));
        assertThat(row.getCell(3), is(2));
    }
    
    @Test
    public void assertGetColumnNames() {
        RQLExecutor<CountDatabaseDiscoveryRuleStatement> executor = new CountDatabaseDiscoveryRuleExecutor();
        Collection<String> columns = executor.getColumnNames();
        assertThat(columns.size(), is(3));
        Iterator<String> iterator = columns.iterator();
        assertThat(iterator.next(), is("rule_name"));
        assertThat(iterator.next(), is("database"));
        assertThat(iterator.next(), is("count"));
    }
    
    private ShardingSphereDatabase mockDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(result.getName()).thenReturn("db_1");
        DatabaseDiscoveryRule databaseDiscoveryRule = mockDatabaseDiscoveryRule();
        when(result.getRuleMetaData()).thenReturn(new ShardingSphereRuleMetaData(Collections.singleton(databaseDiscoveryRule)));
        return result;
    }
    
    private DatabaseDiscoveryRule mockDatabaseDiscoveryRule() {
        DatabaseDiscoveryRule result = mock(DatabaseDiscoveryRule.class);
        Map<String, Collection<String>> datasourceMapper = new HashMap<>(2, 1);
        datasourceMapper.put("ds_0", Collections.singletonList("ds_0"));
        datasourceMapper.put("ds_1", Collections.singletonList("ds_1"));
        when(result.getDataSourceMapper()).thenReturn(datasourceMapper);
        return result;
    }
}
