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
import org.apache.shardingsphere.dbdiscovery.api.config.rule.DatabaseDiscoveryHeartBeatConfiguration;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.statement.ShowDatabaseDiscoveryRulesStatement;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.distsql.query.DistSQLResultSet;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.rule.identifier.type.ExportableRule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class DatabaseDiscoveryRuleQueryResultSetTest {
    
    @Test
    public void assertGetRowData() {
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
        when(metaData.getRuleMetaData().getConfigurations()).thenReturn(Collections.singleton(createRuleConfiguration()));
        ExportableRule exportableRule = mock(ExportableRule.class);
        when(exportableRule.export()).thenReturn(Collections.emptyMap());
        when(metaData.getRuleMetaData().getRules()).thenReturn(Collections.singleton(exportableRule));
        DistSQLResultSet resultSet = new DatabaseDiscoveryRuleQueryResultSet();
        resultSet.init(metaData, mock(ShowDatabaseDiscoveryRulesStatement.class));
        Collection<String> columnNames = resultSet.getColumnNames();
        ArrayList<Object> actual = new ArrayList<>(resultSet.getRowData());
        assertThat(columnNames.size(), is(5));
        columnNames.containsAll(Arrays.asList("name", "data_source_names", "primary_data_source_name", "discover_type", "heartbeat"));
        assertThat(actual.size(), is(5));
        assertThat(actual.get(0), is("ms_group"));
        assertThat(actual.get(1), is("ds_0,ds_1"));
        assertThat(actual.get(2), is(""));
        assertThat(actual.get(3).toString(), is("{name=type_test, type=MGR, props={}}"));
        assertThat(actual.get(4).toString(), is("{name=heartbeat_test, props={}}"));
    }
    
    private RuleConfiguration createRuleConfiguration() {
        DatabaseDiscoveryDataSourceRuleConfiguration databaseDiscoveryDataSourceRuleConfig = new DatabaseDiscoveryDataSourceRuleConfiguration("ms_group", Arrays.asList("ds_0", "ds_1"),
                "heartbeat_test", "type_test");
        ShardingSphereAlgorithmConfiguration shardingSphereAlgorithmConfig = new ShardingSphereAlgorithmConfiguration("MGR", new Properties());
        Map<String, ShardingSphereAlgorithmConfiguration> discoverTypes = new HashMap<>(1, 1);
        discoverTypes.put("type_test", shardingSphereAlgorithmConfig);
        Map<String, DatabaseDiscoveryHeartBeatConfiguration> discoveryHeartbeat = new HashMap<>(1, 1);
        discoveryHeartbeat.put("heartbeat_test", new DatabaseDiscoveryHeartBeatConfiguration(new Properties()));
        return new DatabaseDiscoveryRuleConfiguration(Collections.singleton(databaseDiscoveryDataSourceRuleConfig), discoveryHeartbeat, discoverTypes);
    }
}
