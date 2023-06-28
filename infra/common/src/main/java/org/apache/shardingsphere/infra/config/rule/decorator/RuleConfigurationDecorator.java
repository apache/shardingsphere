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

package org.apache.shardingsphere.infra.config.rule.decorator;

import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.util.spi.annotation.SingletonSPI;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPI;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Map;

/**
 * Rule configuration decorator.
 * 
 * @param <T> type of rule configuration
 */
@SingletonSPI
public interface RuleConfigurationDecorator<T extends RuleConfiguration> extends TypedSPI {
    
    /**
     * Decorate rule configuration.
     * 
     * @param databaseName database name
     * @param dataSources data sources
     * @param builtRules built rules
     * @param ruleConfig rule configuration to be decorated
     * @return decorated rule configuration
     */
    T decorate(String databaseName, Map<String, DataSource> dataSources, Collection<ShardingSphereRule> builtRules, T ruleConfig);
}
