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

package org.apache.shardingsphere.shadow.rule;

import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class ShadowRuleTest {
    
    @Test(expected = IllegalArgumentException.class)
    public void assertNewWithEmptyDataSourceRule() {
        new ShadowRule(new ShadowRuleConfiguration("", Collections.emptyList(), Collections.emptyList()));
    }
    
    @Test
    public void assertFindDataSourceRule() {
        assertShadowRule(createShadowRule());
    }
    
    private ShadowRule createShadowRule() {
        ShadowRuleConfiguration configuration = new ShadowRuleConfiguration("shadow", Arrays.asList("ds", "ds1"), Arrays.asList("shadow_ds", "shadow_ds1"));
        return new ShadowRule(configuration);
    }
    
    private void assertShadowRule(final ShadowRule rule) {
        assertThat(rule.getColumn(), is("shadow"));
        assertThat(rule.getShadowMappings().size(), is(2));
        assertThat(rule.getShadowMappings().get("ds"), is("shadow_ds"));
        assertThat(rule.getShadowMappings().get("ds1"), is("shadow_ds1"));
    }
}
