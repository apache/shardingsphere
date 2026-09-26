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

package org.apache.shardingsphere.globalclock.distsql.handler;

import org.apache.shardingsphere.distsql.handler.executor.export.ExportedSnapshotInfo;
import org.apache.shardingsphere.distsql.handler.executor.export.ExportedSnapshotInfoGenerator;
import org.apache.shardingsphere.globalclock.config.GlobalClockRuleConfiguration;
import org.apache.shardingsphere.globalclock.provider.GlobalClockProvider;
import org.apache.shardingsphere.globalclock.rule.GlobalClockRule;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalClockExportedSnapshotInfoGeneratorTest {
    
    private final ExportedSnapshotInfoGenerator generator = ShardingSphereServiceLoader.getServiceInstances(ExportedSnapshotInfoGenerator.class).iterator().next();
    
    @Test
    void assertGenerateWhenDisabled() {
        GlobalClockRule rule = mock(GlobalClockRule.class);
        GlobalClockRuleConfiguration ruleConfig = mock(GlobalClockRuleConfiguration.class);
        when(rule.getConfiguration()).thenReturn(ruleConfig);
        Optional<ExportedSnapshotInfo> actual = generator.generate(mockMetaData(rule));
        assertFalse(actual.isPresent());
    }
    
    @Test
    void assertGenerateWithCurrentTimestamp() {
        GlobalClockRule rule = mock(GlobalClockRule.class);
        GlobalClockRuleConfiguration ruleConfig = mock(GlobalClockRuleConfiguration.class);
        when(ruleConfig.isEnabled()).thenReturn(true);
        when(rule.getConfiguration()).thenReturn(ruleConfig);
        GlobalClockProvider provider = mock(GlobalClockProvider.class);
        when(provider.getCurrentTimestamp()).thenReturn(123L);
        when(rule.getGlobalClockProvider()).thenReturn(Optional.of(provider));
        Optional<ExportedSnapshotInfo> actual = generator.generate(mockMetaData(rule));
        assertTrue(actual.isPresent());
        assertThat(actual.get().getCsn(), is("123"));
        assertNotNull(actual.get().getCreateTime());
    }
    
    @Test
    void assertGenerateWithoutProvider() {
        GlobalClockRule rule = mock(GlobalClockRule.class);
        GlobalClockRuleConfiguration ruleConfig = mock(GlobalClockRuleConfiguration.class);
        when(ruleConfig.isEnabled()).thenReturn(true);
        when(rule.getConfiguration()).thenReturn(ruleConfig);
        when(rule.getGlobalClockProvider()).thenReturn(Optional.empty());
        Optional<ExportedSnapshotInfo> actual = generator.generate(mockMetaData(rule));
        assertTrue(actual.isPresent());
        assertThat(actual.get().getCsn(), is("0"));
    }
    
    private ShardingSphereMetaData mockMetaData(final GlobalClockRule rule) {
        ShardingSphereMetaData result = mock(ShardingSphereMetaData.class);
        RuleMetaData ruleMetaData = mock(RuleMetaData.class);
        when(result.getGlobalRuleMetaData()).thenReturn(ruleMetaData);
        when(ruleMetaData.getSingleRule(GlobalClockRule.class)).thenReturn(rule);
        return result;
    }
}
