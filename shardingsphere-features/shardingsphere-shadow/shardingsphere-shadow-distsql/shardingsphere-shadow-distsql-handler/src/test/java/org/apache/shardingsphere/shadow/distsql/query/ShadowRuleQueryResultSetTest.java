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

package org.apache.shardingsphere.shadow.distsql.query;

import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.distsql.query.DistSQLResultSet;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.api.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.api.config.table.ShadowTableConfiguration;
import org.apache.shardingsphere.shadow.distsql.handler.query.ShadowRuleQueryResultSet;
import org.apache.shardingsphere.shadow.distsql.parser.statement.ShowShadowRulesStatement;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShadowRuleQueryResultSetTest {
    
    @Test
    public void assertGetRowData() {
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
        when(metaData.getRuleMetaData().getConfigurations()).thenReturn(Collections.singleton(createRuleConfiguration()));
        DistSQLResultSet resultSet = new ShadowRuleQueryResultSet();
        resultSet.init(metaData, mock(ShowShadowRulesStatement.class));
        List<Object> actual = new ArrayList<>(resultSet.getRowData());
        assertThat(actual.size(), is(4));
        assertThat(actual.get(0), is("shadow_rule"));
        assertThat(actual.get(1), is("source"));
        assertThat(actual.get(2), is("shadow"));
        assertThat(actual.get(3), is("t_order,t_order_1"));
    }
    
    private RuleConfiguration createRuleConfiguration() {
        // FIXME because the defined final attribute will be removed, here is just for the new object
        ShadowRuleConfiguration result = new ShadowRuleConfiguration();
        result.getDataSources().put("shadow_rule", new ShadowDataSourceConfiguration("source", "shadow"));
        result.getTables().put("t_order", new ShadowTableConfiguration(Collections.singletonList("shadow_rule"), Collections.emptyList()));
        result.getTables().put("t_order_1", new ShadowTableConfiguration(Collections.singletonList("shadow_rule"), Collections.emptyList()));
        return result;
    }
}
