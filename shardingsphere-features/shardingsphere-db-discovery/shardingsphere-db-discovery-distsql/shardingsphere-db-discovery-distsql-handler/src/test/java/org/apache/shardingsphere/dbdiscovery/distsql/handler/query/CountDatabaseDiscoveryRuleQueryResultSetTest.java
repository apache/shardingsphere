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
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class CountDatabaseDiscoveryRuleQueryResultSetTest {
    
    @Test
    public void assertGetRowData() {
        CountDatabaseDiscoveryRuleQueryResultSet resultSet = new CountDatabaseDiscoveryRuleQueryResultSet();
        resultSet.init(mockDatabase(), mock(CountDatabaseDiscoveryRuleStatement.class));
        assertTrue(resultSet.next());
        List<Object> actual = new ArrayList<>(resultSet.getRowData());
        assertThat(actual.size(), is(3));
        assertThat(actual.get(0), is("db_discovery"));
        assertThat(actual.get(1), is("db_1"));
        assertThat(actual.get(2), is(2));
        assertFalse(resultSet.next());
    }
    
    private ShardingSphereDatabase mockDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(result.getName()).thenReturn("db_1");
        ShardingSphereRuleMetaData ruleMetaData = mock(ShardingSphereRuleMetaData.class);
        DatabaseDiscoveryRule databaseDiscoveryRule = mockDatabaseDiscoveryRule();
        when(ruleMetaData.findSingleRule(DatabaseDiscoveryRule.class)).thenReturn(Optional.of(databaseDiscoveryRule));
        when(result.getRuleMetaData()).thenReturn(ruleMetaData);
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
