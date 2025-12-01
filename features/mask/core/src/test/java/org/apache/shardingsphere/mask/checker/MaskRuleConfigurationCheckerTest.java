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

package org.apache.shardingsphere.mask.checker;

import org.apache.shardingsphere.infra.algorithm.core.exception.UnregisteredAlgorithmException;
import org.apache.shardingsphere.infra.config.rule.checker.DatabaseRuleConfigurationChecker;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;
import org.apache.shardingsphere.mask.config.MaskRuleConfiguration;
import org.apache.shardingsphere.mask.config.rule.MaskColumnRuleConfiguration;
import org.apache.shardingsphere.mask.config.rule.MaskTableRuleConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MaskRuleConfigurationCheckerTest {
    
    @SuppressWarnings("rawtypes")
    private DatabaseRuleConfigurationChecker checker;
    
    @BeforeEach
    void setUp() {
        checker = OrderedSPILoader.getServicesByClass(DatabaseRuleConfigurationChecker.class, Collections.singleton(MaskRuleConfiguration.class)).get(MaskRuleConfiguration.class);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertValidCheck() {
        MaskRuleConfiguration ruleConfig = mockValidConfiguration();
        assertDoesNotThrow(() -> checker.check("test", ruleConfig, Collections.emptyMap(), Collections.emptyList()));
        assertTrue(checker.getTableNames(ruleConfig).contains("t_mask"));
    }
    
    private MaskRuleConfiguration mockValidConfiguration() {
        MaskRuleConfiguration result = mock(MaskRuleConfiguration.class);
        MaskTableRuleConfiguration tableRuleConfig = mock(MaskTableRuleConfiguration.class);
        when(tableRuleConfig.getName()).thenReturn("t_mask");
        when(result.getTables()).thenReturn(Collections.singleton(tableRuleConfig));
        return result;
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertInvalidCheck() {
        MaskRuleConfiguration ruleConfig = mockInvalidConfiguration();
        assertThrows(UnregisteredAlgorithmException.class, () -> checker.check("test", ruleConfig, Collections.emptyMap(), Collections.emptyList()));
    }
    
    private MaskRuleConfiguration mockInvalidConfiguration() {
        MaskRuleConfiguration result = mock(MaskRuleConfiguration.class);
        MaskTableRuleConfiguration tableRuleConfig = mock(MaskTableRuleConfiguration.class);
        MaskColumnRuleConfiguration columnRuleConfig = mock(MaskColumnRuleConfiguration.class);
        when(columnRuleConfig.getMaskAlgorithm()).thenReturn("md5");
        when(tableRuleConfig.getColumns()).thenReturn(Collections.singleton(columnRuleConfig));
        when(result.getTables()).thenReturn(Collections.singleton(tableRuleConfig));
        return result;
    }
}
