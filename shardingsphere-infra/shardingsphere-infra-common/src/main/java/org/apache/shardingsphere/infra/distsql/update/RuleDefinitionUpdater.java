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
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.spi.typed.TypedSPI;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

/**
 * Rule definition updater.
 * 
 * @param <T> type of SQL statement
 * @param <R> type of rule configuration
 */
public interface RuleDefinitionUpdater<T extends SQLStatement, R extends RuleConfiguration> extends TypedSPI {
    
    /**
     * Check SQL statement.
     *
     * @param shardingSphereMetaData ShardingSphere meta data
     * @param sqlStatement SQL statement
     * @param currentRuleConfig current rule configuration
     * @throws DistSQLException definition violation exception
     */
    void checkSQLStatement(ShardingSphereMetaData shardingSphereMetaData, T sqlStatement, R currentRuleConfig) throws DistSQLException;
    
    /**
     * Get rule configuration class.
     * 
     * @return rule configuration class
     */
    Class<R> getRuleConfigurationClass();
}
