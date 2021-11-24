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

package org.apache.shardingsphere.sharding.distsql.query;

import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.distsql.query.DistSQLResultSet;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.query.ShardingTablesCountQueryResultSet;
import org.apache.shardingsphere.sharding.distsql.parser.statement.ShowShardingTableRulesStatement;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShardingTablesCountQueryResultSetTest {
    
    @Test
    public void assertGetRowData() {
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
        when(metaData.getRuleMetaData().getConfigurations()).thenReturn(createRuleConfigurations());
        DistSQLResultSet resultSet = new ShardingTablesCountQueryResultSet();
        resultSet.init(metaData, mock(ShowShardingTableRulesStatement.class));
        List<Object> actual = new ArrayList<>(resultSet.getRowData());
        assertThat(actual.size(), is(2));
        assertThat(actual.get(0), is("t_order"));
        assertThat(actual.get(1), is(4));
        resultSet.next();
        actual = new ArrayList<>(resultSet.getRowData());
        assertThat(actual.get(0), is("t_product"));
        assertThat(actual.get(1), is(2));
    }
    
    @Test
    public void assertGetRowDataWithoutRule() {
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
        ShardingRuleConfiguration emptyRuleConfiguration = new ShardingRuleConfiguration();
        when(metaData.getRuleMetaData().getConfigurations()).thenReturn(Collections.singleton(emptyRuleConfiguration));
        DistSQLResultSet resultSet = new ShardingTablesCountQueryResultSet();
        resultSet.init(metaData, mock(ShowShardingTableRulesStatement.class));
        assertFalse(resultSet.next());
    }
    
    private Collection<RuleConfiguration> createRuleConfigurations() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        result.getTables().add(new ShardingTableRuleConfiguration("t_order", "ds_${0..1}.t_order_${0..1}"));
        result.getAutoTables().add(new ShardingAutoTableRuleConfiguration("t_product", "ds_${0..1}.t_order_${0..1}"));
        return Collections.singleton(result);
    }
}
