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

package org.apache.shardingsphere.distsql.handler.update;

import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Drop rule definition updater.
 * 
 * @param <T> type of SQL statement
 * @param <R> type of rule configuration
 */
public interface RuleDefinitionDropUpdater<T extends SQLStatement, R extends RuleConfiguration> extends RuleDefinitionUpdater<T, R> {
    
    /**
     * TODO Remove temporary default implementation
     * Build to be dropped rule configuration.
     *
     * @param currentRuleConfig current rule configuration to be updated
     * @param sqlStatement SQL statement
     * @return to be dropped rule configuration
     */
    default R buildToBeDroppedRuleConfiguration(R currentRuleConfig, T sqlStatement) {
        return null;
    }
    
    /**
     * Build to be altered rule configuration.
     *
     * @param currentRuleConfig current rule configuration to be updated
     * @param sqlStatement SQL statement
     * @return to be altered rule configuration
     */
    default R buildToBeAlteredRuleConfiguration(R currentRuleConfig, T sqlStatement) {
        return null;
    }
    
    /**
     * TODO remove this method after remove Compatible_Cluster mode
     * Update current rule configuration.
     *
     * @param sqlStatement SQL statement
     * @param currentRuleConfig current rule configuration to be updated
     * @return current rule configuration is empty or not
     */
    boolean updateCurrentRuleConfiguration(T sqlStatement, R currentRuleConfig);
    
    /**
     * Whether there is configuration.
     *
     * @param currentRuleConfig current rule configuration 
     * @return configuration exists or does not exist
     */
    default boolean isExistRuleConfig(R currentRuleConfig) {
        return null != currentRuleConfig;
    }
    
    /**
     * Whether there is dropped data.
     * 
     * @param sqlStatement SQL statement
     * @param currentRuleConfig current rule configuration
     * @return dropped data exists or does not exist
     */
    default boolean hasAnyOneToBeDropped(T sqlStatement, R currentRuleConfig) {
        return true;
    }
    
    /**
     * Get identical data.
     *
     * @param currentRules collection
     * @param toBeDroppedRules collection
     * @return identical data
     */
    default Collection<String> getIdenticalData(Collection<String> currentRules, Collection<String> toBeDroppedRules) {
        return currentRules.stream().filter(each -> toBeDroppedRules.stream().anyMatch(each::equalsIgnoreCase)).collect(Collectors.toSet());
    }
}
