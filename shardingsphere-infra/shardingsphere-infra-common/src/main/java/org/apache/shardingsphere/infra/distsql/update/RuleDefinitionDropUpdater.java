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

package org.apache.shardingsphere.infra.distsql.update;

import org.apache.shardingsphere.infra.config.RuleConfiguration;
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
     * Get the configuration that exists in the configuration that will be dropped.
     *
     * @param sqlStatement SQL statement
     * @param currentRuleConfig current rule configuration to be updated
     * @return can be updated or not
     */
    boolean needToBeUpdated(T sqlStatement, R currentRuleConfig);
    
    /**
     * Update current rule configuration.
     *
     * @param sqlStatement SQL statement
     * @param currentRuleConfig current rule configuration to be updated
     * @return current rule configuration is empty or not 
     */
    boolean updateCurrentRuleConfiguration(T sqlStatement, R currentRuleConfig);
    
    /**
     * Get identical data.
     *
     * @param col1 collection
     * @param col2 collection
     * @return identical data
     */
    default Collection<String> getIdenticalData(Collection<String> col1, Collection<String> col2) {
        return col1.stream().filter(col2::contains).collect(Collectors.toSet());
    }
}
