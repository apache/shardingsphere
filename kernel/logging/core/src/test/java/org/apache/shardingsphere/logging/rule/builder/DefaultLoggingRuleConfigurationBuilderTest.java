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

package org.apache.shardingsphere.logging.rule.builder;

import org.apache.shardingsphere.infra.rule.builder.global.DefaultGlobalRuleConfigurationBuilder;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;
import org.apache.shardingsphere.logging.config.LoggingRuleConfiguration;
import org.apache.shardingsphere.logging.rule.builder.fixture.FixtureILoggerFactory;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(LoggerFactory.class)
class DefaultLoggingRuleConfigurationBuilderTest {
    
    @SuppressWarnings("rawtypes")
    @Test
    void assertBuildWithDefaultShardingSphereLogBuilder() {
        when(LoggerFactory.getILoggerFactory()).thenReturn(mock(ILoggerFactory.class));
        DefaultGlobalRuleConfigurationBuilder builder =
                OrderedSPILoader.getServices(DefaultGlobalRuleConfigurationBuilder.class, Collections.singleton(new LoggingRuleBuilder())).values().iterator().next();
        LoggingRuleConfiguration actual = (LoggingRuleConfiguration) builder.build();
        assertTrue(actual.getLoggers().isEmpty());
        assertTrue(actual.getAppenders().isEmpty());
    }
    
    @SuppressWarnings("rawtypes")
    @Test
    void assertBuildWithTypedShardingSphereLogBuilder() {
        when(LoggerFactory.getILoggerFactory()).thenReturn(new FixtureILoggerFactory());
        DefaultGlobalRuleConfigurationBuilder builder =
                OrderedSPILoader.getServices(DefaultGlobalRuleConfigurationBuilder.class, Collections.singleton(new LoggingRuleBuilder())).values().iterator().next();
        LoggingRuleConfiguration actual = (LoggingRuleConfiguration) builder.build();
        assertThat(actual.getLoggers().size(), is(1));
        assertThat(actual.getAppenders().size(), is(1));
    }
}
