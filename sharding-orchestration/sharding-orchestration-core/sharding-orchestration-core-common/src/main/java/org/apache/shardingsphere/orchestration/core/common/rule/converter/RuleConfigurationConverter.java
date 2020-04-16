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

package org.apache.shardingsphere.orchestration.core.common.rule.converter;

import org.apache.shardingsphere.underlying.common.config.RuleConfiguration;

/**
 * Rule configuration converter.
 * @param <T> type of rule configuration
 */
public interface RuleConfigurationConverter<T extends RuleConfiguration> {
    
    /**
     * YAML context match of schema name.
     *
     * @param context YAML context
     * @return boolean
     */
    default boolean match(String context) {
        return false;
    }
    
    /**
     * Unmarshal rule configuration by YAML context.
     *
     * @param context YAML context
     * @return rule configuration
     */
    T unmarshal(String context);
    
    /**
     * Marshal rule configuration to YAML context.
     *
     * @param ruleConfiguration rule configuration
     * @param shardingSchemaName sharding schema name
     * @return YAML content
     */
    String marshal(T ruleConfiguration, String shardingSchemaName);
}
