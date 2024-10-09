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
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.logging.config.LoggingRuleConfiguration;
import org.apache.shardingsphere.logging.constant.LoggingOrder;
import org.apache.shardingsphere.logging.spi.ShardingSphereLogBuilder;
import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;

import java.util.Collections;

/**
 * Default logging rule configuration builder.
 */
public final class DefaultLoggingRuleConfigurationBuilder implements DefaultGlobalRuleConfigurationBuilder<LoggingRuleConfiguration, LoggingRuleBuilder> {
    
    @SuppressWarnings("unchecked")
    @Override
    public LoggingRuleConfiguration build() {
        ILoggerFactory loggerFactory = LoggerFactory.getILoggerFactory();
        return TypedSPILoader.findService(ShardingSphereLogBuilder.class, loggerFactory.getClass())
                .map(optional -> new LoggingRuleConfiguration(optional.getDefaultLoggers(loggerFactory), optional.getDefaultAppenders(loggerFactory)))
                .orElseGet(() -> new LoggingRuleConfiguration(Collections.emptyList(), Collections.emptySet()));
    }
    
    @Override
    public int getOrder() {
        return LoggingOrder.ORDER;
    }
    
    @Override
    public Class<LoggingRuleBuilder> getTypeClass() {
        return LoggingRuleBuilder.class;
    }
}
