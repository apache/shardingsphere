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

import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.spi.annotation.SingletonSPI;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPI;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Database rule configuration checker.
 * 
 * @param <T> type of rule configuration
 */
@SingletonSPI
public interface DatabaseRuleConfigurationChecker<T extends RuleConfiguration> extends OrderedSPI<T> {
    
    /**
     * Check rule configuration.
     *
     * @param databaseName database name to be checked
     * @param ruleConfig rule configuration to be checked
     * @param dataSourceMap data sources to be checked
     * @param builtRules built rules
     */
    void check(String databaseName, T ruleConfig, Map<String, DataSource> dataSourceMap, Collection<ShardingSphereRule> builtRules);
    
    /**
     * Get required data source names.
     *
     * @param ruleConfig rule configuration
     * @return required data source names
     */
    default Collection<String> getRequiredDataSourceNames(final T ruleConfig) {
        return Collections.emptyList();
    }
    
    /**
     * Get table names.
     *
     * @param ruleConfig rule configuration
     * @return table names
     */
    default Collection<String> getTableNames(final T ruleConfig) {
        return Collections.emptyList();
    }
}
