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

package org.apache.shardingsphere.infra.config.rule.checker;

import org.apache.shardingsphere.infra.config.rule.checker.fixture.RuleConfigurationCheckerFixture;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.fixture.FixtureRuleConfiguration;
import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public final class RuleConfigurationCheckerFactoryTest {
    
    @SuppressWarnings("rawtypes")
    @Test
    public void assertFindInstance() {
        Optional<RuleConfigurationChecker> checker = RuleConfigurationCheckerFactory.findInstance(new FixtureRuleConfiguration());
        assertTrue(checker.isPresent());
        assertThat(checker.get(), instanceOf(RuleConfigurationCheckerFixture.class));
    }
    
    @Test
    public void assertFindInstanceWithoutChecker() {
        assertFalse(RuleConfigurationCheckerFactory.findInstance(mock(RuleConfiguration.class)).isPresent());
    }
}
