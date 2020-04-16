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

import java.util.Optional;

/**
 * Rule configuration convert Facade.
 */
public final class RuleConfigurationConvertFacade {
    
    /**
     * match by YAML context.
     *
     * @param classType class type
     * @param <T> type of class
     * @param context YAML content
     * @return is match or not
     */
    public static <T extends RuleConfiguration> boolean match(final Class<T> classType, final String context) {
        return RuleConfigurationConvertFactory.newInstance(classType).match(context);
    }
    
    /**
     * Match and convert by YAML context.
     *
     * @param context YAML context
     * @return rule configuration from YAML
     */
    public static Optional<RuleConfiguration> matchAndConvert(final String context) {
        return RuleConfigurationConvertFactory.getRuleConfigurationConverters().stream().filter(each -> each.match(context)).map(each -> each.unmarshal(context)).findFirst();
    }
    
    /**
     * convert by YAML context.
     *
     * @param context YAML content
     * @param classType class type
     * @param <T> type of class
     * @return rule configuration from YAML
     */
    public static <T extends RuleConfiguration> RuleConfiguration convert(final Class<T> classType, final String context) {
        return RuleConfigurationConvertFactory.newInstance(classType).unmarshal(context);
    }
    
    /**
     * Marshal YAML to rule configuration.
     *
     * @param ruleConfiguration  rule configuration
     * @param shardingSchemaName sharding schema name
     * @param <T> type of class
     * @return YAML context
     */
    public static <T extends RuleConfiguration> String marshal(final T ruleConfiguration, final String shardingSchemaName) {
        return RuleConfigurationConvertFactory.newInstance(ruleConfiguration).marshal(ruleConfiguration, shardingSchemaName);
    }
}



