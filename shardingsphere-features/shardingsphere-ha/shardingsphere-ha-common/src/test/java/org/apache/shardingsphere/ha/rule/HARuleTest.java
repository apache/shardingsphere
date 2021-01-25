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

package org.apache.shardingsphere.ha.rule;

import com.google.common.collect.ImmutableMap;
import org.apache.shardingsphere.ha.api.config.HARuleConfiguration;
import org.apache.shardingsphere.ha.api.config.rule.HADataSourceRuleConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.rule.event.impl.DataSourceNameDisabledEvent;
import org.junit.Test;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public final class HARuleTest {
    
    private final Map<String, DataSource> dataSourceMap = Collections.singletonMap("ds", mock(DataSource.class));
    
    @Test(expected = IllegalArgumentException.class)
    public void assertNewWithEmptyDataSourceRule() {
        new HARule(new HARuleConfiguration(Collections.emptyList(), Collections.emptyMap(), Collections.emptyMap()), mock(DatabaseType.class), dataSourceMap, "ha_db");
    }
    
    @Test
    public void assertFindDataSourceRule() {
        Optional<HADataSourceRule> actual = createHARule().findDataSourceRule("test_pr");
        assertTrue(actual.isPresent());
        assertDataSourceRule(actual.get());
    }
    
    @Test
    public void assertGetSingleDataSourceRule() {
        assertDataSourceRule(createHARule().getSingleDataSourceRule());
    }
    
    private HARule createHARule() {
        HADataSourceRuleConfiguration config =
                new HADataSourceRuleConfiguration("test_pr", Arrays.asList("replica_ds_0", "replica_ds_1"), "random", true, "haTypeName");
        return new HARule(new HARuleConfiguration(
                Collections.singleton(config), ImmutableMap.of("random", new ShardingSphereAlgorithmConfiguration("RANDOM", new Properties())),
                ImmutableMap.of("mgr", new ShardingSphereAlgorithmConfiguration("MGR", new Properties()))),
                mock(DatabaseType.class), dataSourceMap, "ha_db");
    }
    
    private void assertDataSourceRule(final HADataSourceRule actual) {
        assertThat(actual.getName(), is("test_pr"));
        assertThat(actual.getDataSourceNames(), is(Arrays.asList("replica_ds_0", "replica_ds_1")));
        assertThat(actual.getLoadBalancer().getType(), is("RANDOM"));
    }
    
    @Test
    public void assertUpdateRuleStatusWithNotExistDataSource() {
        HARule haRule = createHARule();
        haRule.updateRuleStatus(new DataSourceNameDisabledEvent("replica_db", true));
        assertThat(haRule.getSingleDataSourceRule().getDataSourceNames(), is(Arrays.asList("replica_ds_0", "replica_ds_1")));
    }
    
    @Test
    public void assertUpdateRuleStatus() {
        HARule haRule = createHARule();
        haRule.updateRuleStatus(new DataSourceNameDisabledEvent("replica_ds_0", true));
        assertThat(haRule.getSingleDataSourceRule().getDataSourceNames(), is(Collections.singletonList("replica_ds_1")));
    }
    
    @Test
    public void assertUpdateRuleStatusWithEnable() {
        HARule haRule = createHARule();
        haRule.updateRuleStatus(new DataSourceNameDisabledEvent("replica_ds_0", true));
        assertThat(haRule.getSingleDataSourceRule().getDataSourceNames(), is(Collections.singletonList("replica_ds_1")));
        haRule.updateRuleStatus(new DataSourceNameDisabledEvent("replica_ds_0", false));
        assertThat(haRule.getSingleDataSourceRule().getDataSourceNames(), is(Arrays.asList("replica_ds_0", "replica_ds_1")));
    }
    
    @Test
    public void assertGetDataSourceMapper() {
        HARule haRule = createHARule();
        Map<String, Collection<String>> actual = haRule.getDataSourceMapper();
        Map<String, Collection<String>> expected = ImmutableMap.of("test_pr", Arrays.asList("replica_ds_0", "replica_ds_1"));
        assertThat(actual, is(expected));
    }
}
