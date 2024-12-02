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

import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.rule.builder.global.GlobalRuleBuilder;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;
import org.apache.shardingsphere.logging.config.LoggingRuleConfiguration;
import org.apache.shardingsphere.logging.constant.LoggingConstants;
import org.apache.shardingsphere.logging.logger.ShardingSphereLogger;
import org.apache.shardingsphere.logging.rule.LoggingRule;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class LoggingRuleBuilderTest {
    
    @Test
    void assertBuildWhenSyncPropertiesToRule() {
        ShardingSphereLogger logger = new ShardingSphereLogger(LoggingConstants.SQL_LOG_TOPIC, null, false, null);
        LoggingRuleConfiguration ruleConfig = new LoggingRuleConfiguration(Collections.singleton(logger), Collections.emptyList());
        ConfigurationProperties props = new ConfigurationProperties(PropertiesBuilder.build(
                new Property(LoggingConstants.SQL_SHOW, Boolean.TRUE.toString()), new Property(LoggingConstants.SQL_SIMPLE, Boolean.TRUE.toString())));
        LoggingRuleBuilder ruleBuilder = (LoggingRuleBuilder) OrderedSPILoader.getServices(GlobalRuleBuilder.class, Collections.singleton(ruleConfig)).get(ruleConfig);
        LoggingRule actual = ruleBuilder.build(ruleConfig, Collections.emptyList(), props);
        assertThat(actual.getConfiguration(), is(ruleConfig));
        assertThat(logger.getProps().getProperty(LoggingConstants.SQL_LOG_ENABLE), is(Boolean.TRUE.toString()));
        assertThat(logger.getProps().getProperty(LoggingConstants.SQL_LOG_SIMPLE), is(Boolean.TRUE.toString()));
    }
    
    @Test
    void assertBuildWhenSyncRuleToProperties() {
        ShardingSphereLogger logger = new ShardingSphereLogger(LoggingConstants.SQL_LOG_TOPIC, null, false, null);
        logger.getProps().setProperty(LoggingConstants.SQL_LOG_ENABLE, Boolean.TRUE.toString());
        logger.getProps().setProperty(LoggingConstants.SQL_LOG_SIMPLE, Boolean.TRUE.toString());
        LoggingRuleConfiguration ruleConfig = new LoggingRuleConfiguration(Collections.singleton(logger), Collections.emptyList());
        ConfigurationProperties props = new ConfigurationProperties(new Properties());
        LoggingRuleBuilder ruleBuilder = (LoggingRuleBuilder) OrderedSPILoader.getServices(GlobalRuleBuilder.class, Collections.singleton(ruleConfig)).get(ruleConfig);
        LoggingRule actual = ruleBuilder.build(ruleConfig, Collections.emptyList(), props);
        assertThat(actual.getConfiguration(), is(ruleConfig));
        assertThat(props.getProps().getProperty(LoggingConstants.SQL_SHOW), is(Boolean.TRUE.toString()));
        assertThat(props.getProps().getProperty(LoggingConstants.SQL_SIMPLE), is(Boolean.TRUE.toString()));
    }
    
    @Test
    void assertBuildWhenSyncNothing() {
        ShardingSphereLogger logger = new ShardingSphereLogger(LoggingConstants.SQL_LOG_TOPIC, null, false, null);
        LoggingRuleConfiguration ruleConfig = new LoggingRuleConfiguration(Collections.singleton(logger), Collections.emptyList());
        ConfigurationProperties props = new ConfigurationProperties(new Properties());
        LoggingRuleBuilder ruleBuilder = (LoggingRuleBuilder) OrderedSPILoader.getServices(GlobalRuleBuilder.class, Collections.singleton(ruleConfig)).get(ruleConfig);
        LoggingRule actual = ruleBuilder.build(ruleConfig, Collections.emptyList(), props);
        assertThat(actual.getConfiguration(), is(ruleConfig));
        assertFalse(logger.getProps().containsKey(LoggingConstants.SQL_LOG_ENABLE));
        assertFalse(logger.getProps().containsKey(LoggingConstants.SQL_LOG_SIMPLE));
        assertFalse(props.getProps().containsKey(LoggingConstants.SQL_SHOW));
        assertFalse(props.getProps().containsKey(LoggingConstants.SQL_SIMPLE));
    }
}
