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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.distsql.statement.DistSQLStatement;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;

/**
 * Global rule definition executor factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GlobalRuleDefinitionExecutorFactory {
    
    /**
     * Create new instance.
     *
     * @param sqlStatement SQL statement
     * @param globalRuleMetaData global rule meta data
     * @return created instance 
     */
    @SuppressWarnings("rawtypes")
    public static GlobalRuleDefinitionExecutor newInstance(final DistSQLStatement sqlStatement, final RuleMetaData globalRuleMetaData) {
        GlobalRuleDefinitionExecutor result = TypedSPILoader.getService(GlobalRuleDefinitionExecutor.class, sqlStatement.getClass());
        setAttributes(result, globalRuleMetaData);
        return result;
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void setAttributes(final GlobalRuleDefinitionExecutor executor, final RuleMetaData globalRuleMetaData) {
        executor.setRule(globalRuleMetaData.getSingleRule(executor.getRuleClass()));
    }
}
