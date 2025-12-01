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

package org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.spi.database.type;

import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.spi.database.DatabaseRuleDefinitionExecutor;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;

/**
 * Database rule drop executor.
 *
 * @param <T> type of SQL statement
 * @param <R> type of rule
 * @param <C> type of rule configuration
 */
public interface DatabaseRuleDropExecutor<T extends SQLStatement, R extends ShardingSphereRule, C extends RuleConfiguration> extends DatabaseRuleDefinitionExecutor<T, R> {
    
    /**
     * TODO Remove temporary default implementation
     * Build to be dropped rule configuration.
     *
     * @param sqlStatement SQL statement
     * @return to be dropped rule configuration
     */
    default C buildToBeDroppedRuleConfiguration(final T sqlStatement) {
        return null;
    }
    
    /**
     * Build to be altered rule configuration.
     *
     * @param sqlStatement SQL statement
     * @return to be altered rule configuration
     */
    default C buildToBeAlteredRuleConfiguration(final T sqlStatement) {
        return null;
    }
    
    /**
     * Whether there is dropped data.
     *
     * @param sqlStatement SQL statement
     * @return dropped data exists or does not exist
     */
    default boolean hasAnyOneToBeDropped(final T sqlStatement) {
        return true;
    }
}
