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

package org.apache.shardingsphere.proxy.backend.text.distsql.rql.impl;

import org.apache.shardingsphere.dbdiscovery.api.config.DatabaseDiscoveryRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.api.config.rule.DatabaseDiscoveryDataSourceRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.statement.ShowDatabaseDiscoveryRulesStatement;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.proxy.backend.text.distsql.rql.RuleQueryResultSet;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public final class DatabaseDiscoveryRuleQueryResultSetTest extends BaseRuleQueryResultSet {
    
    @Override
    protected Collection<RuleConfiguration> buildRuleConfigurations() {
        DatabaseDiscoveryDataSourceRuleConfiguration databaseDiscoveryDataSourceRuleConfig = new DatabaseDiscoveryDataSourceRuleConfiguration("ms_group", Arrays.asList("ds_0", "ds_1"), "test");
        ShardingSphereAlgorithmConfiguration shardingSphereAlgorithmConfig = new ShardingSphereAlgorithmConfiguration("MGR", new Properties());
        Map<String, ShardingSphereAlgorithmConfiguration> discoverTypes = new HashMap<>(1, 1);
        discoverTypes.put("test", shardingSphereAlgorithmConfig);
        return Collections.singleton(new DatabaseDiscoveryRuleConfiguration(Collections.singleton(databaseDiscoveryDataSourceRuleConfig), discoverTypes));
    }
    
    @Test
    public void assertGetRowData() {
        RuleQueryResultSet resultSet = new DatabaseDiscoveryRuleQueryResultSet();
        resultSet.init("test", mock(ShowDatabaseDiscoveryRulesStatement.class));
        Collection<Object> actual = resultSet.getRowData();
        assertThat(actual.size(), is(4));
        assertTrue(actual.contains("ms_group"));
        assertTrue(actual.contains("ds_0,ds_1"));
        assertTrue(actual.contains("MGR"));
    }
}
