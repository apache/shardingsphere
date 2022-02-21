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
import org.apache.shardingsphere.infra.distsql.query.DistSQLResultSet;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class DatabaseDiscoveryHeartbeatQueryResultSetTest {
    
    @Test
    public void assertGetRowData() {
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
        when(metaData.getRuleMetaData().findRuleConfiguration(any())).thenReturn(Collections.singleton(createRuleConfiguration()));
        DistSQLResultSet resultSet = new DatabaseDiscoveryHeartbeatQueryResultSet();
        resultSet.init(metaData, mock(ShowDatabaseDiscoveryRulesStatement.class));
        Collection<String> columnNames = resultSet.getColumnNames();
        ArrayList<Object> actual = new ArrayList<>(resultSet.getRowData());
        assertThat(columnNames.size(), is(2));
        assertThat(actual.size(), is(2));
        assertThat(actual.get(0), is("test_name"));
        assertThat(actual.get(1).toString(), is("{type_key=type_value}"));
    }
    
    private RuleConfiguration createRuleConfiguration() {
        DatabaseDiscoveryDataSourceRuleConfiguration databaseDiscoveryDataSourceRuleConfig = new DatabaseDiscoveryDataSourceRuleConfiguration("ms_group", Arrays.asList("ds_0", "ds_1"),
                "ms-heartbeat", "test");
        Properties discoveryTypeProps = new Properties();
        discoveryTypeProps.put("type_key", "type_value");
        DatabaseDiscoveryHeartBeatConfiguration shardingSphereAlgorithmConfig = new DatabaseDiscoveryHeartBeatConfiguration(discoveryTypeProps);
        Map<String, DatabaseDiscoveryHeartBeatConfiguration> discoverHeartbeat = new HashMap<>(1, 1);
        discoverHeartbeat.put("test_name", shardingSphereAlgorithmConfig);
        return new DatabaseDiscoveryRuleConfiguration(Collections.singleton(databaseDiscoveryDataSourceRuleConfig), discoverHeartbeat, Collections.emptyMap());
    }
}
