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

package org.apache.shardingsphere.logging.yaml.swapper;

import org.apache.shardingsphere.logging.config.LoggingRuleConfiguration;
import org.apache.shardingsphere.logging.logger.ShardingSphereAppender;
import org.apache.shardingsphere.logging.logger.ShardingSphereLogger;
import org.apache.shardingsphere.logging.yaml.config.YamlLoggingRuleConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class YamlLoggingRuleConfigurationSwapperTest {
    
    private final YamlLoggingRuleConfigurationSwapper swapper = new YamlLoggingRuleConfigurationSwapper();
    
    private final YamlLoggerSwapper loggerSwapper = new YamlLoggerSwapper();
    
    private final YamlAppenderSwapper appenderSwapper = new YamlAppenderSwapper();
    
    @Test
    void assertSwapToYamlConfiguration() {
        YamlLoggingRuleConfiguration yamlLoggingRuleConfiguration = swapper.swapToYamlConfiguration(createLoggingRuleConfiguration());
        assertThat(yamlLoggingRuleConfiguration.getLoggers().size(), is(1));
        assertThat(yamlLoggingRuleConfiguration.getAppenders().size(), is(1));
    }
    
    private LoggingRuleConfiguration createLoggingRuleConfiguration() {
        return new LoggingRuleConfiguration(Collections.singleton(new ShardingSphereLogger("ROOT", "INFO", true, "console")),
                Collections.singleton(new ShardingSphereAppender("console", "ch.qos.logback.core.ConsoleAppender", "[%-5level] %d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %logger{36} - %msg%n")));
    }
    
    @Test
    void assertSwapToObject() {
        LoggingRuleConfiguration loggingRuleConfiguration = swapper.swapToObject(createYamlLoggingRuleConfiguration());
        assertThat(loggingRuleConfiguration.getLoggers().size(), is(1));
        assertThat(loggingRuleConfiguration.getAppenders().size(), is(1));
    }
    
    private YamlLoggingRuleConfiguration createYamlLoggingRuleConfiguration() {
        YamlLoggingRuleConfiguration result = new YamlLoggingRuleConfiguration();
        result.setLoggers(Collections.singleton(loggerSwapper.swapToYamlConfiguration(new ShardingSphereLogger("ROOT", "INFO", true, "console"))));
        result.setAppenders(Collections.singleton(appenderSwapper.swapToYamlConfiguration(
                new ShardingSphereAppender("console", "ch.qos.logback.core.ConsoleAppender", "[%-5level] %d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %logger{36} - %msg%n"))));
        return result;
    }
}
