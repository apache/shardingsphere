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

package org.apache.shardingsphere.shadow.checker;

import org.apache.shardingsphere.infra.config.rule.checker.DatabaseRuleConfigurationEmptyChecker;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.shadow.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.config.table.ShadowTableConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class ShadowRuleConfigurationEmptyCheckerTest {
    
    private ShadowRuleConfigurationEmptyChecker checker;
    
    @BeforeEach
    void setUp() {
        checker = (ShadowRuleConfigurationEmptyChecker) TypedSPILoader.getService(DatabaseRuleConfigurationEmptyChecker.class, ShadowRuleConfiguration.class);
    }
    
    @Test
    void assertIsEmptyDataSources() {
        ShadowRuleConfiguration ruleConfig = new ShadowRuleConfiguration();
        ruleConfig.setTables(Collections.singletonMap("foo_tbl", mock(ShadowTableConfiguration.class)));
        assertTrue(checker.isEmpty(ruleConfig));
    }
    
    @Test
    void assertIsEmptyTables() {
        ShadowRuleConfiguration ruleConfig = new ShadowRuleConfiguration();
        ruleConfig.setDataSources(Collections.singleton(mock(ShadowDataSourceConfiguration.class)));
        assertTrue(checker.isEmpty(ruleConfig));
    }
    
    @Test
    void assertIsNotEmpty() {
        ShadowRuleConfiguration ruleConfig = new ShadowRuleConfiguration();
        ruleConfig.setDataSources(Collections.singleton(mock(ShadowDataSourceConfiguration.class)));
        ruleConfig.setTables(Collections.singletonMap("foo_tbl", mock(ShadowTableConfiguration.class)));
        assertFalse(checker.isEmpty(ruleConfig));
    }
}
