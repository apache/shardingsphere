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

package org.apache.shardingsphere.broadcast.rule;

import org.apache.shardingsphere.broadcast.config.BroadcastRuleConfiguration;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.attribute.RuleAttributes;
import org.apache.shardingsphere.infra.rule.attribute.datasource.DataSourceMapperRuleAttribute;
import org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDataSource;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BroadcastRuleTest {
    
    @Test
    void assertGetDataSourceNames() {
        BroadcastRule rule = new BroadcastRule(
                new BroadcastRuleConfiguration(Collections.emptyList()), mockDataSourceMap(), Arrays.asList(mockBuiltRule(), mock(ShardingSphereRule.class, RETURNS_DEEP_STUBS)));
        assertThat(rule.getDataSourceNames(), is(Collections.singleton("foo_ds")));
    }
    
    private static Map<String, DataSource> mockDataSourceMap() {
        Map<String, DataSource> result = new HashMap<>(2, 1F);
        result.put("foo_ds_0", new MockedDataSource());
        result.put("foo_ds_1", new MockedDataSource());
        return result;
    }
    
    private ShardingSphereRule mockBuiltRule() {
        DataSourceMapperRuleAttribute ruleAttribute = mock(DataSourceMapperRuleAttribute.class);
        Map<String, Collection<String>> dataSourceMapper = new HashMap<>(2, 1F);
        dataSourceMapper.put("foo_ds", Arrays.asList("foo_ds_0", "foo_ds_1"));
        dataSourceMapper.put("bar_ds", Arrays.asList("bar_ds_0", "bar_ds_1"));
        when(ruleAttribute.getDataSourceMapper()).thenReturn(dataSourceMapper);
        ShardingSphereRule result = mock(ShardingSphereRule.class);
        when(result.getAttributes()).thenReturn(new RuleAttributes(ruleAttribute));
        return result;
    }
    
    @Test
    void assertGetBroadcastTableNames() {
        BroadcastRule rule = new BroadcastRule(new BroadcastRuleConfiguration(Collections.singleton("foo_tbl")), Collections.emptyMap(), Collections.emptyList());
        assertThat(rule.getBroadcastTableNames(Arrays.asList("foo_tbl", "bar_tbl")), is(Collections.singleton("foo_tbl")));
    }
}
