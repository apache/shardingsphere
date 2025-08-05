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

package org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.spi.global;

import org.apache.shardingsphere.distsql.handler.aware.DistSQLExecutorRuleAware;
import org.apache.shardingsphere.distsql.statement.type.rdl.rule.global.GlobalRuleDefinitionStatement;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.spi.annotation.SingletonSPI;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPI;

/**
 * Global rule definition executor.
 *
 * @param <T> type of rule definition statement
 * @param <R> type of rule
 */
@SingletonSPI
public interface GlobalRuleDefinitionExecutor<T extends GlobalRuleDefinitionStatement, R extends ShardingSphereRule> extends DistSQLExecutorRuleAware<R>, TypedSPI {
    
    /**
     * Check before update.
     *
     * @param sqlStatement SQL statement
     */
    default void checkBeforeUpdate(T sqlStatement) {
    }
    
    /**
     * Build to be altered rule configuration.
     *
     * @param sqlStatement SQL statement
     * @return built to be altered rule configuration
     */
    RuleConfiguration buildToBeAlteredRuleConfiguration(T sqlStatement);
    
    @Override
    Class<T> getType();
}
