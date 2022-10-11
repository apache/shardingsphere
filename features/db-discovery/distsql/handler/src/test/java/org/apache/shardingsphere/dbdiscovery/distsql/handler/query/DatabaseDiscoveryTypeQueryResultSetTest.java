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

import org.apache.shardingsphere.dbdiscovery.api.config.DatabaseDiscoveryRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.api.config.rule.DatabaseDiscoveryDataSourceRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.statement.ShowDatabaseDiscoveryRulesStatement;
import org.apache.shardingsphere.dbdiscovery.rule.DatabaseDiscoveryRule;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.distsql.query.DatabaseDistSQLResultSet;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class DatabaseDiscoveryTypeQueryResultSetTest {
    
    @Test
    public void assertGetRowData() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        DatabaseDiscoveryRule rule = mock(DatabaseDiscoveryRule.class);
        when(rule.getConfiguration()).thenReturn(createRuleConfiguration());
        ShardingSphereRuleMetaData databaseRuleMetaData = mock(ShardingSphereRuleMetaData.class);
        when(database.getRuleMetaData()).thenReturn(databaseRuleMetaData);
        when(databaseRuleMetaData.getSingleRule(DatabaseDiscoveryRule.class)).thenReturn(rule);
        DatabaseDistSQLResultSet resultSet = new DatabaseDiscoveryTypeQueryResultSet();
        resultSet.init(database, mock(ShowDatabaseDiscoveryRulesStatement.class));
        Collection<String> columnNames = resultSet.getColumnNames();
        List<Object> actual = new ArrayList<>(resultSet.getRowData());
        assertThat(columnNames.size(), is(3));
        assertThat(actual.size(), is(3));
        assertThat(actual.get(0), is("test_name"));
        assertThat(actual.get(1), is("MySQL.MGR"));
        assertThat(actual.get(2).toString(), is("{type_key=type_value}"));
    }
    
    private RuleConfiguration createRuleConfiguration() {
        DatabaseDiscoveryDataSourceRuleConfiguration databaseDiscoveryDataSourceRuleConfig = new DatabaseDiscoveryDataSourceRuleConfiguration("ms_group", Arrays.asList("ds_0", "ds_1"),
                "ms-heartbeat", "test");
        Properties discoveryTypeProps = new Properties();
        discoveryTypeProps.put("type_key", "type_value");
        AlgorithmConfiguration shardingSphereAlgorithmConfig = new AlgorithmConfiguration("MySQL.MGR", discoveryTypeProps);
        Map<String, AlgorithmConfiguration> discoverTypes = Collections.singletonMap("test_name", shardingSphereAlgorithmConfig);
        return new DatabaseDiscoveryRuleConfiguration(Collections.singleton(databaseDiscoveryDataSourceRuleConfig), Collections.emptyMap(), discoverTypes);
    }
}
