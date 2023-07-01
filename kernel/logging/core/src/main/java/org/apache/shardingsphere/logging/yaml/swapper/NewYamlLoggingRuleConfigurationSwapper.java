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

import org.apache.shardingsphere.infra.config.nodepath.GlobalNodePath;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.util.yaml.datanode.YamlDataNode;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.NewYamlGlobalRuleConfigurationSwapper;
import org.apache.shardingsphere.logging.config.LoggingRuleConfiguration;
import org.apache.shardingsphere.logging.constant.LoggingOrder;
import org.apache.shardingsphere.logging.rule.builder.DefaultLoggingRuleConfigurationBuilder;
import org.apache.shardingsphere.logging.yaml.config.YamlAppendersConfigurationConverter;
import org.apache.shardingsphere.logging.yaml.config.YamlLoggersConfigurationConverter;
import org.apache.shardingsphere.logging.yaml.config.YamlLoggingRuleConfiguration;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * TODO Rename YamlLoggingRuleConfigurationSwapper when metadata structure adjustment completed. #25485
 * YAML logging rule configuration swapper.
 */
public final class NewYamlLoggingRuleConfigurationSwapper implements NewYamlGlobalRuleConfigurationSwapper<LoggingRuleConfiguration> {
    
    @Override
    public Collection<YamlDataNode> swapToDataNodes(final LoggingRuleConfiguration data) {
        return Collections.singletonList(new YamlDataNode(getRuleTagName().toLowerCase(), YamlEngine.marshal(swapToYamlConfiguration(data))));
    }
    
    private YamlLoggingRuleConfiguration swapToYamlConfiguration(final LoggingRuleConfiguration data) {
        YamlLoggingRuleConfiguration result = new YamlLoggingRuleConfiguration();
        result.setLoggers(YamlLoggersConfigurationConverter.convertYamlLoggerConfigurations(data.getLoggers()));
        result.setAppenders(YamlAppendersConfigurationConverter.convertYamlAppenderConfigurations(data.getAppenders()));
        return result;
    }
    
    @Override
    public Optional<LoggingRuleConfiguration> swapToObject(final Collection<YamlDataNode> dataNodes) {
        for (YamlDataNode each : dataNodes) {
            Optional<String> version = GlobalNodePath.getVersion(getRuleTagName().toLowerCase(), each.getKey());
            if (!version.isPresent()) {
                continue;
            }
            return Optional.of(swapToObject(YamlEngine.unmarshal(each.getValue(), YamlLoggingRuleConfiguration.class)));
        }
        return Optional.empty();
    }
    
    private LoggingRuleConfiguration swapToObject(final YamlLoggingRuleConfiguration yamlConfig) {
        LoggingRuleConfiguration result = new LoggingRuleConfiguration(YamlLoggersConfigurationConverter.convertShardingSphereLogger(yamlConfig.getLoggers()),
                YamlAppendersConfigurationConverter.convertShardingSphereAppender(yamlConfig.getAppenders()));
        if (null == result.getLoggers()) {
            result = getDefaultLoggingRuleConfiguration();
        }
        return result;
    }
    
    private LoggingRuleConfiguration getDefaultLoggingRuleConfiguration() {
        return new DefaultLoggingRuleConfigurationBuilder().build();
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
