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

import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapper;
import org.apache.shardingsphere.logging.config.LoggingRuleConfiguration;
import org.apache.shardingsphere.logging.constant.LoggingOrder;
import org.apache.shardingsphere.logging.yaml.config.YamlLoggingRuleConfiguration;

import java.util.stream.Collectors;

/**
 * YAML logging rule configuration swapper.
 */
public final class YamlLoggingRuleConfigurationSwapper implements YamlRuleConfigurationSwapper<YamlLoggingRuleConfiguration, LoggingRuleConfiguration> {
    
    private final YamlLoggerSwapper loggerSwapper = new YamlLoggerSwapper();
    
    private final YamlAppenderSwapper appenderSwapper = new YamlAppenderSwapper();
    
    @Override
    public YamlLoggingRuleConfiguration swapToYamlConfiguration(final LoggingRuleConfiguration data) {
        YamlLoggingRuleConfiguration result = new YamlLoggingRuleConfiguration();
        result.setLoggers(data.getLoggers().stream().map(loggerSwapper::swapToYamlConfiguration).collect(Collectors.toList()));
        result.setAppenders(data.getAppenders().stream().map(appenderSwapper::swapToYamlConfiguration).collect(Collectors.toList()));
        return result;
    }
    
    @Override
    public LoggingRuleConfiguration swapToObject(final YamlLoggingRuleConfiguration yamlConfig) {
        return new LoggingRuleConfiguration(yamlConfig.getLoggers().stream().map(loggerSwapper::swapToObject).collect(Collectors.toList()),
                yamlConfig.getAppenders().stream().map(appenderSwapper::swapToObject).collect(Collectors.toList()));
    }
    
    @Override
    public Class<LoggingRuleConfiguration> getTypeClass() {
        return LoggingRuleConfiguration.class;
    }
    
    @Override
    public String getRuleTagName() {
        return "LOGGING";
    }
    
    @Override
    public int getOrder() {
        return LoggingOrder.ORDER;
    }
}
