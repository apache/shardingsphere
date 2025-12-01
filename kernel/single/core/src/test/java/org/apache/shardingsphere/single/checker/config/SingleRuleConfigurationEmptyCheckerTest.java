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

package org.apache.shardingsphere.single.checker.config;

import org.apache.shardingsphere.infra.config.rule.checker.DatabaseRuleConfigurationEmptyChecker;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.single.config.SingleRuleConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SingleRuleConfigurationEmptyCheckerTest {
    
    private SingleRuleConfigurationEmptyChecker checker;
    
    @BeforeEach
    void setUp() {
        checker = (SingleRuleConfigurationEmptyChecker) TypedSPILoader.getService(DatabaseRuleConfigurationEmptyChecker.class, SingleRuleConfiguration.class);
    }
    
    @Test
    void assertIsEmpty() {
        assertTrue(checker.isEmpty(new SingleRuleConfiguration()));
    }
    
    @Test
    void assertIsNotEmptyWhenContainsTables() {
        assertFalse(checker.isEmpty(new SingleRuleConfiguration(Collections.singleton("foo_tbl"), null)));
    }
    
    @Test
    void assertIsNotEmptyWhenContainsDefaultDataSource() {
        assertFalse(checker.isEmpty(new SingleRuleConfiguration(Collections.emptyList(), "foo_ds")));
    }
}
