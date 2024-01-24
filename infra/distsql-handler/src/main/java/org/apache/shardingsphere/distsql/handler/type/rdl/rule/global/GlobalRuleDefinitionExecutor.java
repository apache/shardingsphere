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

package org.apache.shardingsphere.distsql.handler.type.rdl.rule.global;

import org.apache.shardingsphere.distsql.statement.rdl.rule.RuleDefinitionStatement;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.spi.annotation.SingletonSPI;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPI;

/**
 * Global rule definition executor.
 * 
 * @param <T> type of rule definition statement
 * @param <R> type of rule configuration
 */
@SingletonSPI
public interface GlobalRuleDefinitionExecutor<T extends RuleDefinitionStatement, R extends RuleConfiguration> extends TypedSPI {
    
    /**
     * Check SQL statement.
     *
     * @param currentRuleConfig current rule configuration
     * @param sqlStatement SQL statement
     */
    void checkSQLStatement(R currentRuleConfig, T sqlStatement);
    
    /**
     * Build altered rule configuration.
     *
     * @param currentRuleConfig current rule configuration
     * @param sqlStatement SQL statement
     * @return built altered rule configuration
     */
    RuleConfiguration buildAlteredRuleConfiguration(R currentRuleConfig, T sqlStatement);
    
    /**
     * Get rule configuration class.
     *
     * @return rule configuration class
     */
    Class<R> getRuleConfigurationClass();
    
    @Override
    Class<T> getType();
}
