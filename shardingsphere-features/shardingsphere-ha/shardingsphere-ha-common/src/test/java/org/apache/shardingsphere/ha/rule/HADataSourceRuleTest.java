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
import org.apache.shardingsphere.ha.api.config.rule.HADataSourceRuleConfiguration;
import org.apache.shardingsphere.ha.mgr.MGRHAType;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class HADataSourceRuleTest {
    
    private final HADataSourceRule haDataSourceRule = new HADataSourceRule(
            new HADataSourceRuleConfiguration("test_pr", Arrays.asList("ds_0", "ds_1"), "haTypeName"), new MGRHAType());
    
    @Test(expected = IllegalArgumentException.class)
    public void assertNewHADataSourceRuleWithoutName() {
        new HADataSourceRule(new HADataSourceRuleConfiguration("", Arrays.asList("ds_0", "ds_1"), "haTypeName"), new MGRHAType());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertNewHADataSourceRuleWithNullDataSourceName() {
        new HADataSourceRule(new HADataSourceRuleConfiguration("ds", null, "haTypeName"), new MGRHAType());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertNewHADataSourceRuleWithEmptyDataSourceName() {
        new HADataSourceRule(new HADataSourceRuleConfiguration("ds", Collections.emptyList(), "haTypeName"), new MGRHAType());
    }
    
    @Test
    public void assertGetDataSourceNamesWithoutDisabledDataSourceNames() {
        assertThat(haDataSourceRule.getDataSourceNames(), is(Arrays.asList("ds_0", "ds_1")));
    }
    
    @Test
    public void assertGetDataSourceNamesWithDisabledDataSourceNames() {
        haDataSourceRule.updateDisabledDataSourceNames("ds_0", true);
        assertThat(haDataSourceRule.getDataSourceNames(), is(Collections.singletonList("ds_1")));
    }
    
    @Test
    public void assertUpdateDisabledDataSourceNamesForDisabled() {
        haDataSourceRule.updateDisabledDataSourceNames("ds_0", true);
        assertThat(haDataSourceRule.getDataSourceNames(), is(Collections.singletonList("ds_1")));
    }
    
    @Test
    public void assertUpdateDisabledDataSourceNamesForEnabled() {
        haDataSourceRule.updateDisabledDataSourceNames("ds_0", true);
        haDataSourceRule.updateDisabledDataSourceNames("ds_0", false);
        assertThat(haDataSourceRule.getDataSourceNames(), is(Arrays.asList("ds_0", "ds_1")));
    }
    
    @Test
    public void assertGetDataSourceMapper() {
        Map<String, Collection<String>> actual = haDataSourceRule.getDataSourceMapper();
        Map<String, Collection<String>> expected = ImmutableMap.of("test_pr", Arrays.asList("ds_0", "ds_1"));
        assertThat(actual, is(expected));
    }
}
